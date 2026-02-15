package org.example;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Board board = new Board();
    private Dice dice = new Dice();
    private int currentPlayer = 1;
    private boolean gameOver = false;
    private int winner = 0;

    public void start() {
        IO.println("Starting Backgammon (simple CLI)");
        while (true) {
            IO.println(board.render());
            IO.println((currentPlayer == 1 ? "White" : "Black") + " to move. Press ENTER to roll.");
            IO.readLine();
            dice.roll();
            IO.println("Dice: " + dice);

            while (dice.hasMoves()) {
                IO.println("Remaining dice: " + dice);
                IO.println("Enter move for a die (format: from-to) where from is 1..24 or 'bar' and to is 1..24 or 'off', or 'pass'");
                IO.print("> ");
                String line = IO.readLine().trim();
                if (line.equalsIgnoreCase("pass")) break;
                String[] parts = line.split("-");
                if (parts.length != 2) { IO.println("Invalid format. Use from-to"); continue; }
                int fromIndex;
                if (parts[0].equalsIgnoreCase("bar")) fromIndex = -1;
                else {
                    try { fromIndex = Integer.parseInt(parts[0]) - 1; }
                    catch (Exception e) { IO.println("Invalid from"); continue; }
                }
                int dieUsed = -1;
                if (parts[1].equalsIgnoreCase("off")) {
                    boolean applied = false;
                    for (int d : dice.getRemaining()) {
                        Board.MoveResult res = board.tryMove(currentPlayer, fromIndex, d);
                        if (res.success) {
                            dice.useDie(d);
                            IO.println("Move: " + res.message);
                            applied = true;
                            break;
                        }
                    }
                    if (!applied) IO.println("No valid die to bear off with that move");
                    continue;
                } else {
                    int toIndex;
                    try { toIndex = Integer.parseInt(parts[1]) - 1; }
                    catch (Exception e) { IO.println("Invalid to"); continue; }
                    boolean applied = false;
                    for (int d : dice.getRemaining()) {
                        int expectedTo = board.getToIndex(currentPlayer, fromIndex, d);
                        if (expectedTo == toIndex) {
                            Board.MoveResult res = board.tryMove(currentPlayer, fromIndex, d);
                            if (res.success) {
                                dice.useDie(d);
                                IO.println("Move: " + res.message);
                            } else IO.println("Illegal move: " + res.message);
                            applied = true;
                            break;
                        }
                    }
                    if (!applied) IO.println("No matching die available");
                }
            }

            if (board.whiteBornOff >= 15) { IO.println("White wins!"); break; }
            if (board.blackBornOff >= 15) { IO.println("Black wins!"); break; }

            currentPlayer = -currentPlayer;
        }
    }

    public Board getBoard() {
        return board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() { return gameOver; }

    public int getWinner() { return winner; }

    public List<Integer> rollDice() {
        dice.roll();
        return dice.getRemaining();
    }

    public List<Integer> getRemainingDice() {
        return dice.getRemaining();
    }

    public boolean hasDiceMoves() { return dice.hasMoves(); }

    public Board.MoveResult applyMoveWithDie(int fromIndex, int die) {
        Board.MoveResult res = board.tryMove(currentPlayer, fromIndex, die);
        if (res.success) {
            dice.useDie(die);
            checkWin();
        }
        return res;
    }

    public Board.MoveResult moveFromTo(int fromIndex, int toIndex) {
        if (toIndex == -2) {
            for (int d : dice.getRemaining()) {
                Board.MoveResult res = board.tryMove(currentPlayer, fromIndex, d);
                if (res.success) {
                    dice.useDie(d);
                    checkWin();
                    return res;
                }
            }
            return new Board.MoveResult(false, "No valid die to bear off");
        }

        for (int d : dice.getRemaining()) {
            int expectedTo = board.getToIndex(currentPlayer, fromIndex, d);
            System.out.println("Debug: moveFromTo attempt: player=" + currentPlayer + " from=" + fromIndex + " die=" + d + " expectedTo=" + expectedTo + " targetRequested=" + toIndex);
            if (expectedTo == toIndex) {
                Board.MoveResult res = board.tryMove(currentPlayer, fromIndex, d);
                System.out.println("Debug: board.tryMove returned: success=" + res.success + " message='" + res.message + "'");
                if (res.success) {
                    dice.useDie(d);
                    checkWin();
                }
                return res;
            }
        }
        return new Board.MoveResult(false, "No matching die available");
    }

    public void endTurn() {
        dice = new Dice();
        currentPlayer = -currentPlayer;
    }

    private void checkWin() {
        if (board.whiteBornOff >= 15) { gameOver = true; winner = 1; }
        if (board.blackBornOff >= 15) { gameOver = true; winner = -1; }
    }

    public List<Integer> getLegalDestinations(int fromIndex) {
        List<Integer> res = new ArrayList<>();
        if (!dice.hasMoves()) return res;
        for (int d : dice.getRemaining()) {
            int toIndex = board.getToIndex(currentPlayer, fromIndex, d);
            if (toIndex < 0 || toIndex > 23) {
                if (board.isMoveLegal(currentPlayer, fromIndex, d)) res.add(-2);
            } else {
                if (board.isMoveLegal(currentPlayer, fromIndex, d)) res.add(toIndex);
            }
        }
        List<Integer> uniq = new ArrayList<>();
        for (int v : res) if (!uniq.contains(v)) uniq.add(v);
        return uniq;
    }

    public boolean canSelectFrom(int fromIndex) {
        if (fromIndex == -1) {
            return (currentPlayer == 1) ? board.whiteBar > 0 : board.blackBar > 0;
        }
        if (fromIndex < 0 || fromIndex > 23) return false;
        Point p = board.points[fromIndex];
        return p.owner == currentPlayer && p.count > 0;
    }
}

