package io.yanmulin.codesnippets.examples.concurrency.puzzle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequentialPuzzleSolver<P, M> implements PuzzleSolver<M> {
    private final Puzzle<P, M> puzzle;
    private final Set<P> seen = new HashSet<>();

    public SequentialPuzzleSolver(Puzzle<P, M> puzzle) {
        this.puzzle = puzzle;
    }

    @Override
    public List<M> solve() {
        P initial = puzzle.initialPosition();
        seen.add(initial);
        return search(new Node(initial, null, null));
    }

    private List<M> search(Node<P, M> node) {

//        System.out.println(node.pos + "," + seen);
        if (puzzle.isGoal(node.pos))
            return node.asMoveList();

        for (M move: puzzle.legalMoves(node.pos)) {
            P nextPos = puzzle.move(node.pos, move);
            if (!seen.add(nextPos)) continue;

            Node<P, M> next = new Node<>(nextPos, move, node);
            List<M> result = search(next);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String board = "2,3,1,*";
        SlidingPuzzle puzzle = new SlidingPuzzle(2, 2, board);
//        String board = "2,1,3,4,5,6,7,8,9,10,11,12,13,15,14,*";
//        SlidingPuzzle puzzle = new SlidingPuzzle(4, 4, board);
        SequentialPuzzleSolver<SlidingPuzzlePosition, SlidingPuzzleMove> solver = new SequentialPuzzleSolver<>(puzzle);
        System.out.println(solver.solve());
    }
}
