from flask import Flask

app = Flask(__name__)


@app.get("/")
def get():
    return {"success": True}


@app.post("/")
def post():
    return {"success": True}


@app.delete("/")
def delete():
    return {"success": True}


app.run()

