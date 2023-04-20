#!/bin/bash
set -e -v -u -x

ssl_root="/app/tls-cert"
openssl pkcs12 -export -in $ssl_root/cert.pem -inkey $ssl_root/key.pem -out $ssl_root/keystore.p12 -name "agent" -password pass:foobar
keytool -importkeystore -srckeystore $ssl_root/keystore.p12 -srcstoretype pkcs12 -destkeystore /app/keystore.jks -deststorepass foobar -srcstorepass foobar

exec "$@"
