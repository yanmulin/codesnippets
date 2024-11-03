package io.yanmulin.codesnippets.examples.concurrency.puzzle;

public class SlidingPuzzleMove {
    final int i, j, di, dj;

    public SlidingPuzzleMove(int i, int j, int di, int dj) {
        this.i = i;
        this.j = j;
        this.di = di;
        this.dj = dj;
    }

    @Override
    public String toString() {
        if (di != 0 && dj == 0) {
            return String.format("%s(i=%d->%d, j=%d)",
                    getClass().getSimpleName(), i, i + di, j);
        } else if (di == 0 && dj != 0) {
            return String.format("%s(i=%d, j=%d->%d)",
                    getClass().getSimpleName(), i, j, j + dj);
        } else if (di != 0 && dj != 0) {
            return String.format("%s(i=%d->%d, j=%d->%d)",
                    getClass().getSimpleName(), i, i + di, j, j + dj);
        } else {
            return String.format("%s(i=%d, j=%d)",
                    getClass().getSimpleName(), i, j);
        }
    }
}
