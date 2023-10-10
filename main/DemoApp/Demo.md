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

Go to the main directory.

```bash
kubectl create ns konstrainer-ns
./publish.sh
kubectl port-forward service/konstrainer-core 8080:8080 -n konstrainer-ns
```

First let's run the basic diagnostics on the cluster. Go to [localhost:8080](http://localhost:8080) and upload 
