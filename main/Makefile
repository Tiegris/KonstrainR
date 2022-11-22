all: helmUninstall dockerBuild dockerPush helmInstall

helmUninstall:
	helm uninstall -n konstrainer  konstrainer || true

helmInstall:
	helm install -n konstrainer konstrainer charts/konstrainer

dockerPush:
	docker push "tiegris/konstrainer:dev"

dockerBuild:
	docker build -t "tiegris/konstrainer:dev" -f ./PyDebugger/Dockerfile ./PyDebugger

test:
	testTeardown testRun

testRun:
	kubectl apply -n demo-ns -f test/deployment.yaml

testTeardown:
	kubectl delete -n demo-ns deployment.apps/demo || true