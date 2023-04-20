#! /bin/bash
home=$PWD
set -e -v -u -x


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
# Build & Publish Ksr-Core
#
#################################

cd $home/KonstraineR-Agent
./gradlew publishImageToLocalRegistry
version="0.0.1"


#################################
#
# Helm install Ksr-Core
#
#################################

cd $home/charts
helm upgrade konstrainr-core konstrainr-core --install
