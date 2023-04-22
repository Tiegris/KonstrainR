#!/bin/bash
set -e -v -u -x

app_root="/app"
ssl_root="$app_root/tls-cert"
openssl pkcs12 -export -in $ssl_root/cert.pem -inkey $ssl_root/key.pem -out $app_root/keystore.p12 -name "AgentCert" -password pass:foobar
keytool -importkeystore -srckeystore $app_root/keystore.p12 -srcstoretype pkcs12 -destkeystore /app/keystore.jks -deststorepass foobar -srcstorepass foobar
rm $app_root/keystore.p12

exec "$@"
