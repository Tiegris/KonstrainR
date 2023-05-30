#! /bin/bash

function log() {
    echo "$1" >> $logsfile
}

base_url="http://${KSR_CORE_BASE_URL}:8080/api/v1/dsls"
dsl_id="${KSR_DSL_ID}"
secret="${KSR_SECRET}"

logsfile='/app/logs'
file='/app/framework/lib/src/main/kotlin/me/btieger/DslInstance.kt'
jar='/app/framework/lib/build/libs/lib.jar'

echo "=== BEGIN ERROR REPORT ===" > $logsfile

log "BASE_URL = $base_url"
log "DSL_ID = $dsl_id"

function try_do() {
    wget "${base_url}/${dsl_id}/file" -O "${file}" && log "Download done" || (log "Download failed" && error_report)
    sed -i 's/package .*/package me.btieger/' "${file}" && log "Sed done" || (log "Sed failed" && error_report)
    cd /app/framework
    ./gradlew jar >> $logsfile && log "Build done" || (log "Build failed" && error_report)
    log "Compilation done"
    curl -F filename="${jar}" -F upload=@${jar} -H "Authorization: ${secret}" -H "Content-Type: application/java-archive" "${base_url}/${dsl_id}/jar" || (log "Upload failed" && error_report)
}

function error_report() {
    log "=== END ERROR REPORT ==="
    curl -H "Authorization: ${secret}" -H "Content-Type: text/plain" -X POST --data "$(cat /app/logs)" "${base_url}/${dsl_id}/jar"
    exit 1
}

try_do
