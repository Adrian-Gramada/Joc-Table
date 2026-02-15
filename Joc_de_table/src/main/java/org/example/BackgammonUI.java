package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BackgammonUI {
    private final Game game = new Game();
    private final JFrame frame = new JFrame("Backgammon");
    private final BoardCanvas boardCanvas = new BoardCanvas();
    private final JLabel statusLabel = new JLabel();
    private final JButton rollButton = new JButton("Roll");
    private final JButton endTurnButton = new JButton("End Turn");
    private final DicePanel dicePanel = new DicePanel();
    private final JButton flipButton = new JButton("Flip Board");

    
    private final JLabel whiteBarLabel = new JLabel();
    private final JLabel blackBarLabel = new JLabel();
    private final JLabel whiteOffLabel = new JLabel();
    private final JLabel blackOffLabel = new JLabel();
    private final JButton selectBarButton = new JButton("Select Bar");
    private final JButton bearOffButton = new JButton("Bear Off (from selected)");

    
    private Integer selectedFrom = null; 
    private List<Integer> legalDestinations = new ArrayList<>();

    public static void createAndShowGUI() {
        BackgammonUI ui = new BackgammonUI();
        ui.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.frame.setSize(1200, 700);
        ui.frame.setLocationRelativeTo(null);
        ui.frame.setVisible(true);
    }

    public BackgammonUI() {
        
        boardCanvas.flip = true;
        
        game.getBoard().setWhiteMovesIncreasing(false);
        
        frame.getContentPane().setBackground(new Color(139, 69, 19));
        frame.setLayout(new BorderLayout(8, 8));

        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        top.setOpaque(false);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        top.add(statusLabel);
        top.add(rollButton);
        top.add(endTurnButton);
        top.add(flipButton);
        top.add(dicePanel);
        frame.add(top, BorderLayout.NORTH);

        
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        boardCanvas.setOpaque(false);
        center.add(boardCanvas, BorderLayout.CENTER);

        
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "Bar / Off"));
        whiteBarLabel.setForeground(Color.WHITE);
        blackBarLabel.setForeground(Color.WHITE);
        whiteOffLabel.setForeground(Color.WHITE);
        blackOffLabel.setForeground(Color.WHITE);
        right.add(Box.createVerticalStrut(8));
        right.add(whiteBarLabel);
        right.add(blackBarLabel);
        right.add(Box.createVerticalStrut(8));
        right.add(whiteOffLabel);
        right.add(blackOffLabel);
        right.add(Box.createVerticalStrut(12));
        right.add(selectBarButton);
        right.add(Box.createVerticalStrut(6));
        right.add(bearOffButton);

        center.add(right, BorderLayout.EAST);

        frame.add(center, BorderLayout.CENTER);

        
        JTextArea help = new JTextArea("Click a point to select a piece, then click destination. Use 'Select Bar' to select from bar. Use 'Bear Off' to attempt bearing off from selected point.");
        help.setEditable(false);
        help.setOpaque(false);
        help.setForeground(Color.WHITE);
        frame.add(help, BorderLayout.SOUTH);

        
        rollButton.addActionListener(e -> onRoll());
        endTurnButton.addActionListener(e -> onEndTurn());
        selectBarButton.addActionListener(e -> onSelectBar());
        bearOffButton.addActionListener(e -> onBearOff());
        flipButton.addActionListener(e -> {
            boardCanvas.flip = !boardCanvas.flip;
            
            selectedFrom = null;
            legalDestinations.clear();
            updateAll();
        });

        endTurnButton.setEnabled(false);

        updateAll();
    }

    private void updateAll() {
        boardCanvas.repaint();
        whiteBarLabel.setText("White bar: " + game.getBoard().whiteBar);
        blackBarLabel.setText("Black bar: " + game.getBoard().blackBar);
        whiteOffLabel.setText("White off: " + game.getBoard().whiteBornOff);
        blackOffLabel.setText("Black off: " + game.getBoard().blackBornOff);
        if (game.hasDiceMoves()) dicePanel.setDice(game.getRemainingDice()); else dicePanel.clear();
        updateStatus();
    }

    private void onRoll() {
        if (game.hasDiceMoves()) {
            JOptionPane.showMessageDialog(frame, "Dice already rolled this turn");
            return;
        }
        List<Integer> dice = game.rollDice();
        dicePanel.setDice(dice);
        rollButton.setEnabled(false);
        endTurnButton.setEnabled(true);
        updateAll();
    }

    private void onEndTurn() {
        game.endTurn();
        selectedFrom = null;
        legalDestinations.clear();
        rollButton.setEnabled(true);
        endTurnButton.setEnabled(false);
        dicePanel.clear();
        updateAll();
    }

    private void onSelectBar() {
        if (!game.canSelectFrom(-1)) {
            JOptionPane.showMessageDialog(frame, "No pieces on bar to select");
            return;
        }
        selectedFrom = -1;
        legalDestinations = game.getLegalDestinations(-1);
        updateAll();
    }

    private void onBearOff() {
        if (selectedFrom == null) {
            JOptionPane.showMessageDialog(frame, "Select a point or the bar first to bear off from.");
            return;
        }
        Board.MoveResult res = game.moveFromTo(selectedFrom, -2);
        selectedFrom = null;
        legalDestinations.clear();
        if (!res.success) JOptionPane.showMessageDialog(frame, "Illegal bear off: " + res.message);
        updateAll();
    }

    
    private class BoardCanvas extends JComponent {
        public boolean flip = false; 
        private final List<Polygon> pointAreas = new ArrayList<>();

        public BoardCanvas() {
            setPreferredSize(new Dimension(1000, 520)); 
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int mx = e.getX(); int my = e.getY();
                    
                    for (int areaIndex = 0; areaIndex < pointAreas.size(); areaIndex++) {
                        Polygon poly = pointAreas.get(areaIndex);
                        if (poly != null && poly.contains(mx, my)) {
                            int gameIndex = areaIndexToGameIndex(areaIndex);
                            if (e.getClickCount() == 2) {
                                
                                List<Integer> dests = game.getLegalDestinations(gameIndex);
                                if (dests.contains(-2)) {
                                    Board.MoveResult res = game.moveFromTo(gameIndex, -2);
                                    if (!res.success) JOptionPane.showMessageDialog(frame, "Illegal bear off: " + res.message);
                                    selectedFrom = null; legalDestinations.clear(); updateAll();
                                    return;
                                }
                                
                            }
                            
                            handleClick(mx, my);
                            return;
                        }
                    }
                }
            });
        }

        private void handleClick(int mx, int my) {
            
            for (int areaIndex = 0; areaIndex < pointAreas.size(); areaIndex++) {
                Polygon poly = pointAreas.get(areaIndex);
                if (poly != null && poly.contains(mx, my)) {
                    int gameIndex = areaIndexToGameIndex(areaIndex);
                    onPointCanvasClicked(gameIndex);
                    return;
                }
            }
            
        }

        private void onPointCanvasClicked(int pointIndex) {
            
            if (selectedFrom == null) {
                if (!game.canSelectFrom(pointIndex)) {
                    
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                selectedFrom = pointIndex;
                legalDestinations = game.getLegalDestinations(pointIndex);
            } else {
                
                if (selectedFrom == pointIndex) {
                    selectedFrom = null; legalDestinations.clear(); updateAll(); return;
                }
                
                if (legalDestinations.contains(pointIndex)) {
                    Board.MoveResult res = game.moveFromTo(selectedFrom, pointIndex);
                    if (!res.success) JOptionPane.showMessageDialog(frame, "Illegal move: " + res.message);
                    selectedFrom = null;
                    legalDestinations.clear();
                } else {
                    
                    if (game.canSelectFrom(pointIndex)) {
                        selectedFrom = pointIndex;
                        legalDestinations = game.getLegalDestinations(pointIndex);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
            updateAll();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            
            g2.setColor(new Color(160, 82, 45));
            g2.fillRoundRect(10, 10, w - 20, h - 20, 24, 24);

            
            int outerMargin = 40;
            int availW = w - outerMargin * 2;
            int availH = h - outerMargin * 2;
            int playSize = Math.max(0, Math.min(availW, availH));
            int marginX = (w - playSize) / 2;
            int marginY = (h - playSize) / 2;
            int margin = marginX; 
            int playW = playSize;
            int playH = playSize;
            int gap = 8;

            
            pointAreas.clear();
            int pointW = (playW - gap * 11) / 12;
            int triHeight = playH / 2 - 20;
            int topY = marginY;
            int bottomY = marginY + playH / 2;

            
            int midX = margin + playW / 2;
            int seamW = Math.max(8, playW / 120);
            
            g2.setColor(new Color(80, 45, 20));
            g2.fillRoundRect(midX - seamW / 2, topY, seamW, playH, seamW, seamW);
            
            g2.setColor(new Color(200, 160, 120, 120));
            g2.fillRect(midX - 2, topY + 6, 4, playH - 12);
            
            Stroke old = g2.getStroke();
            float[] dash = {6f, 6f};
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.setColor(new Color(30, 20, 10, 180));
            g2.drawLine(midX, topY + 10, midX, topY + playH - 10);
            g2.setStroke(old);

            
            for (int i = 0; i < 12; i++) {
                int idx = areaIndexToGameIndex(i);
                int x = margin + i * (pointW + gap);
                Polygon tri = new Polygon();
                tri.addPoint(x, topY);
                tri.addPoint(x + pointW, topY);
                tri.addPoint(x + pointW / 2, topY + triHeight);
                boolean dark = (i % 2 == 0);
                Color triColor = dark ? new Color(102, 51, 0) : new Color(222, 184, 135);
                g2.setColor(triColor);
                g2.fill(tri);
                
                pointAreas.add(tri);

                
                drawCheckersOnPoint(g2, idx, x + pointW / 2, topY + 6, true);

                
                if (legalDestinations.contains(idx)) {
                    int dotX = x + pointW / 2 - 8;
                    int dotY = topY + triHeight - 28; 
                    g2.setColor(new Color(0, 180, 60, 220));
                    g2.fillOval(dotX, dotY, 16, 16);
                    g2.setColor(new Color(0, 110, 30, 200));
                    g2.drawOval(dotX, dotY, 16, 16);
                }
            }

            
            for (int i = 0; i < 12; i++) {
                int idx = areaIndexToGameIndex(12 + i); 
                int x = margin + i * (pointW + gap);
                Polygon tri = new Polygon();
                tri.addPoint(x, bottomY + triHeight);
                tri.addPoint(x + pointW, bottomY + triHeight);
                tri.addPoint(x + pointW / 2, bottomY);
                boolean dark = (i % 2 == 0);
                Color triColor = dark ? new Color(102, 51, 0) : new Color(222, 184, 135);
                g2.setColor(triColor);
                g2.fill(tri);
                pointAreas.add(tri);

                
                drawCheckersOnPoint(g2, idx, x + pointW / 2, bottomY + triHeight - 6, false);

                
                if (legalDestinations.contains(idx)) {
                    int dotX = x + pointW / 2 - 8;
                    int dotY = bottomY - 20; 
                    g2.setColor(new Color(0, 180, 60, 220));
                    g2.fillOval(dotX, dotY, 16, 16);
                    g2.setColor(new Color(0, 110, 30, 200));
                    g2.drawOval(dotX, dotY, 16, 16);
                }
            }

            
            int barW = Math.max(48, playW / 18);
            int barX = margin + 12 * (pointW + gap) + Math.max(8, (playW - (12 * pointW + 11 * gap) - barW) / 2);
            int barY = marginY + playH / 4;
            g2.setColor(new Color(120, 70, 30));
            g2.fillRoundRect(barX, barY, barW, playH / 2, 12, 12);
            g2.setColor(Color.WHITE);
            g2.drawString("BAR", barX + 12, barY + 14);
            
            drawBarCheckers(g2, barX + barW / 2, barY + 20);

            
            g2.setColor(new Color(40, 20, 10, 200));
            g2.setStroke(new BasicStroke(4f));
            g2.drawRoundRect(margin - 6, marginY - 6, playW + 12, playH + 12, 20, 20);
            
            g2.setColor(new Color(220, 190, 160, 90));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(margin - 2, marginY - 2, playW + 4, playH + 4, 16, 16);

            
            if (selectedFrom != null && selectedFrom >= 0 && selectedFrom < 24) {
                Polygon selPoly = pointAreas.get(gameIndexToAreaIndex(selectedFrom));
                if (selPoly != null) {
                    Rectangle selRect = selPoly.getBounds();
                    g2.setColor(new Color(255, 0, 0, 120));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(selRect.x + 2, selRect.y + 2, selRect.width - 4, selRect.height - 4, 8, 8);
                }
            }

            g2.dispose();
        }

        private int areaIndexToGameIndex(int areaIndex) {
            if (!flip) {
                if (areaIndex < 12) return 23 - areaIndex; 
                return areaIndex - 12; 
            } else {
                if (areaIndex < 12) return 12 + areaIndex; 
                return 11 - (areaIndex - 12); 
            }
        }

        private int gameIndexToAreaIndex(int gameIndex) {
            if (!flip) {
                if (gameIndex >= 12) return 23 - gameIndex; 
                return 12 + gameIndex; 
            } else {
                if (gameIndex >= 12) return gameIndex - 12; 
                return 12 + (11 - gameIndex); 
            }
        }

        private void drawBarCheckers(Graphics2D g2, int cx, int startY) {
            Board b = game.getBoard();
            int y = startY;
            if (b.blackBar > 0) {
                for (int i = 0; i < b.blackBar; i++) {
                    drawChecker(g2, cx - 20, y + i * 18, 28, -1);
                }
            }
            if (b.whiteBar > 0) {
                int y2 = startY + 50;
                for (int i = 0; i < b.whiteBar; i++) {
                    drawChecker(g2, cx - 20, y2 + i * 18, 28, 1);
                }
            }
        }

        private void drawCheckersOnPoint(Graphics2D g2, int pointIndex, int cx, int yBase, boolean downwards) {
            Board b = game.getBoard();
            Point p = b.points[pointIndex];
            int count = p.count;
            int owner = p.owner;
            int radius = 28;
            int spacing = radius - 8;
            if (count == 0) return;
            if (downwards) {
                for (int i = 0; i < count; i++) {
                    int y = yBase + i * spacing;
                    drawChecker(g2, cx - radius / 2, y, radius, owner);
                }
            } else {
                for (int i = 0; i < count; i++) {
                    int y = yBase - i * spacing;
                    drawChecker(g2, cx - radius / 2, y, radius, owner);
                }
            }
            if (count > 6) {
                
                g2.setColor(new Color(0, 0, 0, 160));
                int bx = cx + 20;
                int by = yBase - 10;
                g2.fillOval(bx, by, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                String s = String.valueOf(count);
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(s);
                int sh = fm.getAscent();
                g2.drawString(s, bx + (28 - sw) / 2, by + (28 + sh) / 2 - 3);
            }
        }

        private void drawChecker(Graphics2D g2, int x, int y, int size, int owner) {
            Color pieceColor;
            if (owner == 1) pieceColor = Color.WHITE;
            else if (owner == -1) pieceColor = new Color(153, 0, 0); 
            else pieceColor = Color.LIGHT_GRAY;
            
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(x + 4, y + 6, size, size);
            
            g2.setColor(pieceColor);
            g2.fillOval(x, y, size, size);
            
            g2.setColor(new Color(50, 30, 20));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x, y, size, size);
        }
    }

    
    private class DicePanel extends JPanel {
        private List<Integer> dice = new ArrayList<>();

        public DicePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(180, 48));
        }

        public void setDice(List<Integer> dice) {
            this.dice = new ArrayList<>(dice);
            repaint();
        }

        public void clear() {
            this.dice.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            
            g2.setColor(new Color(60, 40, 20, 180));
            g2.fillRoundRect(0, 0, w, h, 8, 8);

            int dieSize = 36;
            int gap = 8;
            int startX = 6;
            for (int i = 0; i < Math.max(2, dice.size()); i++) {
                int x = startX + i * (dieSize + gap);
                int y = 6;
                if (i < dice.size()) drawDie(g2, x, y, dieSize, dice.get(i));
                else drawDie(g2, x, y, dieSize, 0);
            }
            g2.dispose();
        }

        private void drawDie(Graphics2D g2, int x, int y, int size, int value) {
            
            g2.setColor(new Color(250, 250, 250));
            g2.fillRoundRect(x, y, size, size, 6, 6);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(x, y, size, size, 6, 6);
            if (value <= 0) return;
            
            g2.setColor(Color.BLACK);
            int cx = x + size / 2;
            int cy = y + size / 2;
            int offset = size / 4;
            int r = 4 + size / 18;
            switch (value) {
                case 1:
                    fillCircle(g2, cx, cy, r); break;
                case 2:
                    fillCircle(g2, cx - offset, cy - offset, r);
                    fillCircle(g2, cx + offset, cy + offset, r); break;
                case 3:
                    fillCircle(g2, cx - offset, cy - offset, r);
                    fillCircle(g2, cx, cy, r);
                    fillCircle(g2, cx + offset, cy + offset, r); break;
                case 4:
                    fillCircle(g2, cx - offset, cy - offset, r);
                    fillCircle(g2, cx + offset, cy - offset, r);
                    fillCircle(g2, cx - offset, cy + offset, r);
                    fillCircle(g2, cx + offset, cy + offset, r); break;
                case 5:
                    fillCircle(g2, cx - offset, cy - offset, r);
                    fillCircle(g2, cx + offset, cy - offset, r);
                    fillCircle(g2, cx, cy, r);
                    fillCircle(g2, cx - offset, cy + offset, r);
                    fillCircle(g2, cx + offset, cy + offset, r); break;
                case 6:
                    fillCircle(g2, cx - offset, cy - offset, r);
                    fillCircle(g2, cx + offset, cy - offset, r);
                    fillCircle(g2, cx - offset, cy, r);
                    fillCircle(g2, cx + offset, cy, r);
                    fillCircle(g2, cx - offset, cy + offset, r);
                    fillCircle(g2, cx + offset, cy + offset, r);
                    break;
            }
        }

        private void fillCircle(Graphics2D g2, int cx, int cy, int r) {
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }
    }


    private void updateStatus() {
        String player = game.getCurrentPlayer() == 1 ? "White" : "Black";
        statusLabel.setText("Player: " + player + (selectedFrom != null ? "  (selected: " + (selectedFrom == -1 ? "BAR" : selectedFrom + 1) + ")" : ""));
        if (game.hasDiceMoves()) dicePanel.setDice(game.getRemainingDice());
        if (game.isGameOver()) {
            String w = game.getWinner() == 1 ? "White" : "Black";
            JOptionPane.showMessageDialog(frame, w + " wins!");
            rollButton.setEnabled(false);
            endTurnButton.setEnabled(false);
            selectBarButton.setEnabled(false);
            bearOffButton.setEnabled(false);
        }
    }
}

