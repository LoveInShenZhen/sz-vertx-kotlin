#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import multiprocessing as mp
import socketserver
import sys
from http.server import HTTPServer
from http.server import SimpleHTTPRequestHandler


class ThreadingHTTPServer(socketserver.ThreadingMixIn, HTTPServer):
    daemon_threads = True


def run():
    if len(sys.argv) == 1:
        port = 10086
    else:
        port = int(sys.argv[1])

    server_address = ('', port)
    http_server = ThreadingHTTPServer(server_address = server_address, RequestHandlerClass = SimpleHTTPRequestHandler)
    try:
        print("starting http server on port: %s" % port)
        http_server.serve_forever()
    except KeyboardInterrupt:
        http_server.stop()

if __name__ == '__main__':
    print("sys.argv len: %s" % len(sys.argv))
    run()
