'''
Google Virtual Onsite Round 1 - 9/18/2024
Knock-out Tournament

*input*
brackets: the order the teams are playing
P[i][j]: the game outcome if team i plays against team j, 1 - team i wins, 0 - team i loses

*output*
the team wins the championship

*constraints*
len(brackets) is the power of 2
P[i][i] = 0 and P[i][j] + P[j][i] = 1

*example*
bracket = [1, 0, 7, 3, 2, 6, 4, 5]
P = [
    [0, 1, 0, 0, 1, 0, 1, 0],
    [0, 0, 0, 1, 0, 1, 1, 0],
    [1, 1, 0, 0, 0, 1, 1, 1],
    [1, 0, 1, 0, 1, 0, 0, 0],
    [0, 1, 1, 0, 0, 1, 0, 1],
    [1, 0, 0, 1, 0, 0, 1, 0],
    [0, 0, 0, 1, 1, 0, 0, 0],
    [1, 1, 0, 1, 0, 1, 1, 0]
]
=> the wining team is 4

1
| - 0
0   |
-   |- 7
7   |  |
| - 7  |
3      |
-      |- 4
2      | 
| - 2  |
6   |  |
-   |- 4
4   |
| - 4
5

*follow up*
P[i][j]: the probability that team i wins against team j
returns an array, with each element representing the probability the team wins the championship

'''

import unittest
import math


def solve(bracket, P):
    n = len(bracket)
    playing_teams = [i for i in range(n)]

    while len(playing_teams) > 1:
        winning_teams = []
        for i in range(0, len(playing_teams), 2):
            t1, t2 = bracket[playing_teams[i]], bracket[playing_teams[i+1]]
            if P[t1][t2] == 1: winning_teams.append(playing_teams[i])
            else: winning_teams.append(playing_teams[i+1])
        playing_teams = winning_teams
    return bracket[playing_teams[0]]


def followup(bracket, P):
    n, nrounds = len(bracket), int(math.log2(len(bracket)) + 1)
    memo = [[-1] * nrounds for _ in range(n)]

    def find_opponents(team, r):
        gsize, prev_gsize = 2 ** (r + 1), 2 ** r
        gi, prev_gi = team // gsize, team // prev_gsize
        start, end = gi * gsize, (gi + 1) * gsize
        ex_start, ex_end = prev_gi * prev_gsize, (prev_gi + 1) * prev_gsize
        return [i for i in range(start, end) if not (ex_start <= i < ex_end)]

    def dfs(team, r):
        if memo[team][r] >= 0: return memo[team][r]
        if r == 0:
            memo[team][r] = 1.0
            return 1.0

        prob = 0
        for oppo in find_opponents(team, r - 1):
            t1, t2 = bracket[team], bracket[oppo]
            prob += dfs(team, r - 1) * dfs(oppo, r - 1) * P[t1][t2]

        memo[team][r] = prob
        return prob
    
    ans = [0] * n
    for i in range(n):
        ans[bracket[i]] = dfs(i, nrounds - 1)

    return ans


class SolutionTests(unittest.TestCase):
    def test_solution(self):
        self.assertEqual(solve([0], [[0]]), 0)
        self.assertEqual(solve([1, 0], [[0, 1], [0, 0]]), 0)
        bracket = [1, 0, 7, 3, 2, 6, 4, 5]
        P = [
            [0, 1, 0, 0, 1, 0, 1, 0],
            [0, 0, 0, 1, 0, 1, 1, 0],
            [1, 1, 0, 0, 0, 1, 1, 1],
            [1, 0, 1, 0, 1, 0, 0, 0],
            [0, 1, 1, 0, 0, 1, 0, 1],
            [1, 0, 0, 1, 0, 0, 1, 0],
            [0, 0, 0, 1, 1, 0, 0, 0],
            [1, 1, 0, 1, 0, 1, 1, 0]
        ]
        self.assertEqual(solve(bracket, P), 4)

    def test_followup(self):
        bracket = [0, 1, 2, 3, 4, 5, 6, 7]
        P = [
            [.0, .7, .1, .3, .6, .4, .8, .1],
            [.3, .0, .2, .7, .3, .5, .8, .1],
            [.9, .8, .0, .3, .1, .6, .8, .7],
            [.7, .3, .7, .0, .8, .2, .3, .5],
            [.4, .7, .9, .2, .0, .7, .3, .6],
            [.6, .5, .4, .8, .3, .0, .9, .1],
            [.2, .2, .2, .7, .7, .1, .0, .2],
            [.9, .9, .3, .5, .4, .9, .8, .0]
        ]
        ans = followup(bracket, P)
        self.assertAlmostEqual(ans[4], 0.1885464)
        self.assertAlmostEqual(sum(ans), 1.0)

        bracket = [1, 0, 7, 3, 2, 6, 4, 5]
        ans = followup(bracket, P)
        self.assertAlmostEqual(ans[2], 0.1248)
        self.assertAlmostEqual(sum(ans), 1.0)

if __name__ == '__main__':
    unittest.main()