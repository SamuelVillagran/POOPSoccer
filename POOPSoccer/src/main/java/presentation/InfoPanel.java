package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Top HUD bar: player flags, names, stats, health bars, power recharge bars, active power, score, and timer.
 */
public class InfoPanel extends JPanel { /*Perfeccionado con Antigravity.*/

    private static final int BASE_WIDTH = 800; // Ancho lógico de referencia usado para escalar proporcionalmente
    private static final int BASE_HEIGHT = 80; // Alto lógico suficiente para bandera, barras, poder y estadísticas
    private static final int FLAG_WIDTH = 22; // Ancho en píxeles de cada bandera junto al nombre
    private static final int FLAG_HEIGHT = 14; // Alto en píxeles de cada bandera junto al nombre

    private static final Font FONT_HUD = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_POWER = new Font("Arial", Font.PLAIN, 11);
    private static final Font FONT_STATS = new Font("Arial", Font.BOLD, 11);
    private static final Font FONT_SCORE = new Font("Arial", Font.BOLD, 22);
    private static final Font FONT_TIMER = new Font("Arial", Font.BOLD, 13);

    private static final Color BAR_BG = new Color(45, 45, 55); // Fondo gris oscuro de las barras vacías
    private static final Color HP_HIGH = new Color(50, 200, 50); // Verde cuando la vida supera el 50%
    private static final Color HP_MID = new Color(220, 180, 0); // Amarillo cuando la vida está entre 25% y 50%
    private static final Color HP_LOW = new Color(200, 40, 40); // Rojo de alerta cuando queda 25% o menos de vida
    private static final Color POWER_CHARGING = new Color(0, 170, 255); // Azul celeste mientras se recarga el poder
    private static final Color POWER_READY = new Color(255, 215, 0); // Dorado brillante cuando la barra está al 100%
    private static final Color STATS_COLOR = new Color(178, 202, 232); // Tono claro para la línea compacta de estadísticas
    private static final String NO_ACTIVE_POWER_LABEL = "Ninguno"; // Texto de presentación cuando no hay poder activo

    private MatchHudSnapshot snapshot = new MatchHudSnapshot( // Estado inicial neutro mientras comienza el primer frame
            new PlayerHudInfo("P1", "", 1.0f, 0.0f, "", ""),
            new PlayerHudInfo("P2", "", 1.0f, 0.0f, "", ""),
            "0 - 0",
            0f,
            false
    );

    protected InfoPanel() {
    }

    public static InfoPanel create() {
        InfoPanel panel = new InfoPanel();
        panel.initializeView();
        return panel;
    }

    protected void initializeView() {
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setBackground(new Color(20, 20, 25));
        setOpaque(true);
    }

    /**
     * Updates the HUD with the latest immutable match snapshot and repaints the bar.
     */
    public void updateMatchInfo(MatchHudSnapshot hud) {
        if (hud == null) {
            return;
        }
        this.snapshot = hud;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.scale((double) getWidth() / BASE_WIDTH, (double) getHeight() / BASE_HEIGHT); // Adapta el HUD al ancho actual de la ventana

        int barW = Math.max(160, Math.min(240, (int) (BASE_WIDTH * 0.29))); // Reserva el 29% lateral para las barras de cada jugador
        int p1BarX = 12;
        int p2BarX = BASE_WIDTH - 12 - barW;

        drawPlayerHud(g2, snapshot.getPlayer1(), p1BarX, barW, Color.CYAN, false);
        drawCenterScoreAndTimer(g2);
        drawPlayerHud(g2, snapshot.getPlayer2(), p2BarX, barW, Color.ORANGE, true);
        g2.dispose();
    }

    private void drawPlayerHud(Graphics2D g2, PlayerHudInfo info, int barX, int barW,
                               Color nameColor, boolean rightAligned) {
        drawPlayerIdentityWithFlag(g2, info, barX, barW, nameColor, rightAligned);
        drawBar(g2, barX, 23, barW, 10, clamp01(info.getHealthPercent()), resolveHpColor(info.getHealthPercent()), rightAligned);
        drawBar(g2, barX, 36, barW, 8, clamp01(info.getPowerPercent()), resolvePowerColor(info.getPowerPercent()), rightAligned);
        drawPowerStatusText(g2, info, barX, barW, rightAligned);
        drawStatsSummary(g2, info.getStatsSummary(), barX, barW, rightAligned);
    }

