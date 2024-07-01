'''
Fastest Route to Databricks HQ
You live in San Francisco city and want to minimize your commute time to the Databricks HQ.
Given a 2D matrix of the San Francisco grid and the time as well as cost matrix of all the available transportation
modes, return the fastest mode of transportation. If there are multiple such modes then return one with the least cost.
Rules:
1. The input grid represents the city blocks, so the commuter is only allowed to travel along the horizontal and vertical axes.
Diagonal traversal is not permitted.
2. The commuter can only move to the neighboring cells with the same transportation mode.
Sample Input:
2D Grid: Legend:
|3|3|S|2|X|X|         X = Roadblock
|3|1|1|2|X|2|         S = Source
|3|1|1|2|2|2|         D = Destination
|3|1|1|1|D|3|         1 = Walk, 2 = Bike, 3 = Car, 4 = Train
|3|3|3|3|3|4|
|4|4|4|4|4|4|
Transportation Modes:        ["Walk", "Bike", "Car", "Train"]
Cost Matrix (Dollars/Block): [0,      1,      3,     2]
Time Matrix (Minutes/Block): [3,      2,      1,     1]
NOTE: In this example, we are only counting the blocks between 'S' and 'D' to compute the minimum time & dollar cost.
Sample Output: Bike
*/
/*
walk:  minTime, dollar ?
[0,2], [1,2], .... [3,4] 4 steps minTime: 4 * 3 = 12, cost: 4 * 0 = 0
bike: [0,2], [0,3], ... 4 stpes.  minTime: 4 * 2 = 8, cost: 4 * 1 =4
car:  ...
train: ...
for each transportation:
  bfs to find num of blocks
  if cannot reach to desti, ignore this type
  els:
    update minTime, minCost
o(m*n) time
o(m*n) space
   0 1 2 3 4 5
0 |3|3|S|2|X|X|
1 |3|1|1|2|X|2|
2 |3|1|1|2|2|2|
3 |3|1|1|1|D|3|
4 |3|3|3|3|3|4|
5 |4|4|4|4|4|4|
grid = {
  {'3', '3', 'S', '2', 'X', 'X'},
  {'3', '1', '1', '2', 'X', '2'},
  {'3', '1', '1', '2', '2', '2'},
  {'3', '1', '1', '1', 'D', '3'},
  {'3', '3', '3', '3', '3', '4'},
  {'4', '4', '4', '4', '4', '4'}
};
cost_matrix = {0, 1, 3, 2};
time_matrix = {3, 2, 1, 1};
'''

import unittest
import heapq
from collections import deque


def solve(grid, times, costs):
    n, m = len(grid), len(grid[0])
    src, dest = (0, 0), (0, 0)
    for i in range(n):
        for j in range(m):
            if grid[i][j] == 'S': src = (i, j)
            if grid[i][j] == 'D': dest = (i, j)
    
    dirs = ((-1, 0), (1, 0), (0, 1), (0, -1))

    def bfs(transport):
        visited = [[False for _ in range(m)] for _ in range(n)]
        dist = 0
        q = deque()
        q.append(src)
        visited[src[0]][src[1]] = True

        while len(q) > 0:
            for _ in range(len(q)):
                i, j = q.popleft()
                for di, dj in dirs:
                    ni, nj = i + di, j + dj
                    if ni < 0 or ni >= n or nj < 0 or nj >= m: continue
                    if (ni, nj) == dest: return dist + 1
                    if grid[ni][nj] != str(transport): continue
                    if visited[ni][nj]: continue
                    visited[ni][nj] = True
                    q.append((ni, nj))
            dist += 1
        return 1e20

    dists = []
    transports = range(1, len(times) + 1)
    for transport, time, cost in zip(transports, times, costs):
        dist = bfs(transport)
        dists.append((dist * time, dist * cost))
    return min(transports, key=lambda i: dists[i-1])


'''
follow-up: allow to change transportation during commute
'''
def follow_up(grid, times, costs):
    n, m = len(grid), len(grid[0])
    src, dest = (0, 0), (0, 0)
    for i in range(n):
        for j in range(m):
            if grid[i][j] == 'S': src = (i, j)
            if grid[i][j] == 'D': dest = (i, j)
    
    dirs = ((-1, 0), (1, 0), (0, 1), (0, -1))

    dists = [[(1e20, 1e20) for _ in range(m)] for _ in range(n)]
    parents = [[(-1, -1) for _ in range(m)] for _ in range(n)]
    dists[src[0]][src[1]] = (0, 0)
    pq = [(0, 0, *src)]
    while len(pq) > 0:
        t, c, i, j = heapq.heappop(pq)
        if grid[i][j] == 'D': break
        for di, dj in dirs:
            ni, nj = i + di, j + dj
            if ni < 0 or ni >= n or nj < 0 or nj >= m: continue
            if not grid[ni][nj].isdigit() and grid[ni][nj] != 'D': continue
            if grid[i][j] == 'S':
                heapq.heappush(pq, (0, 0, ni, nj))
                dists[ni][nj] = (0, 0)
                parents[ni][nj] = (i, j)
            else:
                transport = ord(grid[i][j]) - ord('1')
                nt, nc = t + times[transport], c + costs[transport]
                if (nt, nc) < dists[ni][nj]:
                    dists[ni][nj] = (nt, nc)
                    parents[ni][nj] = (i, j)
                    heapq.heappush(pq, (nt, nc, ni, nj))
    
    point = dest
    answer = [dest]
    while point != src and point != (-1, -1):
        point = parents[point[0]][point[1]]
        answer.append(point)
    
    if point == (-1, -1): return []
    else: return list(reversed(answer))


class SolutionTests(unittest.TestCase):
    def test_solution(self):
        grid = [
            ['3', '3', 'S', '2', 'X', 'X'],
            ['3', '1', '1', '2', 'X', '2'],
            ['3', '1', '1', '2', '2', '2'],
            ['3', '1', '1', '1', 'D', '3'],
            ['3', '3', '3', '3', '3', '4'],
            ['4', '4', '4', '4', '4', '4'],
        ]
        costs = [0, 1, 3, 2]
        times = [3, 2, 1, 1]
        self.assertEqual(solve(grid, times, costs), 2)
    
    def test_followup(self):
        grid = [
            ['3', '3', 'S', '2', 'X', 'X'],
            ['3', '1', '1', '2', 'X', '2'],
            ['3', '1', '1', '2', '2', '2'],
            ['3', '1', '1', '1', 'D', '3'],
            ['3', '3', '3', '3', '3', '4'],
            ['4', '4', '4', '4', '4', '4'],
        ]
        costs = [0, 1, 3, 2]
        times = [3, 2, 1, 1]
        self.assertEqual(follow_up(grid, times, costs), [(0, 2), (0, 3), (1, 3), (2, 3), (2, 4), (3, 4)])
    
    def test_followup_change_transport(self):
        grid = [
            ['3', '3', 'S', '2', 'X', 'X'],
            ['3', '1', '3', '2', 'X', '2'],
            ['3', '1', '3', '2', '2', '2'],
            ['3', '1', '3', '1', 'D', '3'],
            ['3', '3', '3', '3', '3', '4'],
            ['4', '4', '4', '4', '4', '4'],
        ]
        costs = [0, 1, 3, 2]
        times = [3, 2, 1, 1]
        self.assertEqual(follow_up(grid, times, costs), [(0, 2), (1, 2), (2, 2), (2, 3), (2, 4), (3, 4)])
    

if __name__ == '__main__':
    unittest.main()