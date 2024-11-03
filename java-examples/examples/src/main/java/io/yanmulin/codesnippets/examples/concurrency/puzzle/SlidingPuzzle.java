package io.yanmulin.codesnippets.examples.concurrency.puzzle;

import java.util.HashSet;
import java.util.Set;

public class SlidingPuzzle implements Puzzle<SlidingPuzzlePosition, SlidingPuzzleMove> {

    final int n, m;
    final SlidingPuzzlePosition goal;
    final SlidingPuzzlePosition initialPosition;

    public SlidingPuzzle(int n, int m, String board) {
        this.n = n;
        this.m = m;
        this.initialPosition = new SlidingPuzzlePosition(board);

        StringBuilder goalState = new StringBuilder();
        for (int i=1;i<n*m;i++) {
            if (i > 1) goalState.append(",");
            goalState.append(i);
        }
        goalState.append(",");
        goalState.append("*");
        this.goal = new SlidingPuzzlePosition(goalState.toString());
    }

    @Override
    public SlidingPuzzlePosition initialPosition() {
        return initialPosition;
    }

    @Override
    public boolean isGoal(SlidingPuzzlePosition position) {
        return goal.equals(position);
    }

    @Override
    public Set<SlidingPuzzleMove> legalMoves(SlidingPuzzlePosition position) {
        String[] comps = position.state.split(",");
        int k;
        for (k=0;k<comps.length;k++) {
            if ("*".equals(comps[k])) break;
        }

        int i = k / n, j = k % n;
        Set<SlidingPuzzleMove> moves = new HashSet<>();
        if (i < n - 1) moves.add(new SlidingPuzzleMove(i, j, 1, 0));
        if (i > 0) moves.add(new SlidingPuzzleMove(i, j, -1, 0));
        if (j < m - 1) moves.add(new SlidingPuzzleMove(i, j, 0, 1));
        if (j > 0) moves.add(new SlidingPuzzleMove(i, j, 0, -1));
        return moves;
    }

    @Override
    public SlidingPuzzlePosition move(SlidingPuzzlePosition position, SlidingPuzzleMove move) {
        String[] comps = position.state.split(",");
        int k;
        for (k=0;k<comps.length;k++) {
            if ("*".equals(comps[k])) break;
        }

        int i = move.i, j = move.j;
        int ni = move.i + move.di, nj = move.j + move.dj;
        String tmp = comps[i * n + j];
        comps[i * n + j] = comps[ni * n + nj];
        comps[ni * n + nj] = tmp;

        return new SlidingPuzzlePosition(String.join(",", comps));
    }
}
