function watch_k8s() {
    watch "kubectl get pods,services,deployments,jobs,secrets,mutatingwebhookconfigurations" -n konstrainer-ns
}

function fwd_core() {
    clear && k port-forward service/konstrainr-core 8080:8080 -n konstrainer-ns
}

function fwd_agent() {
    clear && k port-forward service/example-server 8443:443 -n konstrainer-ns
}

function logs_core() {
    clear && k logs -f service/konstrainr-core -n konstrainer-ns
}

function logs_builder() {
    clear && k logs -f job.batch/dsl-build-job-$1 -n konstrainer-ns
}

function logs_agent() {
    clear && k logs -f service/example-server -n konstrainer-ns
}
