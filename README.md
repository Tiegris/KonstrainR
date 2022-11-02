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
