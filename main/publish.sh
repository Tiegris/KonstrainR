#! /bin/bash
home=$PWD
set -e -v -u -x

helm uninstall konstrainr-core
git pull

#################################
#
# Build dsl library
#
#################################

cd $home/Konstrainer-Dsl
./gradlew build publishToMavenLocal

#################################
#
# Build & Publish Ksr-Core
#
#################################

cd $home/Konstrainer-Core
./gradlew publishImageToLocalRegistry
version="0.0.2"
docker tag tiegris/konstrainer-core:snapshot "tiegris/konstrainer-core:$version"
docker push "tiegris/konstrainer-core:$version"

#################################
#
# Build & Publish Ksr-Agent
#
#################################

cd $home/KonstraineR-Agent
./gradlew publishImageToLocalRegistry
version="0.0.1"
cd docker
docker build -t tiegris/konstrainer-agent:$version .


#################################
#
# Helm install Ksr-Core
#
#################################

cd $home/charts
helm upgrade konstrainr-core konstrainr-core --install
