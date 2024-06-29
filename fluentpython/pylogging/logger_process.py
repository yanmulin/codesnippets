import multiprocessing
import logging
import socketserver
import struct
import sys
import pickle
import functools
from logging import handlers
from commons import create_worker

LOGGER_HOST = 'localhost'
LOGGER_PORT = handlers.DEFAULT_TCP_LOGGING_PORT

class LogRecordStreamHandler(socketserver.StreamRequestHandler):
    
    def handle(self):
        server_logger = logging.getLogger('logger')
        server_logger.setLevel(logging.DEBUG)
        server_logger.addHandler(
            logging.StreamHandler(sys.stdout)
        )
        
        logging.getLogger('worker').addHandler(
            logging.FileHandler('my.log', 'w')
        )
        
        server_logger.info('logger start handling')
        while True:
            chunk = self.connection.recv(4)
            if len(chunk) < 4:
                break
            slen, *_ = struct.unpack('>L', chunk)
            chunk = self.connection.recv(slen)
            while len(chunk) < slen:
                chunk += self.connection.recv(slen - len(chunk))
            obj = pickle.loads(chunk)
            record = logging.makeLogRecord(obj)
            server_logger.info('receive a log record %s from %s', record.msg, record.name)
            
            logger = logging.getLogger(record.name)
            logger.handle(record)
            
def logger_process(inited):
    server = socketserver.ThreadingTCPServer(
        (LOGGER_HOST, LOGGER_PORT), LogRecordStreamHandler
    )
    inited.set()
    server.serve_forever()

def start_logger():
    inited = multiprocessing.Event()
    logger = multiprocessing.Process(target=logger_process, args=(inited,))
    logger.start()
    inited.wait()
    return logger

def config_logging(name, host, port):
    logger = logging.getLogger(name)
    handler = handlers.SocketHandler(host, port)
    logger.addHandler(handler)
    return logger

def main():
    n_workers = 5
    configurer = functools.partial(config_logging, host=LOGGER_HOST, port=LOGGER_PORT)
    logger = start_logger()
    
    workers = [create_worker(id, configurer) for id in range(n_workers)]
    
    for worker in workers:
        worker.process.start()

    try:
        wait_forever = multiprocessing.Event()
        wait_forever.wait()
    except KeyboardInterrupt:
        for worker in workers:
            worker.stop.set()
        logger.kill()

        for worker in workers:
            worker.process.join()
        logger.join()

if __name__ == '__main__':
    main()
