"""
Hudson River Trading, LLC
Implement a hash table (HashMap) with the following functions in O(1):
- insert(key, value)
- delete(key)
- get(key)
- get_random_key()

The hash table must match the behavior tested in the test suite, which matches the
behavior of a regular python dictionary.
You may use any built-in Python functions and data structures. Keep in mind that all
operations must be in O(1). For example, you are not allowed to use the dict.keys()
function, or "key in list".
Assume that there will be no collisions in any of the inserts per hash table instance.
"""

import sys
import unittest
import random
from collections import defaultdict

class HashMap:
   
    def __init__(self):
        self.key_map = {}
        self.kv_list = []

    def __getitem__(self, key):
        index = self.key_map[key]
        _, value = self.kv_list[index]
        return value

    def __setitem__(self, key, val):
        self._try_remove_key(key)
        self.key_map[key] = len(self.kv_list)
        self.kv_list.append((key, val))

    def __delitem__(self, key):
        self._try_remove_key(key)

    def _try_remove_key(self, key):
        index = self.key_map.get(key)
        if index is not None:
            if len(self.kv_list) > 1:
                last_key, _ = self.kv_list[-1]
                self.kv_list[index], self.kv_list[-1] = self.kv_list[-1], self.kv_list[index]
                self.key_map[last_key] = index
            del self.key_map[key]
            self.kv_list.pop()

    def __str__(self):
        # Implement this if you'd like to print your hash table.
        pass

    def __len__(self):
        return len(self.kv_list)

    def get_random_key(self):
        index = random.randrange(0, len(self.kv_list))
        key, _ = self.kv_list[index]
        return key


# no need to modify anything below
class BasicHashMapTest(unittest.TestCase):
    def setUp(self):
        self.map = HashMap()

    def test_insert(self):
        self.map[0] = "Hudson River Trading"
        self.map[1] = "New York"

    def test_insert_get(self):
        self.map[0] = "Hudson River Trading"
        self.map[1] = "New York"
        self.assertEqual(self.map[0], "Hudson River Trading")
        self.assertEqual(self.map[1], "New York")

    def test_insert_get_delete(self):
        self.map[0] = "Hudson River Trading"
        self.map[1] = "New York"
        self.assertEqual(self.map[0], "Hudson River Trading")
        self.assertEqual(self.map[1], "New York")
        del self.map[1]
        # assert keys exist
        self.assertEqual(self.map[0], "Hudson River Trading")
        # assert delete key does not exist
        with self.assertRaises(KeyError):
            self.map[1]

    def test_full_basic(self):
        self.map[0] = "Hudson River Trading"
        self.map[1] = "New York"
        self.assertEqual(len(self.map), 2)
        self.assertEqual(self.map[0], "Hudson River Trading")
        self.assertEqual(self.map[1], "New York")
        del self.map[1]
        self.assertEqual(len(self.map), 1)
        # assert keys exist
        self.assertEqual(self.map[0], "Hudson River Trading")
        # assert delete key does not exist
        with self.assertRaises(KeyError):
            self.map[1]
        self.map[2] = "Value 1"
        self.assertEqual(len(self.map), 2)
        del self.map[2]
        self.map[3] = "Value 2"
        self.assertEqual(len(self.map), 2)
        self.assertEqual(self.map[3], "Value 2")
        with self.assertRaises(KeyError):
            self.map[2]


class RandomKeyHashMapTest(unittest.TestCase):
    def setUp(self):
        self.map = HashMap()

    def verify_random_keys(self, possible_keys):
        rands = defaultdict(int)
        for _ in range(1000000):
            rands[self.map.get_random_key()] += 1
        try:
            self.assertEqual(set(rands.keys()), set(possible_keys))
        except AssertionError:
            print("Your random function generated non-existing keys.")
            raise
        total = sum(rands.values())
        expected_probability = round(1 / float(len(possible_keys)), 2)
        for k, v in rands.items():
            probability = round(v / float(total), 2)
            try:
                self.assertEqual(probability, expected_probability)
            except AssertionError:
                print(
                    f"{k}'s probablity is {probability}. "
                    f"Expected: {expected_probability}."
                )
                raise
    
    def test_two_keys(self):
        self.map[0] = "Hudson River Trading"
        self.map[1] = "New York"
        self.verify_random_keys([0, 1])

    def test_four_keys(self):
        self.map[0] = "Hudson River Trading"
        self.map[1] = "Hudson River Trading"
        self.map[2] = "LLC"
        self.map[3] = "New York"
        self.map[4] = "NY"
        self.map[5] = "NY"
        del self.map[0]
        del self.map[5]
        self.verify_random_keys([1, 2, 3, 4])


if __name__ == "__main__":
    print("\n==== Running basic tests ====\n")
    basic_suite = unittest.TestSuite()
    basic_suite.addTest(unittest.makeSuite(BasicHashMapTest))
    basic_test = unittest.runner.TextTestRunner(verbosity=2).run(basic_suite)
    if basic_test.errors or basic_test.failures:
        # If above basic test fails, do not run next random test.
        sys.exit(1)
    print("\n==== Running random keys tests ====\n")
    random_suite = unittest.TestSuite()
    random_suite.addTest(unittest.makeSuite(RandomKeyHashMapTest))
    random_test = unittest.TextTestRunner(verbosity=2).run(random_suite)