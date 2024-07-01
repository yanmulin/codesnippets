from threading import Thread
from queue import SimpleQueue

class DataWriterDaemon(Thread):
    def __init__(self, filepath, buffer):
        self.buffer = buffer
        self.fp = open(filepath, 'a+')

    def run(self):
        while True:
            b = self.buffer.get()
            self.fp.write(b)

# https://www.1point3acres.com/bbs/thread-1066176-1-1.html
class DataWriter:
    def __init__(self, filepath):
        self.filepath = filepath
        self.buffer = SimpleQueue()
        self.daemon = Thread()
    
    def push(self, bytes):
        self.buffer.put(bytes)
