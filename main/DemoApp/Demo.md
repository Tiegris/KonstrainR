# AppleFruits inc. demo

This is a demonstration on the capabilities of the Konstrainer application through the story of a fictional company: AppleFruits inc.

## Background tale

AppleFruits inc. wants to modernize their business operations. The mangers decided that buying an existing solution is too expensive, so they opted for a custom application built inhouse. They also decided that it must be an application build on the microservices architecture, running in Kubernetes. The lack of experience, improper of planning and rushed deadlines resulted in many mistakes.

Their app is still work in progress and has limited functionality.

Let's see what mistakes they made and let's prevent many future mistakes they might make using the power of the Konstrainer platform.

## Demo

Make sure you are in the `DemoApp` folder.

Requirements:

- Docker
- Sandbox Kubernetes cluster
- Helm
- yq (Tested with version: (https://github.com/mikefarah/yq/) version 4.18.)

### Initial setup

**Optional:** You can build the Docker images for yourself, but they are publicly available, so this is optional. Run this, so the setups script builds the images for you. If you do this, you have to manually edit the items.yaml and users.yaml files to use your image.

```bash
export BUILD_IMAGES=1
export REPO=<your-docker.io-account-name>
```

Run the initial setup script. This replicates the current state of our hypothetical system by running some of the commands the engineers would have used, or running commands that simulate steps of development.

```bash
./initial-setup.sh
```

### Breakdown of setup

Let's break down the steps of the initial setup and put the story behind it.

When the the first two microservices of the app were created, the developers created a yaml file for each apps, and defined the the most basic k8s resources needed for them:

```bash
# Initial creation of users app
kubectl create ns users
kubectl apply -n users -f $home/k8s/users.yaml

# Initial creation of items app
kubectl create ns items
kubectl apply -n items -f $home/k8s/items.yaml
```

The resource descriptions were generated using the VS Code Kubernetes extension and slightly customized, so most values were left on default.

> Note that the items.yaml contains an unused pvc. This could be the result of a copy paste oversight, or just someone forgot to delete it. We will use Konstrainer to detect it.

The items app went through a long development. The setup script simulated the multiple redeployment of the app like this:

```bash
# Simulate a history of changes
for v in {1..14}; do
    yq eval 'select(.kind == "Deployment").spec.template.metadata.annotations.v = env(v)' $home/k8s/items.yaml -i
    kubectl apply -n items -f $home/k8s/items.yaml
done
# Restore yaml file
yq eval 'select(.kind == "Deployment").spec.template.metadata.annotations.v = "0"' $home/k8s/items.yaml -i
```

Also the engineers in charge of the kubernetes deployments created their test environments, and forgot to delete them.

This is simulated by these commands:

```bash
# Simulate John Athan creating test namespace
kubectl create ns jathan-test || true
# Simulate Ida Red creating test namespace and leaving some leftover junk in it
kubectl create ns ired-test || true
kubectl apply -n ired-test -f $home/k8s/junk.yaml
```

The developers decided that they want to use a NoSQL database. They searched the internet and one of the first search results was this: [https://bitnami.com/stack/mongodb/helm](https://bitnami.com/stack/mongodb/helm). This website shows a command that looks like it deploys a mongo db. The engineers tested it, and it worked.

```bash
kubectl create ns mongo
helm install -n mongo my-release oci://registry-1.docker.io/bitnamicharts/mongodb
```

The output of the command has some helpful tips. The engineers deploying mongo followed them like this:

```bash
export MONGODB_ROOT_PASSWORD=$(kubectl get secret --namespace mongo my-release-mongodb -o jsonpath="{.data.mongodb-root-password}" | base64 -d)
export MONGO_HOST="my-release-mongodb.mongo.svc.cluster.local"
```

They configured the items service to use mongo db:

```bash
yq eval 'select(.kind == "Deployment").spec.template.spec.containers[0].env[0].value = env(MONGODB_ROOT_PASSWORD)' $home/k8s/items.yaml -i
yq eval 'select(.kind == "Deployment").spec.template.spec.containers[0].env[1].value = env(MONGO_HOST)' $home/k8s/items.yaml -i
kubectl apply -n items -f $home/k8s/items.yaml
```

### Test Deployment

Let's test the app.

```bash
kubectl port-forward -n items service/items 8080:8080
# In a separate terminal
curl http://localhost:8080/items
```

If you got some JSON response, that means the setup was successful.

### Teardown

```bash
helm uninstall my-release -n mongo
kubectl delete ns users items jathan-test ired-test mongo
```

## Running Konstrainer

Install the Konstrainer helm chart.

```bash
cd ../charts
kubectl create ns konstrainer-ns
helm upgrade konstrainr-core konstrainr-core -n konstrainer-ns --install
kubectl port-forward service/konstrainer-core 8443:8080 -n konstrainer-ns
```

Wait for it to start, than port forward the core component.

```bash
kubectl port-forward service/konstrainer-core 8443:8080 -n konstrainer-ns
```

### Basic Diagnostics

First let's run the basic diagnostics on the cluster.

The Basic Diagnostics is a build dsl file, which generates a report of the most common best-practice violations in the cluster.

Go to [https://localhost:8443](https://localhost:8443). The site has a self signed certificate, configure your browser to trust it. For login use admin:admin. Upload the basic diagnostics dsl: [$PROJECT_ROOT/main/Konstrainer-Dsl/src/main/kotlin/me/btieger/builtins/BasicDiagnostic.kt](../Konstrainer-Dsl/src/main/kotlin/me/btieger/builtins/BasicDiagnostic.kt) Use browse, than submit. Wait for it to compile. The site does not refresh automatically, refresh the page after about 20 seconds to see if it's compiled. You should see: `Build status: Ready`

Click `Start Server`. Wait for it to start. After about 5 seconds refresh the page. You should see: `Server status: Up`

Click `Monitors` in the navigation menu. You should see the report generated by the BasicDiagnostic. Let's inspect it.

Here we can see many of the problem with our cluster.

Read the report like this:

- The outer most box is an agent. It is a server instance running in your cluster that generates reports or enforces constraints.

- It contains many aggregation groups. An aggregation group is a bundle of problems usually about the same kind of Kubernetes resources. The first aggregation group in this report is the: `Services`

  - An aggregation group holds Kubernetes resources which have problems. Resources with problems are called marked resources. In this case the only marked resource in the `Services` aggregation group is the: `services/users.users`

  - The format of the marked Kubernetes resources are: `apiKind/name.namespace`

  - The body of the box lists the problems separated by commas. In this case the only problem is: `No backend`

The `No backend` mark means that the service has no pods at all.

The users service was deployed in the using the `initial-setup.sh` and it is defined in the [./k8s/users.yaml](./k8s/users.yaml) file.

If we inspect this file very carefully, we can see that the selector of the service is looking for pods with the labels: `app: users`, but the deployment defines pods with the label: `app: user`. Honestly this is a very easy typo to make, so in this case the Konstrainer discovered a bug.

The next aggregation group is the `Deployments`. The most common tags are:

- No resources defined: This means that not all containers in the pods of the deployment have resource requests and limits set. This is a warning, it is usually a good practice to set the values.
- No node selector: This means that there is no logic provided on which node (usually a virtual machine) should the pods of the deployment run.
- No probes: No liveness probes are defined. Without probes the Kubernetes api can not determine if a pod is healthy or not, so it is a good practice to define probes.
- Has long history: The revisionHistoryLimit of the deployment is set to more than 4. The revisionHistoryLimit determines how many replicasets should the deployment keep as a history. The default is 10, but if it is too many, it can result in a lot of unused resources which can put an unnecessary load on the Kubernetes api, so it is a good practice to set it lower than the default.

The next aggregation group with problems is the PVCs. It tells us that there is a PersistentVolumeClaim, which is unused. This pvc is defined in the [./k8s/items.yaml](./k8s/items.yaml) file. If we inspect this file we can see that this pvc is in fact not used. It is probably the result of a copy paste error or some kind of leftover from the evolution of the project. Dangling pvcs can be very expensive. Storage is cheap, but if many pvcs claim some storage from a cloud provider and it is unused, the price can add up quickly.

The next group is the namespaces. It says that the jathan-test namespace has no pods. It is common for developers to create a test namespace which they can use as a sandbox, but often when they are finished with their work, they leave it behind and forgot to delete it. In our case the engineer John Athan probably forgot to delete his sandbox namespace.

The last group is the Pods. It has two items. The first is the pod 'my-test' in the ired-test namespace. It is a dangling pod, which means that there is no operator (eg.: a deployment or statefulset) managing its lifecycle. The seconds pod has way more problems. It is not running because the docker image of the pod could not be pulled. In this case both pods were created by a developer for testing or development purposes, but she forgot to delete them.

### Introducing basic rule enforcements

Upload and start the [$PROJECT_ROOT/main/Konstrainer-Dsl/src/main/kotlin/me/btieger/builtins/BasicEnforce.kt](../Konstrainer-Dsl/src/main/kotlin/me/btieger/builtins/BasicEnforce.kt) dsl file.

This file describes basic rules that are evaluated upon events, like deployment creation. This can be used to prevent the creation of poorly configured resources, and can enforce best practices from the beginning.

Lets deploy the new component of the company app. First, lets create a namespace for it:

```bash
cd DemoApp
kubectl apply -f k8s/ns.yaml
```

Now let's deploy the new app, but intentionally forget to switch to the newly created namespace:

```bash
# make sure you are in the default namespace
kubectl config set-context --current --namespace=default
kubectl apply -f k8s/accounting.yaml
```

You should get a warning: `Warning: You are working in the namespace: default`

If you work in the default namespace that might be a mistake, so the BasicDiagnostics warns you. Let's revert our mistake and switch to the correct namespace:

```bash
kubectl delete deployment.apps/accounting
kubectl apply -f k8s/accounting.yaml --namespace=accounting
```

### Company policies

```bash
curl localhost:8078/login -H "Content-Type: application/json" -d '{"user": "John A. Gold", "passwd": "apples"}'
```
