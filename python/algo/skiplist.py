import random
import unittest

class SkipListNode:
    def __init__(self, key, value, level):
        self.key = key
        self.value = value
        self.forward = [None for _ in range(level)]

    @property
    def level(self):
        return len(self.forward)

    def __str__(self):
        return '%s(key=%s, value=%s, level=%d)' \
            % (self.__class__.__name__, self.key, self.value, len(self.forward))


class SkipList:
    def __init__(self, max_level, p=0.5):
        self.level = 1
        self.max_level = max_level
        self.header = SkipListNode(None, None, max_level)
        self.p = p
        self.random = random.Random()

    def search(self, key):
        p = self.header
        for i in reversed(range(self.level)):
            while self._greater_than(key, p.forward[i]):
                p = p.forward[i]
        p = p.forward[0]

        if p != None and p.key == key:
            return p.value
        else: return None

    def insert(self, key, value):
        p = self.header
        update = [None for _ in range(self.max_level)]

        for i in reversed(range(self.level)):
            while self._greater_than(key, p.forward[i]):
                p = p.forward[i]
            update[i] = p
        p = p.forward[0]
        
        if p != None and p.key == key:
            p.value = value
        else:
            level = self._random_level()
            while level > self.level:
                update[self.level] = self.header
                self.level += 1

            node = SkipListNode(key, value, level)
            for i in range(node.level):
                node.forward[i] = update[i].forward[i]
                update[i].forward[i] = node
    
    def delete(self, key):
        p = self.header
        update = [None for _ in range(self.level)]
        
        for i in reversed(range(self.level)):
            while self._greater_than(key, p.forward[i]):
                p = p.forward[i]
            update[i] = p

        p = p.forward[i]
        if p != None and p.key == key:
            for i in range(p.level):
                update[i].forward[i] = p.forward[i]
            
            while self.level > 1 and self.header.forward[self.level-1] == None:
                self.level -= 1
            return True
        else: return False

    def _greater_than(self, key, node):
        if node == None: return False
        else: return key > node.key
    
    def _random_level(self):
        level = 1
        while self.random.uniform(0, 1) < self.p:
            level += 1
        
        return min(self.max_level, level)
    
    def __str__(self):
        nodes = []
        p = self.header.forward[0]
        while p != None:
            nodes.append(p)
            p = p.forward[0]
        return '%s(max_level=%s, p=%f, nodes=(%s))' \
            % (
                self.__class__.__name__, 
                self.max_level, 
                self.p, 
               ', '.join(str(n) for n in nodes)
            ) 


class SkipListBuilder:
    def __init__(self, max_level):
        self.list = SkipList(max_level)

    def append_node(self, key, level=1):
        if level > self.list.max_level:
            raise ValueError('node level(%d) exceeds list\'s max level(%d)' 
                             % (level, self.list.max_level))
        node = SkipListNode(key, key, level)
        
        p = self.list.header
        for i in reversed(range(level)):
            while p.forward[i] != None:
                p = p.forward[i]
            p.forward[i] = node
        
        if p != self.list.header and p.key > key:
            raise ValueError('node key(%d) smaller than last key(%d)' 
                             % (key, p.key))
        
        self.list.level = max(self.list.level, level)
        return self

    def build(self):
        return self.list
    

class SkipListTests(unittest.TestCase):

    def sanity_check(self, skiplist):
        keys = []
        p = skiplist.header.forward[0]
        while p != None:
            keys.append(p.key)
            p = p.forward[0]

        p_idx = 0
        p = skiplist.header.forward[0]
        while p != None:
            v = skiplist.header
            v_idx = -1
            for i in reversed(range(p.level)):
                while v != None and v.forward[i] != p:
                    v = v.forward[i]
                    v_idx += 1
                
                # test v is a valid node before p
                self.assertIsNotNone(v)

                # test orderness
                if v != skiplist.header:
                    self.assertLessEqual(v.key, p.key)

                # test any node between v_idx and p_idx has level < i + 1
                w = v.forward[0]
                while w != p:
                    self.assertLess(w.level, i + 1, 
                                    'expect %s.forward[%d]=%s, but actual is %s, the list is %s' % 
                                    (w, i, p, v, skiplist))
                    w = w.forward[0]
                
            p_idx += 1
            p = p.forward[0]

    def test_simple(self):
        sl = SkipListBuilder(4)\
            .append_node(3, 1).append_node(6, 4)\
            .append_node(7, 1).append_node(9, 2)\
            .append_node(12, 1).append_node(17, 2)\
            .append_node(19, 1).append_node(21, 1)\
            .append_node(25, 4).append_node(26, 2)\
            .build()        
        # search
        self.assertEqual(sl.search(19), 19)
        self.assertEqual(sl.search(17), 17)
        self.assertEqual(sl.search(13), None)
        self.assertEqual(sl.search(8), None)
        self.assertEqual(sl.search(25), 25)

        # insert
        sl.insert(19, 15)
        self.sanity_check(sl)
        self.assertEqual(sl.search(19), 15)
        sl.insert(8, 8)
        self.sanity_check(sl)
        self.assertEqual(sl.search(8), 8)
        sl.insert(11, 11)
        self.sanity_check(sl)
        self.assertEqual(sl.search(11), 11)

        # delete
        sl.delete(19)
        self.sanity_check(sl)
        self.assertEqual(sl.search(19), None)
        sl.delete(25)
        self.sanity_check(sl)
        self.assertEqual(sl.search(25), None)
        sl.delete(17)
        self.sanity_check(sl)
        self.assertEqual(sl.search(17), None)

    def test_random_dataset(self):
        sl = SkipList(max_level=32)

        def shuffled_range(n):
            x = [i for i in range(n)]
            random.shuffle(x)
            return x
        
        for i in shuffled_range(1024):
            sl.insert(i, i)
        for i in shuffled_range(1024):
            self.assertEqual(sl.search(i), i)
        for i in shuffled_range(1024):
            sl.delete(i)
        for i in shuffled_range(1024):
            self.assertEqual(sl.search(i), None)



if __name__ == '__main__':
    unittest.main()