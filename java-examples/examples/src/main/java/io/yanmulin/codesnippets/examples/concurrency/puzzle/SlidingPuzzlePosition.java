package io.yanmulin.codesnippets.examples.concurrency.puzzle;

public class SlidingPuzzlePosition {
    final String state;

    public SlidingPuzzlePosition(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof SlidingPuzzlePosition) {
            SlidingPuzzlePosition anotherPos = (SlidingPuzzlePosition) obj;
            return state.equals(anotherPos.state);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public String toString() {
        return "SlidingPuzzlePosition('" + state + "')";
    }
}
