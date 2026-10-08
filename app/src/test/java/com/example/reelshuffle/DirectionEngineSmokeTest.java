package com.example.reelshuffle;

import java.util.Random;

public class DirectionEngineSmokeTest {
    public static void main(String[] args) {
        DirectionEngine engine = new DirectionEngine(new Random(12345));
        int forwards = 0;
        int ups = 0;
        int downs = 0;
        for (int i = 0; i < 10000; i++) {
            boolean expectedUp = engine.nextRequiresUp();
            if (expectedUp) ups++; else downs++;
            if (engine.classify(expectedUp) != DirectionEngine.Move.NEXT) throw new AssertionError("correct direction failed");
            if (engine.classify(!expectedUp) != DirectionEngine.Move.PREVIOUS) throw new AssertionError("reverse direction failed");
            int prior = engine.index();
            engine.dispatched(DirectionEngine.Move.NEXT);
            if (engine.index() != prior + 1) throw new AssertionError("forward index");
            forwards++;
        }
        for (int i = 0; i < 10000; i++) engine.dispatched(DirectionEngine.Move.PREVIOUS);
        if (engine.index() != 0) throw new AssertionError("reverse history");
        boolean firstExpected = engine.nextRequiresUp();
        engine.dispatched(DirectionEngine.Move.PREVIOUS);
        if (engine.index() != 0) throw new AssertionError("index underflow");
        if (forwards != 10000 || ups < 4500 || downs < 4500) throw new AssertionError("random distribution");
        System.out.println("PASS: 10000 forward decisions, 10000 backward decisions, random up=" + ups + " down=" + downs + "; initial direction=" + firstExpected);
    }
}
