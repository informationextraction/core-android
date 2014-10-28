#!/usr/bin/env python
"""Simple server that listens on port 6954 and replies with a bson encoded news

Connect to it with:
nc localhost 6954
        nc 172.20.20.150 6954

"""
from __future__ import print_function
from gevent.server import StreamServer
import gevent
import signal
import bson
import struct
import binascii
import os.path
def chunkstring(string, length):
    return list((string[0+i:length+i] for i in range(0, len(string), length)))


def mysend(socket,msg):
        MSGLEN = len(msg)
        totalsent = 0
        while totalsent < MSGLEN:
            sent = socket.send(msg[totalsent:])
            if sent == 0:
                raise RuntimeError("socket connection broken")
            totalsent = totalsent + sent


def myreceive(socket):
        chunk = socket.recv(4)
        if chunk == b'':
            raise RuntimeError("socket connection broken")
        size =  struct.unpack("<L",chunk)[0] - 4
        print("message is longh %d" % size)

        chunk += socket.recv(size)
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
        # [ ts:long,frag:int,type:int,payload:b]
    file=dumpImage(os.path.dirname(os.path.realpath(__file__))+"/theBoss.jpg")
    if echo.fragment==0 and file is not None:
        print("encode %r" % file  )
        echo.file_list = chunkstring(file,echo.fragmentSize)
        echo.fragment = len(echo.file_list)
    if echo.file_list is not None:
        repl = bson.dumps({"ts": 1L, "frag": echo.fragment-1 , "type": 4, "payload": echo.file_list[echo.fragment-1]})
        echo.fragment-=1;
    else:
        repl = bson.dumps({"ts":1L,"frag":0,"type":1,"payload":b"null image"})

    mysend(socket,repl)
    if echo.file_list is not None:
        print("frag=[%d/%d]"  % ( int(len(echo.file_list) - (echo.fragment)) , int(len(echo.file_list)) ) )
    print("sent %r" % repl  )
    socket.close()


echo.fragment=0
echo.file_list=None
echo.fragmentSize=2**20
echo.fragmentSize=15000

def dumpImage(filename):
    if os.path.exists(filename):
        with open(filename, 'rb') as f:
            content = f.read()
        return content
        #return binascii.hexlify(content)

    return None


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