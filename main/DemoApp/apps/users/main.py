import os
import sqlite3
from flask import Flask, request

app = Flask(__name__)
dbfile = os.getenv("DB_FILE", "users.db")


@app.get("/users")
def get():
    with sqlite3.connect(dbfile) as con:
        cur = con.cursor()
        return cur.execute("SELECT * FROM users").fetchall()


@app.post("/users")
def post():
    data = request.get_json()
    data = (data.name, data.fav_color, data.birt_year)
    with sqlite3.connect(dbfile) as con:
        cur = con.cursor()
        cur.execute("INSERT INTO users VALUES(?, ?, ?)", data)
        con.commit()


def init_db():
    with sqlite3.connect(dbfile) as con:
        cur = con.cursor()
        cur.execute("CREATE TABLE users(name, fav_color, birt_year)")
        cur.execute("""
        INSERT INTO users VALUES
            ('John Athan', 'red', 1864),
            ('Ida Red', 'red', 1935),
            ('Granny Smith', 'green', 1868)
        """)
        con.commit()


if not os.path.isfile(dbfile):
   init_db()

app.run(host="0.0.0.0", port=8078)
