package io.yanmulin.codesnippets.examples.concurrency.puzzle;

import java.util.Set;

public interface Puzzle<P, M> {
    P initialPosition();
    boolean isGoal(P position);
    Set<M> legalMoves(P position);
    P move(P position, M move);
}
