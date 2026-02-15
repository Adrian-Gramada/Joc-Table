package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dice {
    private final Random rnd = new Random();
    private List<Integer> remaining = new ArrayList<>();

    public void roll() {
        remaining.clear();
        int a = rnd.nextInt(6) + 1;
        int b = rnd.nextInt(6) + 1;
        if (a == b) {
            for (int i = 0; i < 4; i++) remaining.add(a);
        } else {
            remaining.add(a);
            remaining.add(b);
        }
    }

    public List<Integer> getRemaining() { return new ArrayList<>(remaining); }

    public boolean useDie(int value) {
        for (int i = 0; i < remaining.size(); i++) {
            if (remaining.get(i) == value) {
                remaining.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean hasMoves() { return !remaining.isEmpty(); }

    public String toString() {
        return remaining.toString();
    }
}

