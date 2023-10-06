#!/bin/bash
set -e

# Setup
home=$(pwd)

if [ "$BUILD_IMAGES" = "1" ]; then
    # Build images
    repo=${REPO:-tiegris}
    cd $home/apps/items
    docker build -t $repo/apples-items:latest .
    docker push $repo/apples-items:latest
    cd $home/apps/users
    docker build -t $repo/apples-users:latest .
    docker push $repo/apples-users:latest
else
    echo "Skipping build images"
fi



# Initial creation of users app
kubectl create ns users || true
kubectl apply -n users -f $home/k8s/users.yaml

# Initial creation of items app
kubectl create ns items || true
kubectl apply -n items -f $home/k8s/items.yaml

# Simulate a history of changes
export v
for v in {1..10}; do
    yq eval 'select(.kind == "Deployment").spec.template.metadata.annotations.v = env(v)' $home/k8s/items.yaml -i
    kubectl apply -n items -f $home/k8s/items.yaml
done
# Restore yaml file
yq eval 'select(.kind == "Deployment").spec.template.metadata.annotations.v = "0"' $home/k8s/items.yaml -i

# Simulate John Athan creating test namespace
kubectl create ns jathan-test || true
# Simulate Ida Red creating test namespace and leaving some leftover junk in it
kubectl create ns ired-test || true
kubectl apply -n ired-test -f $home/k8s/junk.yaml

# install mongodb
# https://bitnami.com/stack/mongodb/helm
kubectl create ns mongo
helm install -n mongo my-release oci://registry-1.docker.io/bitnamicharts/mongodb

export MONGODB_ROOT_PASSWORD=$(kubectl get secret --namespace mongo my-release-mongodb -o jsonpath="{.data.mongodb-root-password}" | base64 -d)
export MONGO_HOST="my-release-mongodb.mongo.svc.cluster.local"

yq eval 'select(.kind == "Deployment").spec.template.spec.containers[0].env[0].value = env(MONGODB_ROOT_PASSWORD)' $home/k8s/items.yaml -i
yq eval 'select(.kind == "Deployment").spec.template.spec.containers[0].env[1].value = env(MONGO_HOST)' $home/k8s/items.yaml -i
kubectl apply -n items -f $home/k8s/items.yaml
