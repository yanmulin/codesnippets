import unittest


def convert_ip(ip):
    parts = ip.split('.')
    nums = [int(x) << ((3 - i) * 8) for i, x in enumerate(parts)]
    return sum(nums)


def mask(m):
    shift = 32 - int(m)
    if shift == 0: return (1 << 32) - 1
    else: return ~((1 << shift) - 1)


def solve(ip, rules):
    ip_num = convert_ip(ip)
    for rule in rules:
        rip, m = rule[0].split('/')
        rule_mask = convert_ip(rip) & mask(m)
        if rule_mask & ip_num == rule_mask:
            return rule[1]
    return None


class CIDRSolverTests(unittest.TestCase):
    def test_simple(self):
        self.assertEqual(
            solve(
                '1.2.3.4', 
                ((('1.2.3.5/32', 'DENY'), ('1.2.4.0/24', 'DENY'), ('1.2.3.1/31', 'ALLOW')))
            ),
            'ALLOW'
        )


if __name__ == '__main__':
    unittest.main()