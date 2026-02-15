package org.example;

public class Point {
    int owner;
    int count;

    public Point() {
        this.owner = 0;
        this.count = 0;
    }

    public Point(int owner, int count) {
        this.owner = owner;
        this.count = count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void add(int owner) {
        if (count == 0) {
            this.owner = owner;
            this.count = 1;
        } else if (this.owner == owner) {
            this.count++;
        } else {
        }
    }

    public void removeOne() {
        if (count > 0) {
            count--;
            if (count == 0) owner = 0;
        }
    }
}

