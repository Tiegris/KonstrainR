#!/bin/bash
set -e

# Helpers
kns () {
        if [ -n "${1}" ]
        then
                echo "/root/bin/kubectl config set-context --current --namespace=${1}"
                kubectl config set-context --current "--namespace=${1}"
        else
                echo "Usage: $0 [namespace]"
        fi
}

# Setup
repo=tiegris
home=$(pwd)

# Build images
cd $home/apps/items
docker build -t $repo/apples-items:latest .
docker push $repo/apples-items:latest
cd $home/apps/users
docker build -t $repo/apples-users:latest .
docker push $repo/apples-users:latest

# Initial creation of users app
kubectl create ns users
kns users
kubectl apply -f $home/k8s/users.yaml

# Initial creation of items app
kubectl create ns items
kns items
kubectl apply -f $home/k8s/items.yaml

# Simulate a history of changes
for v in {1..14}; do
    yq eval 'select(.kind == "Deployment").spec.template.metadata.annotations.v = env(v)' $home/k8s/items.yaml -i
    kubectl apply -f $home/k8s/items.yaml
done
v=0
yq eval 'select(.kind == "Deployment").spec.template.metadata.annotations.v = env(v)' $home/k8s/items.yaml -i

# Simulate collegaue 1 creating test namespace
kubectl create ns johndoe-test
# Simulate collegaue 2 creating test namespace and leaving some leftover junk in it
kubectl create ns janeroe-test
kns janeroe-test
kubectl apply -f $home/k8s/junk.yaml

kubectl create ns mongo
kns mongo

# install mongodb
# https://bitnami.com/stack/mongodb/helm
helm install my-release oci://registry-1.docker.io/bitnamicharts/mongodb