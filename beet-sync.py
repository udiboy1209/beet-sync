from flask import Flask, send_from_directory, \
                  appcontext_pushed, appcontext_tearing_down
import sqlite3
import json
import os
from netifaces import ifaddresses, AF_INET
from zeroconf import Zeroconf,ServiceInfo
import socket

wifi_ip_obj = ifaddresses('wlp3s0')
wifi_ip = wifi_ip_obj[AF_INET][0]['addr']

desc = {'host':socket.gethostname()}
info = ServiceInfo("_beetsync._tcp.local.",
                   "%s._beetsync._tcp.local." % socket.gethostname(),
                   socket.inet_aton(wifi_ip), 5000, 0, 0,
                   desc)

app = Flask(__name__)
music_folder = os.path.expanduser('~/Music')

conn = None

def start_app(sender, **extra):
    global conn
    conn = sqlite3.connect(os.path.expanduser('~/.config/beets/library.db'))

def stop_app(sender, **extra):
    global conn
    conn.close()

appcontext_pushed.connect(start_app, app)
appcontext_tearing_down.connect(stop_app, app)

@app.route('/')
def index():
    return 'Welcome to Beet-Sync!'

@app.route('/albums')
def albums():
    cursor = conn.cursor()
    cursor.execute('SELECT album,albumartist FROM albums ORDER BY albumartist;')
    ret_obj = []
    for row in cursor:
        ret_row = {'name': str(row[0]), 'artist': str(row[1])}
        ret_obj.append(ret_row)

    cursor.execute('SELECT artist FROM items WHERE album = ? '\
                   'GROUP BY artist ORDER BY artist', ("",));

    for row in cursor:
        ret_row = {'name': 'Singles', 'artist': str(row[0])}
        ret_obj.append(ret_row)

    ret_obj = sorted(ret_obj, key=lambda x: x['artist']);

    return json.dumps(ret_obj)

@app.route('/album/<artist>/<album>')
def songs(artist,album):
    if album == "Singles":
        album = ""
    params = (artist,album,)
    cursor = conn.cursor()
    cursor.execute('SELECT title,path,artist,album,id FROM items '\
                   'WHERE artist=? AND album=? ORDER BY track;', params)
    ret_obj = []
    for row in cursor:
        path = str(row[1])
        i = path.index('Music/')
        link = path[i+6:-1]
        ret_row = {'name': str(row[0]),
                   'link': link,
                   'artist':str(row[2]),
                   'album':str(row[3]),
                   'id': str(row[4]),}
        ret_obj.append(ret_row)

    return json.dumps(ret_obj)

@app.route('/song/<int:id>')
def song(id):
    params = (id,)

    cursor = conn.cursor()
    cursor.execute('SELECT title,path,artist,album FROM items '\
                   'WHERE id=?;', params)

    row = cursor.fetchone();
    if row != None:
        path = str(row[1])
        i = path.index('Music/')
        link = '/download/%s' % path[i+6:]
        ret_row = {'name': str(row[0]),
                   'link': link,
                   'artist':str(row[2]),
                   'album':str(row[3]),}
    else:
        ret_row = {}

    return json.dumps(ret_row)

@app.route('/download/<path:filename>')
def download(filename):
    return send_from_directory(directory=music_folder,filename=filename)


if __name__ == '__main__':
    server = Zeroconf()
    server.register_service(info)

    try:
        app.run(host='0.0.0.0', threaded=True)
    finally:
        server.unregister_service(info)
        server.close()
