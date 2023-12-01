# KonstrainR

TODO: Write a readme








## Magic commands

```PowerShell
./gradlew build publishToMavenLocal
```

```bash
kubectl -n demo-ns run alpine \
    --image=alpine \
    --restart=Never \
    --labels 'reject=true' \
    --command -- sleep infinity
kubectl -n demo-ns delete pod alpine
```

```bash
cd /pems && \
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name "sampleAlias" -password pass:foobar && \
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststorepass foobar -srcstorepass foobar && \
mkdir /agents/build/ || true && \
mv ./keystore.jks /agents/build/ && \
cd /agents
```

```bash
java -jar app.jar
keytool -list -v -keystore build/keystore.jks -storepass foobar
```
