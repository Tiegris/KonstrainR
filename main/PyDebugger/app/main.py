import util
from kubernetes import client, config
from kubernetes.client.models.admissionregistration_v1_service_reference import AdmissionregistrationV1ServiceReference
from kubernetes.client.models.admissionregistration_v1_webhook_client_config import AdmissionregistrationV1WebhookClientConfig
import base64
import json


from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/inject", methods=['POST'])
def webhook():
    content = request.json
    print("got it")


    return jsonify(
        {
            'apiVersion': content['apiVersion'],
            'kind': content['kind'],
            'response': {
                'uid': content['request']['uid'],
                'allowed': True,
                "status": {"message": "Alma alma?"},
                "patchType": "JSONPatch",
                "patch": base64.b64encode(json.dumps([
                    {
                        'op': 'add',
                        'path': '/metadata/labels/alma',
                        'value': content['request']['name']
                    },
                    ]).encode('UTF-8')
                ).decode('UTF-8')
            }
        }
    )


if __name__ == "__main__":
    cert, certKey = util.read_pems()
    config.load_incluster_config()
    app.run(host="0.0.0.0", ssl_context=('/pems/cert.pem', '/pems/key.pem'), port=8443)
