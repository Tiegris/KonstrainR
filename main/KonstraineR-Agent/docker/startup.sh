#!/bin/bash
set -e -v -u -x

app_root="/app"
ssl_root="$app_root/tls-cert"

cp "$ssl_root/rootCa.pem" /usr/local/share/ca-certificates
update-ca-certificates

openssl pkcs12 -export -in $ssl_root/cert.pem -inkey $ssl_root/key.pem -out $app_root/keystore.p12 -name "AgentCert" -password pass:foobar
keytool -importkeystore -srckeystore $app_root/keystore.p12 -srcstoretype pkcs12 -destkeystore /app/keystore.jks -deststorepass foobar -srcstorepass foobar
rm $app_root/keystore.p12

wget "http://${KSR_CORE_BASE_URL}:8080/api/v1/dsls/${KSR_DSL_ID}/jar" -O "/app/libs/agentdsl.jar"
echo ":/app/libs/agentdsl.jar" >> "/app/jib-classpath-file"

exec "$@"
