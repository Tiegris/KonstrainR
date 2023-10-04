from flask import Flask, request
from pymongo import MongoClient
import os

mongo_url = os.getenv('MongoUrl', 'mongodb://mongodb:27017')
mongo = MongoClient(mongo_url)

app = Flask(__name__)


@app.get("/items")
def get_all():
    collection = mongo.todoapp.users
    output = []
    for u in collection.find():
        output.append({'name': u['name'], 'id': u['id']})
    return output


# Get a particular user
@app.get("/items/<int:id>")
def get(id):
    collection = mongo.todoapp.users
    u = collection.find_one({'id': id})
    if u:
        output = {'name': u['name'], 'id': u['id']}
        return output
    else:
        return {'error': 'Not found'}, 404


app.run()
