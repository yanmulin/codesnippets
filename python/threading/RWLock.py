import unittest
from threading import Lock, Condition, local

# https://heather.cs.ucdavis.edu/matloff/public_html/158/PLN/RWLock.c
class RWLock:

    _READER = 1
    _WRITER = 1

    def __init__(self):
        self._cond = Condition(Lock())
        self._current = local()
        self._r_wait = 0
        self._r_active = 0
        self._w_wait = 0
        self._w_active = False
    
    def acquire_r(self):
        with self._cond:
            if self._w_active:
                self._r_wait += 1
                self._cond.wait_for(lambda: not self._w_active)
                self._r_wait -= 1
            self._r_active += 1
        setattr(self._current, 'acquired', self._READER)
    
    def acquire_w(self):
        with self._cond:
            if self._w_active or self._r_active > 0:
                self._w_wait += 1
                self._cond.wait_for(lambda: not self._w_active and self._r_active == 0)
                self._w_wait -= 1
            self._w_active = True
        setattr(self._current, 'acquired', self._WRITER)
    
    def _release_r(self):
        with self._cond:
            self._r_active -= 1
            if self._r_active == 0 and self._w_wait > 0:
                self._cond.notify(self._w_wait)
    
    def _release_w(self):
        with self._cond:
            self._w_active = False
            if self._r_wait > 0 or self._w_wait > 0:
                self._cond.notify(self._r_wait + self._w_wait)
    
    def release(self):
        if getattr(self._current, 'acquired') == self._READER:
            self._release_r()
        elif getattr(self._current, 'acquired') == self._WRITER:
            self._release_w()


#TODO: contextmanager    


# TODO
class RWLockTests(unittest.TestCase):
    pass
    