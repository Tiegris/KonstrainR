#! /bin/bash

export base_ul="http://${KSR_CORE_BASE_URL}:8080/api/v1/dsls"
export dsl_id=$KSR_DSL_ID
export secret=$KSR_SECRET

function try_do() {
    file=/app/framework/lib/src/main/kotlin/me/btieger/DslInstance.kt
    wget "{base_url}/{dsl_id}/file" -O $file
    sed -i 's/package .*/package me.btieger' $file
    cd /app/framework
    ./gradlew jar
    wget --header="Content-type: multipart/form-data boundary=FILEUPLOAD" --post-file postfile "${base_url}/${dsl_id}/jar"
}

function error_report() {

    exit 1
}

try_do || error_report