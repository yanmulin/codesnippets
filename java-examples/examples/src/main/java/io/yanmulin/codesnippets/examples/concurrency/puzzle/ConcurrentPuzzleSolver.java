package io.yanmulin.codesnippets.examples.concurrency.puzzle;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentPuzzleSolver<P, M> implements PuzzleSolver<M> {

    private static final int N_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    private static final int TIMEOUT = 100;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;
    final Puzzle<P, M> puzzle;
    final ConcurrentMap<P, Boolean> seen = new ConcurrentHashMap<>();
    final ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
    final CompletableFuture<List<M>> solution = new CompletableFuture<>();
    final AtomicInteger taskCount = new AtomicInteger(0);

    class SolverTask implements Runnable {
        Node<P, M> node;
        public SolverTask(Node<P, M> node) {
            this.node = node;
            taskCount.incrementAndGet();
        }

        @Override
        public void run() {
            try {
                if (solution.isCancelled() || solution.isDone()) return;
                if (seen.putIfAbsent(node.pos, true) != null) return;

                if (puzzle.isGoal(node.pos)) {
                    solution.complete(node.asMoveList());
                    return;
                }

                for (M move : puzzle.legalMoves(node.pos)) {
                    P nextPos = puzzle.move(node.pos, move);
                    Node<P, M> next = new Node<>(nextPos, move, node);
                    executor.execute(new SolverTask(next));
                }
            } finally {
                if (taskCount.decrementAndGet() == 0) {
                    solution.complete(null);
                }
            }
        }
    }

    public ConcurrentPuzzleSolver(Puzzle<P, M> puzzle) {
        this.puzzle = puzzle;
    }

    @Override
    public List<M> solve() {
        P initialPosition = puzzle.initialPosition();
        Node<P, M> node = new Node<>(initialPosition, null, null);
        executor.execute(new SolverTask(node));
        try {
            try {
                return solution.get();
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            } finally {
                executor.shutdown();
                executor.awaitTermination(TIMEOUT, UNIT);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) return (RuntimeException) t;
        else if (t instanceof Error) throw (Error) t;
        else throw new IllegalStateException("unknown throwable", t);
    }

    public static void main(String[] args) {
//        String board = "2,3,1,*";
//        String board = "*,1,2,3";
//        SlidingPuzzle puzzle = new SlidingPuzzle(2, 2, board);
        String board = "2,1,3,4,5,6,7,8,9,10,11,12,13,15,14,*";
        SlidingPuzzle puzzle = new SlidingPuzzle(4, 4, board);
        ConcurrentPuzzleSolver<SlidingPuzzlePosition, SlidingPuzzleMove> solver = new ConcurrentPuzzleSolver<>(puzzle);
        System.out.println(solver.solve());
    }
}
