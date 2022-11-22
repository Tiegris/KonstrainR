# KonstrainR

```bash
helm uninstall konstrainer
helm install konstrainer konstrainer
docker build -t tiegris/konstrainer:dev . && docker push tiegris/konstrainer:dev
```

```bash
kubectl -n demo-ns run alpine \
    --image=alpine \
    --restart=Never \
    --command -- sleep infinity
```

```bash
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name "sampleAlias" -password pass:foobar
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststorepass foobar -srcstorepass foobar
```
