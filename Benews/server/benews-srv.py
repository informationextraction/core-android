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
import datetime
import time

def chunkstring(string, length):
    return list((string[0+i:length+i] for i in range(0, len(string), length)))

batch_file = "./batch.txt"


def extract_news_from_line(line):
#Date|Title|headline|content|type|filepath|imei|trials
    news = None
    line = line.rstrip()
    news_item = line.split("|")
    first = None

    if not line.isspace():
        for i in line:
            if i=='#' and first is None:
                printl ("skipping comment")
                return news
            elif not i.isspace():
                first = i
        if len(news_item) == 8:
            if news_item[0] is not None and news_item[0].isdigit():
                news = {'date': news_item[0], 'title': news_item[1], 'headline': news_item[2], 'content': news_item[3],
                        'type': news_item[4], 'filepath': news_item[5], 'imei': news_item[6], 'trials': news_item[7]}
                #sanity check on news
                if not news['date'] or not news['date'].isdigit():
                    printl ("Invalid date field not present or not a digit")
                    news = None
                if not news['filepath'] or not os.path.exists(news['filepath']):
                    printl ("Invalid filepath field not present or file not available")
                    news = None
    return news


def is_imei_valid(imei, client_imei):
    if imei and client_imei:
        if imei == client_imei:
            return True
        else:
            return False
    elif client_imei:
        return True
    return False


def is_ts_valid(ts, client_ts, trial, client_trial):
    if ts and client_ts is not None and ts.isdigit():
        if client_ts == int(ts):
            if trial is not None and trial.isdigit() and client_trial < int(trial):
                return True
            else:
                return False
        elif client_ts < int(ts):
            return True
    return False


def get_next_news(client_param,client_stats):
    nline = 0;
    printl("opening file %s" %batch_file)
    if os.path.exists(batch_file):
        opened = open(batch_file)
        for line in sorted(opened, key = str.lower):
            news = extract_news_from_line(line)
            if news:
                try:
                    if not is_imei_valid(news['imei'], client_param['imei']):
                        news = None
                        continue
                    if is_ts_valid(news['date'], client_param['ts'], news['trials'], client_stats['ts_trial']):
                        client_stats['ts_trial'] = int(client_stats['ts_trial']) + 1
                        printl("sending file %s" % news['filepath'])
                        break
                    else:
                        news = None
                        continue
                except:
                    news = None
                    continAu
            else:
                printl ("invalid line:\n%s" % line)
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
        printl("message is longh %d" % size)
        try:
            chunk += socket.recv(size)
            if chunk == b'':
                raise RuntimeError("socket connection broken")
        except MemoryError as e:
            printl("memory Error:%s" % e)
        return chunk




def printl(*s):
    if not echo._file_logs or echo._file_logs.closed:
        return False
    for i in s:
        echo._file_logs.write(i)
    echo._file_logs.write("\n")
    echo._file_logs.flush()
    return True


def close_log_file():
    if echo._file_logs and not echo._file_logs.closed:
        echo._file_logs.close
    return


def open_log_file(dir):
    st = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d-%H.%M')
    filename = dir+"/"+st+".log"
    print("opening file for log:%s" % filename)
    try:
        echo._file_logs = open(filename, "wa")
    except IOError as err:
        print("unable to open file :%s %s" % (filename, err))
        echo._file_logs = None
    return echo._file_logs


def save_bad_request(ip, port, data, dir):
    st = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d-%H.%M')
    filename =dir+"/"+ip+"_"+"%s" %port+st+".dump"
    printl("invalid payload passed! saving to:%s" % filename)
    try:
        file = open(filename, "wa")
    except:
        printl("unable to open file :%s" % filename)
        return False
    statinfo = os.stat(filename)
    initial_size=statinfo.st_size
    if data in None:
        file.write("no data recived")
    else:
        file.write(data)
    file.flush()
    file.close()
    statinfo = os.stat(filename)
    return initial_size != statinfo.st_size


# this handler will be run for each incoming connection in a dedicated greenlet
clients = {}

def echo(socket, address):
    printl('New connection from %s:%s' % address)
    (ip, port) = address

    try:
        data = myreceive(socket)
        client_param = bson.loads(data)
    except:
        save_bad_request(ip,port,data,dump_dir)
        socket.close
        return



    #{u'imei': 1, u'ts': 23}
    #add new clients to clients list
    incoming_imei = "%s", client_param['imei']
    if not clients.has_key(client_param['imei']):
        printl("adding new client ", client_param['imei'])
        clients[client_param['imei']] = {'lts': 0, 'ltf': 0, 'ts_trial': 0}
    else:
        printl("client already present", client_param['imei'])

    printl("read: %s" % client_param)
    if not client_param:
        printl("client disconnected")
        return
        # [ ts:long,frag:int,type:int,payload:b]


    #check if there are more fragment to sent
    #if (clients[client_param['imei']]['lts'] == client_param['ts'] and clients[client_param['imei']]['ltf'] != 0) and  next_news:
    next_news=echo.next_news
    if echo.fragment == 0:
        echo.next_news = get_next_news(client_param, clients[client_param['imei']])
        next_news = echo.next_news
        if next_news:
            echo.file_list = None
            printl ("ready to sent another news")
            clients[client_param['imei']]['lts'] = next_news['date']
            if os.path.isabs(next_news['filepath']):
                file=dumpImage(next_news['filepath'])
            else:
                file=dumpImage(os.path.dirname(os.path.realpath(__file__))+"/"+next_news['filepath'])
            if file:
                printl("image is valid")
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
            printl("frag=[%d/%d] frag=%d"  % ( int(len(echo.file_list) - (echo.fragment)) , int(len(echo.file_list)), echo.fragment) )

    #else:
    #    repl = bson.dumps({"ts":-1L,"frag":0,"type":1,"payload":b"null image"})


    socket.close()


echo.fragment=0
echo.file_list=None
echo.fragmentSize=2**20
echo.fragmentSize=100000
echo.next_news=None
echo._file_logs = None

def dumpImage(filename):
    if os.path.exists(filename):
        with open(filename, 'rb') as f:
            content = f.read()
        return content
        #return binascii.hexlify(content)

    return None


def check_dir(dir):
    if not os.path.exists(dir):
        d = os.mkdir(dir)
    if os.path.exists(dir):
        return True
    return False

dump_dir = "./dumps"
log_dir = "./logs"


if __name__ == '__main__':
    if not check_dir(dump_dir):
        print ("unable to create %s" % dump_dir)
        exit
    if not check_dir(log_dir):
        print ("unable to create %s" % log_dir)
        exit
    if not open_log_file(log_dir):
        exit
    bson.patch_socket()
    # to make the server use SSL, pass certfile and keyfile arguments to the constructor
    server = StreamServer(('0.0.0.0', 8080), echo)
    # to start the server asynchronously, use its start() method;
    # we use blocking serve_forever() here because we have no other jobs
    print('Starting benews server on port 8080')
    printl('Starting benews server on port 8080')
    #gevent.signal(signal.SIGTERM, server.close)
    #gevent.signal(signal.SIGINT, server.close)
    if len(sys.argv) > 1:
        print('arg passed for batch file %s' % sys.argv[1])
        printl('arg passed for batch file %s' % sys.argv[1])
        batch_file = sys.argv[1]

    server.serve_forever()