    private void drawPlayerIdentityWithFlag(Graphics2D g2, PlayerHudInfo info, int barX, int barW,
                                            Color nameColor, boolean rightAligned) {
        g2.setFont(FONT_HUD);
        String label = info.getDisplayName() != null ? info.getDisplayName() : "";
        int textWidth = g2.getFontMetrics().stringWidth(label);
        int flagY = 5;
        int textY = 16;

        String flagKey = (info.getFlagKey() != null && !info.getFlagKey().isBlank())
                ? info.getFlagKey()
                : info.getTeamName();

        if (rightAligned) {
            int flagX = barX + barW - FLAG_WIDTH; // Ubica la bandera pegada al borde derecho para Jugador 2
            CountryFlagRenderer.drawFlag(g2, flagKey, flagX, flagY, FLAG_WIDTH, FLAG_HEIGHT);
            g2.setColor(nameColor);
            g2.drawString(label, flagX - 6 - textWidth, textY);
            return;
        }
        CountryFlagRenderer.drawFlag(g2, flagKey, barX, flagY, FLAG_WIDTH, FLAG_HEIGHT);
        g2.setColor(nameColor);
        g2.drawString(label, barX + FLAG_WIDTH + 6, textY);
    }

    private void drawPowerStatusText(Graphics2D g2, PlayerHudInfo info, int barX, int barW, boolean rightAligned) {
        g2.setFont(FONT_POWER);
        float pow = clamp01(info.getPowerPercent());
        String active = info.getActivePower() != null ? info.getActivePower() : "";
        g2.setColor(resolvePowerTextColor(active, pow));
        String label = formatPowerLabel(active, pow);
        int x = rightAligned ? barX + barW - g2.getFontMetrics().stringWidth(label) : barX;
        g2.drawString(label, x, 57);
    }

    private void drawStatsSummary(Graphics2D g2, String statsSummary, int barX, int barW, boolean rightAligned) {
        if (statsSummary == null || statsSummary.isEmpty()) {
            return;
        }
        g2.setFont(FONT_STATS);
        g2.setColor(STATS_COLOR);
        int x = rightAligned ? barX + barW - g2.getFontMetrics().stringWidth(statsSummary) : barX;
        g2.drawString(statsSummary, x, 72);
    }

    private void drawCenterScoreAndTimer(Graphics2D g2) {
        g2.setFont(FONT_SCORE);
        g2.setColor(Color.YELLOW);
        String scoreText = snapshot.getScore() != null ? snapshot.getScore() : "0 - 0";
        int scoreX = (BASE_WIDTH - g2.getFontMetrics().stringWidth(scoreText)) / 2;
        g2.drawString(scoreText, scoreX, 36);

        String timerLabel = formatTimerLabel(snapshot.getTimeRemaining(), snapshot.isSuddenDeath());
        if (!timerLabel.isEmpty()) {
            g2.setFont(FONT_TIMER);
            g2.setColor(snapshot.isSuddenDeath() ? Color.RED : Color.LIGHT_GRAY);
            int timerX = (BASE_WIDTH - g2.getFontMetrics().stringWidth(timerLabel)) / 2;
            g2.drawString(timerLabel, timerX, 58);
        }
    }

    private String formatTimerLabel(float timeRemaining, boolean suddenDeath) {
        if (suddenDeath) {
            return "¡MUERTE SÚBITA!";
        }
        if (timeRemaining > 0f) {
            return "Tiempo: " + String.format("%.0fs", Math.ceil(timeRemaining));
        }
        return "";
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value)); // Acota porcentajes entre 0.0 y 1.0 para evitar desbordes gráficos
    }

    private Color resolveHpColor(float pct) {
        return pct > 0.50f ? HP_HIGH : pct > 0.25f ? HP_MID : HP_LOW;
    }

    private Color resolvePowerColor(float pct) {
        return pct >= 0.999f ? POWER_READY : POWER_CHARGING;
    }

    private boolean hasActivePower(String activePower) {
        return activePower != null && !activePower.isBlank() && !NO_ACTIVE_POWER_LABEL.equals(activePower);
    }

    private Color resolvePowerTextColor(String activePower, float powerPct) {
        if (hasActivePower(activePower) || powerPct >= 0.999f) {
            return POWER_READY;
        }
        return Color.LIGHT_GRAY;
    }

    private String formatPowerLabel(String activePower, float powerPct) {
        if (hasActivePower(activePower)) {
            return "Poder activo: " + activePower;
        }
        if (powerPct >= 0.999f) {
            return "Poder activo: " + NO_ACTIVE_POWER_LABEL + " (¡Barra llena!)";
        }
        return "Poder activo: " + NO_ACTIVE_POWER_LABEL;
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         float pct, Color fillColor, boolean rightToLeft) {
        g2.setColor(BAR_BG);
        g2.fillRoundRect(x, y, w, h, 6, 6);

        g2.setColor(fillColor);
        int fillW = (int) (w * pct);
        if (fillW > 0) {
            int fillX = rightToLeft ? x + w - fillW : x; // Rellena desde la derecha cuando pertenece al Jugador 2
            g2.fillRoundRect(fillX, y, fillW, h, 6, 6);
        }
    }
}
