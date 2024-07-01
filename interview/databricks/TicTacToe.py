import unittest

# https://www.1point3acres.com/bbs/thread-1026916-1-1.html
class TicTacToe:
    def solve(self, board, k):
        n, m = len(board), len(board[0])
        visited = [[False for _ in range(m)] for _ in range(n)]
        dirs = ((1, 0), (0, 1), (1, 1), (1, -1))

        def dfs(i, j, di, dj, cnt):
            visited[i][j] = True
            if cnt == k: return True
            
            if 0 <= i + di < n and 0 <= j + dj < m:
                ni, nj = i + di, j + dj
                if not visited[ni][nj] and board[ni][nj] == board[i][j] and dfs(ni, nj, di, dj, cnt + 1):
                    return True
            
            if 0 <= i - di < n and 0 <= j - dj < m:
                ni, nj = i - di, j - dj
                if not visited[ni][nj] and board[ni][nj] == board[i][j] and dfs(ni, nj, di, dj, cnt + 1):
                    return True
            return False


        for i in range(n):
            for j in range(m):
                if board[i][j] not in ('o', 'x'): continue
                for di, dj in dirs:
                    if dfs(i, j, di, dj, 1): 
                        return True 

        return False


class TicTacToeTests(unittest.TestCase):
    def test_simple_3x3(self):
        self.assertFalse(TicTacToe().solve([
            ['o', ' ', 'o'],
            ['x', 'x', ' '],
            ['x', 'o', 'x'],
        ], 3))
        self.assertTrue(TicTacToe().solve([
            ['o', ' ', 'x'],
            ['x', 'x', ' '],
            ['x', 'o', 'x'],
        ], 3))
        self.assertTrue(TicTacToe().solve([
            ['o', ' ', 'o'],
            ['x', 'x', 'o'],
            ['x', 'x', 'o'],
        ], 3))


if __name__ == '__main__':
    unittest.main()