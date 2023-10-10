import os
import sqlite3
from flask import Flask, request
import logging

app = Flask(__name__)
dbfile = os.getenv("DB_FILE", "users.db")


@app.get("/users")
def get():
    with sqlite3.connect(dbfile) as con:
        cur = con.cursor()
        return cur.execute("SELECT * FROM users").fetchall()


@app.post("/login")
def post():
    json = request.get_json()
    data = (json["user"], json["passwd"])    
    with sqlite3.connect(dbfile) as con:
        cur = con.cursor()
        success = len(cur.execute(
            "SELECT * FROM users WHERE name = ? AND passwd = ?", data
        ).fetchall()) > 0
        if success:
            return "Logged in"
        else:
            logging.warning(f"Invalid login atempt: {json}")
            return "Invalid pass"
            


def init_db():
    with sqlite3.connect(dbfile) as con:
        cur = con.cursor()
        cur.execute("CREATE TABLE users(name, passwd, fav_color, birt_year)")
        cur.execute(
            """
        INSERT INTO users VALUES
            ('John Athan', 'password', 'red', 1864),
            ('Ida Red', '1234', 'red', 1935),
            ('Granny Smith', 'ledig', 'green', 1868),
            ('John A. Gold', 'apples', 'red', 1943)
        """
        )
        con.commit()


if not os.path.isfile(dbfile):
    init_db()

app.run(host="0.0.0.0", port=8078)
