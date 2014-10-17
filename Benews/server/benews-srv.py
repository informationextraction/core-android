#!/usr/bin/env python
"""Simple server that listens on port 6954 and replies with a bson encoded news

Connect to it with:
  nc localhost 6954

"""
from __future__ import print_function
from gevent.server import StreamServer
import gevent
import signal
import bson

def mysend(socket,msg):
        MSGLEN = len(msg)
        totalsent = 0
        while totalsent < MSGLEN:
            sent = socket.send(msg[totalsent:])
            if sent == 0:
                raise RuntimeError("socket connection broken")
            totalsent = totalsent + sent

def myreceive(socket):
        chunk = socket.recv(2048)
        if chunk == b'':
            raise RuntimeError("socket connection broken")
        return chunk

# this handler will be run for each incoming connection in a dedicated greenlet
def echo(socket, address):
    print('New connection from %s:%s' % address)
    
    data = myreceive(socket)
    line = bson.loads(data)
    print("read: ", line)
    if not line:
        print("client disconnected")
        return
    
    repl = bson.dumps({"cmd":"save","type":1,"payload":b"ciao mondo"})
    mysend(socket,repl)
    print("sent %r" % repl)
    socket.close()


if __name__ == '__main__':
    bson.patch_socket()
    # to make the server use SSL, pass certfile and keyfile arguments to the constructor
    server = StreamServer(('0.0.0.0', 6954), echo)
    # to start the server asynchronously, use its start() method;
    # we use blocking serve_forever() here because we have no other jobs
    print('Starting benews server on port 6954')
    #gevent.signal(signal.SIGTERM, server.close)
    #gevent.signal(signal.SIGINT, server.close)
    server.serve_forever()