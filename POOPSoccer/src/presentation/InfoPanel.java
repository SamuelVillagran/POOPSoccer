package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Top HUD bar: player names, health bars, score and timer.
 */
public class InfoPanel extends JPanel {

    private static final Font FONT_HUD   = new Font("Arial", Font.BOLD, 13);
    private static final Font FONT_SCORE = new Font("Arial", Font.BOLD, 18);

    private static final Color BAR_BG    = new Color(60, 10, 10);
    private static final Color BAR_HIGH  = new Color(50, 200, 50);   // > 50%
    private static final Color BAR_MID   = new Color(220, 180, 0);   // 25-50%
    private static final Color BAR_LOW   = new Color(200, 40, 40);   // <= 25%

    // State updated each frame from GamePanel
    private String p1Name  = "P1";
    private String p2Name  = "P2";
    private String score   = "0 - 0";
    private String timer   = "0.0s";
    private float  hp1     = 1.0f;  // 0.0 - 1.0
    private float  hp2     = 1.0f;

    public InfoPanel() {
        setPreferredSize(new Dimension(800, 50));
        setBackground(new Color(20, 20, 25));
        setOpaque(true);
    }

    /**
     * Called from the game loop (via SwingUtilities.invokeLater).
     * @param p1Info  player 1 name
     * @param score   "X - Y"
     * @param timeRemaining seconds left (or 0 for per-goals mode)
     * @param p2Info  player 2 name
     * @param hp1Pct  player 1 health 0.0-1.0
     * @param hp2Pct  player 2 health 0.0-1.0
     */
    public void updateMatchInfo(String p1Info, String score, float timeRemaining,
                                String p2Info, float hp1Pct, float hp2Pct) {
        this.p1Name = p1Info;
        this.p2Name = p2Info;
        this.score  = score;
        this.timer  = timeRemaining > 0f
                ? String.format("%.0fs", Math.ceil(timeRemaining))
                : "";
        this.hp1 = Math.max(0f, Math.min(1f, hp1Pct));
        this.hp2 = Math.max(0f, Math.min(1f, hp2Pct));
        repaint();
    }

    /** Legacy overload used by older callers (no health data). */
    public void updateMatchInfo(String p1Info, String score, float timeRemaining, String p2Info) {
        updateMatchInfo(p1Info, score, timeRemaining, p2Info, hp1, hp2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // ---- Layout constants ----
        int barW  = 220;
        int barH  = 14;
        int barY  = (h - barH) / 2 + 6;
        int nameY = barY - 2;

        int p1BarX = 10;
        int p2BarX = w - 10 - barW;

        // ---- Player 1 (left, bar grows right) ----
        g2.setFont(FONT_HUD);
        g2.setColor(Color.CYAN);
        g2.drawString(p1Name, p1BarX, nameY);

        drawBar(g2, p1BarX, barY + 4, barW, barH, hp1, false);

        // ---- Score + Timer (center) ----
        g2.setFont(FONT_SCORE);
        g2.setColor(Color.YELLOW);
        String center = timer.isEmpty() ? score : score + "  |  " + timer;
        int centerX = (w - g2.getFontMetrics().stringWidth(center)) / 2;
        g2.drawString(center, centerX, h - 10);

        // ---- Player 2 (right, bar grows left) ----
        g2.setFont(FONT_HUD);
        g2.setColor(Color.ORANGE);
        int p2NameX = p2BarX + barW - g2.getFontMetrics().stringWidth(p2Name);
        g2.drawString(p2Name, p2NameX, nameY);

        drawBar(g2, p2BarX, barY + 4, barW, barH, hp2, true);
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         float pct, boolean rightToLeft) {
        // Background
        g2.setColor(BAR_BG);
        g2.fillRoundRect(x, y, w, h, 6, 6);

        // Fill color based on percentage
        Color fill = pct > 0.50f ? BAR_HIGH : pct > 0.25f ? BAR_MID : BAR_LOW;
        g2.setColor(fill);

        int fillW = (int) (w * pct);
        if (fillW > 0) {
            if (rightToLeft) {
                g2.fillRoundRect(x + w - fillW, y, fillW, h, 6, 6);
            } else {
                g2.fillRoundRect(x, y, fillW, h, 6, 6);
            }
        }

        // Border
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x, y, w, h, 6, 6);
    }
}
