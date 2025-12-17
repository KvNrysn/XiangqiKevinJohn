package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.ChessBoardModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MoveHistoryPanel extends JPanel {
    private static final Color GOLD = new Color(212, 175, 55);//colorvars
    private static final Color BG = new Color(14, 14, 14);
    private static final Color CAPTURE_GREEN = new Color(120, 200, 120);//for captures, history are green

    private JPanel tableBody;//table panel
    private JScrollPane scrollPane;//scrollpane inside the panel
    private JLabel footerLabel;//footer inside pannel

    public MoveHistoryPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);//parent is transparent, no dim
        setPreferredSize(new Dimension(320, 0));//width panel

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 18;

        g2.setColor(BG);//panel overlay rounded
        g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, arc, arc);

        g2.setStroke(new BasicStroke(1.5f));//gold border
        g2.setColor(GOLD);
        g2.drawRoundRect(6, 6, getWidth() - 12, getHeight() - 12, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(22, 18, 12, 18));

        JLabel titleMain = new JLabel("Move Record");
        titleMain.setFont(new Font("Serif", Font.BOLD, 20));
        titleMain.setForeground(Color.WHITE);
        titleMain.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleSub = new JLabel("Moves History");
        titleSub.setFont(new Font("Serif", Font.PLAIN, 14));
        titleSub.setForeground(GOLD);
        titleSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel columns = new JPanel(new GridLayout(1, 3));//column headers rond, red, black
        columns.setOpaque(false);
        columns.setBorder(new EmptyBorder(18, 6, 6, 6));

        columns.add(columnLabel("Round", GOLD));
        columns.add(columnLabel("Red", new Color(200, 0, 0)));
        columns.add(columnLabel("Black", Color.LIGHT_GRAY));

        header.add(titleMain);
        header.add(Box.createVerticalStrut(4));//spacing between title/subtitle
        header.add(titleSub);
        header.add(columns);

        return header;
    }

    private JLabel columnLabel(String text, Color color) {//helper method to create a styled column label
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 13));
        l.setForeground(color);
        return l;
    }

    private JComponent buildTable() {//scrollable
        tableBody = new JPanel();
        tableBody.setLayout(new BoxLayout(tableBody, BoxLayout.Y_AXIS));
        tableBody.setOpaque(false);
        tableBody.setBorder(new EmptyBorder(4, 10, 4, 10));

        scrollPane = new JScrollPane(tableBody);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);//smooth scrolling
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 18, 14, 18));

        footerLabel = new JLabel("Total 0 rounds | 0 moves");
        footerLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        footerLabel.setForeground(GOLD);

        footer.add(footerLabel, BorderLayout.WEST);
        return footer;
    }

    public void updateMoves(List<ChessBoardModel.Move> moves) {//refresh move list for new game
        tableBody.removeAll();//clear previous game's data

        int rounds = (moves.size() + 1) / 2;//every 2 moves = 1 round

        for (int i = 0; i < rounds; i++) {
            ChessBoardModel.Move red =
                    (i * 2 < moves.size()) ? moves.get(i * 2) : null;
            ChessBoardModel.Move black =
                    (i * 2 + 1 < moves.size()) ? moves.get(i * 2 + 1) : null;
            tableBody.add(createRow(i + 1, red, black));
        }

        footerLabel.setText(
                "Total " + rounds + " rounds | " + moves.size() + " moves"
        );

        tableBody.revalidate();
        tableBody.repaint();

        SwingUtilities.invokeLater(() -> {//automatically scroll to the bottom after update
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private JPanel createRow(int round, ChessBoardModel.Move red, ChessBoardModel.Move black) {
        JPanel row = new JPanel(new GridLayout(1, 3));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 4, 6, 4));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        row.add(cell(String.valueOf(round), GOLD));//round number and two move cells
        row.add(moveCell(red, true));
        row.add(moveCell(black, false));

        return row;
    }

    private JLabel cell(String text, Color color) {//generic cell with colored text
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.PLAIN, 13));
        l.setForeground(color);
        return l;
    }

    private JLabel moveCell(ChessBoardModel.Move m, boolean redSide) {
        if (m == null) return cell("", Color.GRAY); //empty move slot

        Color base = m.capture ? CAPTURE_GREEN : (redSide ? new Color(200, 0, 0) : Color.LIGHT_GRAY);

        JLabel l = new JLabel(formatMove(m), SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.PLAIN, 13));
        l.setForeground(base);
        return l;
    }

    private String formatMove(ChessBoardModel.Move m) {//formulate every moves into the current data historyformat
        String arrow = "→";
        return m.piece +
                " (" + m.fromCol + "," + m.fromRow + ")" +
                arrow +
                "(" + m.toCol + "," + m.toRow + ")";
    }
}
