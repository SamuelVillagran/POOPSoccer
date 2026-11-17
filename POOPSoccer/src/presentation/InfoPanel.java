package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Top HUD bar: player identification (name + country), health bars,
 * power recharge bars, active power status, score and timer.
 */
public class InfoPanel extends JPanel {

    private static final Font FONT_HUD   = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_POWER = new Font("Arial", Font.PLAIN, 11);
    private static final Font FONT_SCORE = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_TIMER = new Font("Arial", Font.BOLD, 13);

    private static final Color BAR_BG         = new Color(45, 45, 55);
    private static final Color HP_HIGH        = new Color(50, 200, 50);   // > 50%
    private static final Color HP_MID         = new Color(220, 180, 0);   // 25-50%
    private static final Color HP_LOW         = new Color(200, 40, 40);   // <= 25%
    private static final Color POWER_CHARGING = new Color(0, 170, 255);   // charging
    private static final Color POWER_READY    = new Color(255, 215, 0);   // 100% full

    // State updated each frame from GamePanel
    private String p1Name       = "P1";
    private String p2Name       = "P2";
    private String score        = "0 - 0";
    private String timer        = "";
    private boolean suddenDeath = false;
    private float  hp1          = 1.0f;  // 0.0 - 1.0
    private float  hp2          = 1.0f;
    private float  power1       = 0.0f;  // 0.0 - 1.0
    private float  power2       = 0.0f;
    private String activePower1 = "Ninguno";
    private String activePower2 = "Ninguno";

    public InfoPanel() {
        setPreferredSize(new Dimension(800, 68));
        setBackground(new Color(20, 20, 25));
        setOpaque(true);
    }

    /**
     * Full HUD update with health, power recharge progress, sudden death status, and active power.
     */
    public void updateMatchInfo(String p1Info, String score, float timeRemaining, boolean suddenDeath,
                                String p2Info, float hp1Pct, float hp2Pct,
                                float pow1Pct, float pow2Pct,
                                String activePow1, String activePow2) {
        this.p1Name       = p1Info;
        this.p2Name       = p2Info;
        this.score        = score;
        this.suddenDeath  = suddenDeath;
        this.timer        = suddenDeath
                ? "¡MUERTE SÚBITA!"
                : (timeRemaining > 0f ? String.format("%.0fs", Math.ceil(timeRemaining)) : "");
        this.hp1          = clamp01(hp1Pct);
        this.hp2          = clamp01(hp2Pct);
        this.power1       = clamp01(pow1Pct);
        this.power2       = clamp01(pow2Pct);
        this.activePower1 = (activePow1 != null) ? activePow1 : "Ninguno";
        this.activePower2 = (activePow2 != null) ? activePow2 : "Ninguno";
        repaint();
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();

        // ---- Layout constants (proportional to current width) ----
        int barW   = Math.max(160, Math.min(240, (int) (w * 0.29)));
        int hpBarH = 11;
        int pwBarH = 9;

        int nameY  = 15;
        int hpBarY = 20;
        int pwBarY = 34;
        int powerTextY = 58;

        int p1BarX = 12;
        int p2BarX = w - 12 - barW;

        // ---- Player 1 (left, bars grow right) ----
        g2.setFont(FONT_HUD);
        g2.setColor(Color.CYAN);
        g2.drawString(p1Name, p1BarX, nameY);

        drawBar(g2, p1BarX, hpBarY, barW, hpBarH, hp1, resolveHpColor(hp1), false);
        drawBar(g2, p1BarX, pwBarY, barW, pwBarH, power1, resolvePowerColor(power1), false);

        g2.setFont(FONT_POWER);
        g2.setColor(resolvePowerTextColor(activePower1, power1));
        String p1PowerLabel = formatPowerLabel(activePower1, power1);
        g2.drawString(p1PowerLabel, p1BarX, powerTextY);

        // ---- Score + Timer (center) ----
        g2.setFont(FONT_SCORE);
        g2.setColor(Color.YELLOW);
        int scoreX = (w - g2.getFontMetrics().stringWidth(score)) / 2;
        g2.drawString(score, scoreX, 34);

        if (!timer.isEmpty()) {
            g2.setFont(FONT_TIMER);
            g2.setColor(suddenDeath ? Color.RED : Color.LIGHT_GRAY);
            String timerLabel = suddenDeath ? timer : "Tiempo: " + timer;
            int timerX = (w - g2.getFontMetrics().stringWidth(timerLabel)) / 2;
            g2.drawString(timerLabel, timerX, 55);
        }

        // ---- Player 2 (right, bars grow left) ----
        g2.setFont(FONT_HUD);
        g2.setColor(Color.ORANGE);
        int p2NameX = p2BarX + barW - g2.getFontMetrics().stringWidth(p2Name);
        g2.drawString(p2Name, p2NameX, nameY);

        drawBar(g2, p2BarX, hpBarY, barW, hpBarH, hp2, resolveHpColor(hp2), true);
        drawBar(g2, p2BarX, pwBarY, barW, pwBarH, power2, resolvePowerColor(power2), true);

        g2.setFont(FONT_POWER);
        g2.setColor(resolvePowerTextColor(activePower2, power2));
        String p2PowerLabel = formatPowerLabel(activePower2, power2);
        int p2PowerX = p2BarX + barW - g2.getFontMetrics().stringWidth(p2PowerLabel);
        g2.drawString(p2PowerLabel, p2PowerX, powerTextY);
    }

    private Color resolveHpColor(float pct) {
        return pct > 0.50f ? HP_HIGH : pct > 0.25f ? HP_MID : HP_LOW;
    }

    private Color resolvePowerColor(float pct) {
        return pct >= 0.999f ? POWER_READY : POWER_CHARGING;
    }

    private Color resolvePowerTextColor(String activePower, float powerPct) {
        if (!"Ninguno".equals(activePower) || powerPct >= 0.999f) {
            return POWER_READY;
        }
        return Color.LIGHT_GRAY;
    }

    private String formatPowerLabel(String activePower, float powerPct) {
        if (!"Ninguno".equals(activePower)) {
            return "Poder activo: " + activePower;
        }
        if (powerPct >= 0.999f) {
            return "Poder activo: Ninguno (¡Barra llena!)";
        }
        return "Poder activo: Ninguno";
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         float pct, Color fillColor, boolean rightToLeft) {
        // Background
        g2.setColor(BAR_BG);
        g2.fillRoundRect(x, y, w, h, 6, 6);

        // Fill
        g2.setColor(fillColor);
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
