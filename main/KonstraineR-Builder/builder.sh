#! /bin/bash

function log() {
    echo "$1" >> $logsfile
}

export base_url="http://${KSR_CORE_BASE_URL}:8080/api/v1/dsls"
export dsl_id="${KSR_DSL_ID}"
export secret="${KSR_SECRET}"

logsfile='/app/logs'
file='/app/framework/lib/src/main/kotlin/me/btieger/DslInstance.kt'
jar='/app/framework/lib/build/libs/lib.jar'

echo "=== BEGIN ERROR REPORT ===" > $logsfile

log "BASE_URL = $base_url"
log "DSL_ID = $dsl_id"
log "SECRET = $secret"

function try_do() {
    wget "${base_url}/${dsl_id}/file" -O "${file}" && log "Download done"
    sed -i 's/package .*/package me.btieger/' "${file}" && log "Sed done"
    cd /app/framework
    ./gradlew jar >> $logsfile
    log "Compilation done"
    curl -F filename="${jar}" -F upload=@${jar} -H "Authorization: ${secret}" -H "Content-Type: application/java-archive" "${base_url}/${dsl_id}/jar"
}

function error_report() {
    log "=== END ERROR REPORT ==="
    curl -H "Authorization: ${secret}" -H "Content-Type: text/plain" -X POST --data "$(cat /app/logs)" "${base_url}/${dsl_id}/jar"
    exit 1
}

try_do || error_report