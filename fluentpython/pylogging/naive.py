import logging
import multiprocessing
import os
import time

def log(n):
    start_at = time.time()
    print(f'start with pid {os.getpid()} at {start_at}')
    logging.basicConfig(
        level=logging.INFO, 
        filename='./my.log', 
        format='%(message)s'
    )
    for _ in range(n):
        logging.info('hello')
    
    end_at = time.time()
    print(f'end with pid {os.getpid()} at {end_at}, '
        f'elapsed {end_at - start_at} seconds')

def proc(ready, n):
    ready.wait()
    log(n)

def create_proc(n):
    ready = multiprocessing.Event()
    process = multiprocessing.Process(target=proc, args=(ready, n))
    process.start()
    return ready, process

def main():
    n = 1000000
    n_procs = 10
    
    procs = [create_proc(n) for _ in range(n_procs)]
    
    for ready, _ in procs:
        ready.set()

    for _, proc in procs:
        proc.join()
    
if __name__ == '__main__':
    main()