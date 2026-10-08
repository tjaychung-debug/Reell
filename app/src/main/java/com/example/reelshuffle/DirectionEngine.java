package com.example.reelshuffle;

import java.util.ArrayList;
import java.util.Random;

/** One randomly selected "next" gesture per visited reel. Pure Java for testing. */
public final class DirectionEngine {
    public enum Move { NEXT, PREVIOUS }

    private final Random random;
    private final ArrayList<Boolean> nextIsUp = new ArrayList<>();
    private int index;

    public DirectionEngine(Random random) {
        this.random = random;
        reset();
    }

    public void reset() {
        nextIsUp.clear();
        nextIsUp.add(random.nextBoolean());
        index = 0;
    }

    public boolean nextRequiresUp() {
        return nextIsUp.get(index);
    }

    public Move classify(boolean fingerMovedUp) {
        return fingerMovedUp == nextRequiresUp() ? Move.NEXT : Move.PREVIOUS;
    }

    /** Call only after Android confirms a synthetic swipe was dispatched. */
    public void dispatched(Move move) {
        if (move == Move.NEXT) {
            index++;
            if (index == nextIsUp.size()) {
                nextIsUp.add(random.nextBoolean());
            }
        } else if (index > 0) {
            index--;
        } else {
            // The user moved to a video that predates activation.
            nextIsUp.set(0, random.nextBoolean());
        }
    }

    public int index() {
        return index;
    }
}
