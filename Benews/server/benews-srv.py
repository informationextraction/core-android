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
import sys

def chunkstring(string, length):
    return list((string[0+i:length+i] for i in range(0, len(string), length)))

batch_file = "./batch.txt"


def get_next_news(ts):
    nline=0;
    news=None
    print("opening file %s" %batch_file)
    if os.path.exists(batch_file):
        opened = open(batch_file)
        for line in sorted(opened, key = str.lower):
            line = line.rstrip()
            news_item = line.split("|")
            if news_item[0] == 'Date':
                continue
            #Date|Title|headline|content|type|filepath
            if len(news_item) == 6:
                if ts < int(news_item[0]):
                    print("sending file %s" %news_item[5])
                    news={'date': news_item[0], 'title': news_item[1], 'headline': news_item[2], 'content': news_item[3], 'type': news_item[4],'filepath': news_item[5]}
                    break
    return news


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

clients = {}

def echo(socket, address):
    print('New connection from %s:%s' % address)
    
    data = myreceive(socket)
    client_param = bson.loads(data)
    #{u'imei': 1, u'ts': 23}
    #add new clients to clients list
    incoming_imei = "%d", int(client_param['imei'])
    if not clients.has_key(client_param['imei']):
        print("adding new client ", client_param['imei'])
        clients[client_param['imei']] = {'lts': 0, 'ltf': 0}
    else:
        print("client already present", client_param['imei'])

    print("read: ", client_param)
    if not client_param:
        print("client disconnected")
        return
        # [ ts:long,frag:int,type:int,payload:b]

    next_news = get_next_news(client_param['ts'])
    #check if there are more fragment to sent
    #if (clients[client_param['imei']]['lts'] == client_param['ts'] and clients[client_param['imei']]['ltf'] != 0) and  next_news:
    if clients[client_param['imei']]['ltf'] == 0 and next_news:
        print ("ready to sent another news")
        clients[client_param['imei']]['lts'] = next_news['date']
        if os.path.isabs(next_news['filepath']):
            file=dumpImage(next_news['filepath'])
        else:
            file=dumpImage(os.path.dirname(os.path.realpath(__file__))+"/"+next_news['filepath'])
        if file:
            print("image is valid")
            echo.file_list = chunkstring(file,echo.fragmentSize)
            echo.fragment = len(echo.file_list)
            clients[client_param['imei']]['ltf'] = echo.fragment

    if echo.file_list is not None and next_news:
        repl = bson.dumps({"ts":long(next_news['date']) , "frag": echo.fragment-1 , "type": int(next_news['type']),
                           "headline": next_news['headline'], "payload": echo.file_list[echo.fragment-1],
                           "content": next_news['content'], "title": next_news['title']
        })
        echo.fragment-=1;
        clients[client_param['imei']]['ltf'] = echo.fragment
        mysend(socket,repl)
        if echo.file_list is not None:
            print("frag=[%d/%d]"  % ( int(len(echo.file_list) - (echo.fragment)) , int(len(echo.file_list)) ) )
        print("sent %r" % repl  )
    #else:
    #    repl = bson.dumps({"ts":-1L,"frag":0,"type":1,"payload":b"null image"})


    socket.close()


echo.fragment=0
echo.file_list=None
echo.fragmentSize=2**20
echo.fragmentSize=100000

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
    server = StreamServer(('0.0.0.0', 8080), echo)
    # to start the server asynchronously, use its start() method;
    # we use blocking serve_forever() here because we have no other jobs
    print('Starting benews server on port 8080')
    #gevent.signal(signal.SIGTERM, server.close)
    #gevent.signal(signal.SIGINT, server.close)
    if len(sys.argv) > 1:
        print('arg passed for batch file %s' %sys.argv[1])
        batch_file = sys.argv[1]

    server.serve_forever()