import logging
import time
import os
import multiprocessing
from random import choice
from typing import NamedTuple

LOG_LEVELS = (logging.DEBUG, logging.INFO, logging.WARNING, logging.ERROR, logging.CRITICAL)
LOG_MESSAGES = (
    'Random message #1',
    'Random message #2',
    'Random message #3',
)

def message():
    return choice(LOG_MESSAGES)

def level():
    return choice(LOG_LEVELS)

class Worker(NamedTuple):
    stop: multiprocessing.Event
    process: multiprocessing.Process

def worker_process(id, configurer, stop):
    logger = configurer('worker')
    logger.info('worker(pid %s) started.', os.getpid())
    while not stop.is_set():
        logger.log(level(), f'worker-{id}: {message()}')
        time.sleep(0.2)
    logger.info('worker(pid %s) exited.', os.getpid())
    
def create_worker(id, configurer):
    stop = multiprocessing.Event()
    process = multiprocessing.Process(target=worker_process, args=(id, configurer, stop))
    return Worker(stop, process)