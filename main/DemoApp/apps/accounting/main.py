from flask import Flask

app = Flask(__name__)

@app.get("/echo")
def echo():
    return "ok"

app.run(host="0.0.0.0", port=8079)

