from flask import Flask, request
from pymongo import MongoClient
import os

mongo_pass = os.getenv("MongoPass", "0CGdI215Iz")
mongo_url = os.getenv("MongoUrl", f"mongodb://root:{mongo_pass}@localhost:27017")
mongo = MongoClient(mongo_url)

app = Flask(__name__)

def convert(x):
    return {'id': str(x['_id']), 'name': x['name'], 'color': x['color'], 'category': x['category'], 'price': x['price']}


@app.get("/items")
def get_all():
    collection = mongo.applesapp.items
    return [convert(x) for x in collection.find()]


# Get a particular user
@app.get("/items/<name>")
def get(name):
    collection = mongo.applesapp.items
    return convert(collection.find_one({"name": name}))


def init_db():
    items = [
        {"name": "Jonathan", "color": "red", "category": "apple", "price": "1000"},
        {"name": "Idared", "color": "red", "category": "apple", "price": "1000"},
    ]
    mongo.applesapp.items.insert_many(items)


app.run(host="0.0.0.0", port=8079)


