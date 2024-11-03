package io.yanmulin.codesnippets.examples.concurrency.puzzle;

import java.util.List;

public interface PuzzleSolver<M> {
    List<M> solve();
}
