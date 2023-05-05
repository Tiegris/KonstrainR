#! /bin/bash

export base_ul="http://${KSR_CORE_BASE_URL}:8080/api/v1/dsls"
export dsl_id=$KSR_DSL_ID
export secret=$KSR_SECRET

function try_do() {

}

function error_report() {

    exit 1
}