# KonstrainR

```bash
kubectl -n demo-ns run alpine \
    --image=alpine \
    --restart=Never \
    --command -- sleep infinity
kubectl -n demo-ns delete pod alpine
```

```bash
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name "sampleAlias" -password pass:foobar && \
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststorepass foobar -srcstorepass foobar && \
mkdir /agents/build/ && \
mv ./keystore.jks /agents/build/ && \
cd /agents
```
