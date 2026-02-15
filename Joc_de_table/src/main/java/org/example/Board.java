package org.example;

import java.util.Arrays;

public class Board {
    public Point[] points = new Point[24];
    public int whiteBar = 0;
    public int blackBar = 0;
    public int whiteBornOff = 0;
    public int blackBornOff = 0;

    private boolean whiteMovesIncreasing = true;

    public Board() {
        for (int i = 0; i < 24; i++) points[i] = new Point();
        setupInitial();
    }

    public void setWhiteMovesIncreasing(boolean v) { this.whiteMovesIncreasing = v; }
    public boolean isWhiteMovesIncreasing() { return this.whiteMovesIncreasing; }

    public int getToIndex(int player, int fromIndex, int die) {
        int to;
        if (fromIndex == -1) {
            if ((player == 1) == whiteMovesIncreasing) {
                to = die - 1;
            } else {
                to = 24 - die;
            }
        } else {
            if ((player == 1) == whiteMovesIncreasing) return fromIndex + die; else return fromIndex - die;
        }
        System.out.println("Debug:getToIndex player=" + player + " from=" + fromIndex + " die=" + die + " -> toIndex=" + to + " whiteMovesIncreasing=" + whiteMovesIncreasing);
        return to;
    }

    private void setupInitial() {
        for (Point p : points) { p.owner = 0; p.count = 0; }

        points[0] = new Point(-1, 2);
        points[5] = new Point(1, 5);
        points[7] = new Point(1, 3);
        points[11] = new Point(-1, 5);
        points[12] = new Point(1, 5);
        points[16] = new Point(-1, 3);
        points[18] = new Point(-1, 5);
        points[23] = new Point(1, 2);
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board:\n");
        for (int i = 23; i >= 12; i--) {
            Point p = points[i];
            sb.append(String.format("%2d[%s%2d] ", i+1, ownerChar(p.owner), p.count));
            if (i == 12) sb.append('\n');
        }
        for (int i = 0; i <= 11; i++) {
            Point p = points[i];
            sb.append(String.format("%2d[%s%2d] ", i+1, ownerChar(p.owner), p.count));
        }
        sb.append(String.format("\nWhite bar: %d  Black bar: %d  White off: %d  Black off: %d\n", whiteBar, blackBar, whiteBornOff, blackBornOff));
        return sb.toString();
    }

    private String ownerChar(int owner) {
        if (owner == 1) return "W";
        if (owner == -1) return "B";
        return " ";
    }

    public boolean allInHome(int player) {
        int inHome = 0;
        if ((player == 1) == whiteMovesIncreasing) {
            for (int i = 18; i <= 23; i++) if (points[i].owner == player) inHome += points[i].count;
        } else {
            for (int i = 0; i <= 5; i++) if (points[i].owner == player) inHome += points[i].count;
        }
        int bornOff = (player == 1) ? whiteBornOff : blackBornOff;
        int bar = (player == 1) ? whiteBar : blackBar;
        return (inHome + bornOff) == 15 && bar == 0;
    }

    public boolean isMoveLegal(int player, int fromIndex, int die) {
        int toIndex = getToIndex(player, fromIndex, die);

        if (toIndex < 0 || toIndex > 23) {
            if (!allInHome(player)) return false;
            if (fromIndex == -1) return false;
            Point src = points[fromIndex];
            if (src.owner != player || src.count == 0) return false;
            if ((player == 1) == whiteMovesIncreasing) {
                int required = 24 - fromIndex;
                if (die == required) return true;
                if (die > required) {
                    for (int i = 18; i < fromIndex; i++) {
                        if (points[i].owner == player && points[i].count > 0) return false;
                    }
                    return true;
                }
                return false;
            } else {
                int required = fromIndex + 1;
                if (die == required) return true;
                if (die > required) {
                    for (int i = fromIndex - 1; i > 5; i--) {
                        if (points[i].owner == player && points[i].count > 0) return false;
                    }
                    return true;
                }
                return false;
            }
        }

        if (fromIndex == -1) {
            if ((player == 1 && whiteBar == 0) || (player == -1 && blackBar == 0)) return false;
        } else {
            if (fromIndex < 0 || fromIndex > 23) return false;
            Point src = points[fromIndex];
            if (src.owner != player || src.count == 0) return false;
        }

        Point dest = points[toIndex];
        if (dest.owner == -player && dest.count > 1) return false;
        return true;
    }

    public MoveResult tryMove(int player, int fromIndex, int die) {
        int toIndex = getToIndex(player, fromIndex, die);
        System.out.println("Debug: tryMove called player=" + player + " from=" + fromIndex + " die=" + die + " computedTo=" + toIndex);

        if (!isMoveLegal(player, fromIndex, die)) {
            return new MoveResult(false, "Illegal move or bearing off not allowed");
        }

        if (toIndex < 0 || toIndex > 23) {
            if (allInHome(player)) {
                if (fromIndex == -1) return new MoveResult(false, "No pieces on bar to bear off");
                Point p = points[fromIndex];
                if (p.owner != player || p.count == 0) return new MoveResult(false, "No piece of yours at from point");
                p.removeOne();
                if (player == 1) whiteBornOff++; else blackBornOff++;
                return new MoveResult(true, "Borne off");
            } else {
                return new MoveResult(false, "Cannot bear off unless all pieces in home and no pieces on bar");
            }
        }

        Point dest = points[toIndex];
        if (fromIndex != -1) {
            Point src = points[fromIndex];
            if (src.owner != player || src.count == 0) return new MoveResult(false, "No piece of yours at from point");
        } else {
            if ((player == 1 && whiteBar == 0) || (player == -1 && blackBar == 0)) return new MoveResult(false, "No pieces on bar to enter");
        }

        if (dest.owner == -player && dest.count > 1) {
            return new MoveResult(false, "Destination blocked by opponent");
        }

        if (fromIndex == -1) {
            if (player == 1) whiteBar--; else blackBar--;
        } else {
            Point src = points[fromIndex];
            src.removeOne();
        }

        if (dest.owner == -player && dest.count == 1) {
            dest.removeOne();
            if (player == 1) blackBar++; else whiteBar++;
        }

        dest.add(player);
        System.out.println("Debug: performed move player=" + player + " toIndex=" + toIndex + " destOwner=" + dest.owner + " destCount=" + dest.count);
        return new MoveResult(true, "Moved to " + (toIndex+1));
    }

    public static class MoveResult {
        public final boolean success;
        public final String message;
        public MoveResult(boolean success, String message) { this.success = success; this.message = message; }
    }
}

