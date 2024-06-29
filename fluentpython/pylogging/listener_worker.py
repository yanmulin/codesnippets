import multiprocessing
import threading
import logging
import functools
from logging import handlers
from commons import create_worker


def log_listener(queue):
    logging.basicConfig(level=logging.INFO)
    logging.getLogger('worker').addHandler(
        logging.FileHandler('my.log', 'w')
    )
    while True:
        record = queue.get()
        if record is None:
            break
        logging.info('receive a log record %s from %s', record.msg, record.name)
        logger = logging.getLogger(record.name)
        logger.handle(record)

def config_logging(name, queue):
    logger = logging.getLogger(name)
    logger.addHandler(handlers.QueueHandler(queue))
    return logger

def main():
    queue = multiprocessing.Queue()
    
    listener = threading.Thread(target=log_listener, args=(queue,))
    listener.start()
    
    n_workers = 5
    configurer = functools.partial(config_logging, queue=queue)
    workers = [create_worker(id, configurer) for id in range(n_workers)]
    for worker in workers:
        worker.process.start()
    for worker in workers:
        worker.ready.set()
    
    try:
        wait_forever = multiprocessing.Event()
        wait_forever.wait()
    except KeyboardInterrupt:
        for worker in workers:
            worker.stop.set()
        for worker in workers:
            worker.process.join()
        queue.put(None)
        listener.join()

if __name__ == '__main__':
    main()