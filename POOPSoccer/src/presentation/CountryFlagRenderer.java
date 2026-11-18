package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Renders crisp vectorized country flags for the HUD without relying on external image assets.
 */
public final class CountryFlagRenderer {

    private static final Color FLAG_BORDER = new Color(255, 255, 255, 165);

    private CountryFlagRenderer() {
    }

    /**
     * Draws the flag matching the given country name inside the specified bounds.
     */
    public static void drawFlag(Graphics2D g2, String teamName, int x, int y, int width, int height) {
        String key = normalize(teamName);
        Graphics2D fg = (Graphics2D) g2.create();
        Shape previousClip = fg.getClip();
        RoundRectangle2D bounds = new RoundRectangle2D.Float(x, y, width, height, 4, 4);
        fg.clip(bounds);

        switch (key) {
            case "coreadelsur" -> drawSouthKorea(fg, x, y, width, height);
            case "camerun" -> drawCameroon(fg, x, y, width, height);
            case "nigeria" -> drawNigeria(fg, x, y, width, height);
            case "estadosunidos" -> drawUsa(fg, x, y, width, height);
            case "colombia" -> drawColombia(fg, x, y, width, height);
            default -> drawFallbackFlag(fg, x, y, width, height);
        }

        fg.setClip(previousClip);
        fg.setColor(FLAG_BORDER);
        fg.setStroke(new BasicStroke(1.0f));
        fg.draw(bounds);
        fg.dispose();
    }

    private static void drawSouthKorea(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, w, h);

        int d = Math.min(w, h) * 5 / 9;
        int cx = x + (w - d) / 2;
        int cy = y + (h - d) / 2;
        g2.setColor(new Color(205, 46, 58));
        g2.fill(new Arc2D.Float(cx, cy, d, d, 0, 180, Arc2D.PIE));
        g2.setColor(new Color(0, 71, 160));
        g2.fill(new Arc2D.Float(cx, cy, d, d, 180, 180, Arc2D.PIE));

        g2.setColor(new Color(30, 30, 35));
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawLine(x + 3, y + 3, x + 6, y + 5);
        g2.drawLine(x + w - 6, y + 5, x + w - 3, y + 3);
        g2.drawLine(x + 3, y + h - 3, x + 6, y + h - 5);
        g2.drawLine(x + w - 6, y + h - 5, x + w - 3, y + h - 3);
    }

    private static void drawCameroon(Graphics2D g2, int x, int y, int w, int h) {
        int third = w / 3;
        g2.setColor(new Color(0, 122, 94));
        g2.fillRect(x, y, third, h);
        g2.setColor(new Color(206, 17, 38));
        g2.fillRect(x + third, y, third, h);
        g2.setColor(new Color(252, 209, 22));
        g2.fillRect(x + third * 2, y, w - third * 2, h);
        drawCenteredStar(g2, x + w / 2.0, y + h / 2.0, Math.min(w, h) * 0.20);
    }

    private static void drawNigeria(Graphics2D g2, int x, int y, int w, int h) {
        int third = w / 3;
        g2.setColor(new Color(0, 135, 81));
        g2.fillRect(x, y, third, h);
        g2.setColor(Color.WHITE);
        g2.fillRect(x + third, y, third, h);
        g2.setColor(new Color(0, 135, 81));
        g2.fillRect(x + third * 2, y, w - third * 2, h);
    }

    private static void drawUsa(Graphics2D g2, int x, int y, int w, int h) {
        int stripes = 7;
        double stripeH = (double) h / stripes;
        for (int i = 0; i < stripes; i++) {
            g2.setColor(i % 2 == 0 ? new Color(178, 34, 52) : Color.WHITE);
            int sy = y + (int) Math.round(i * stripeH);
            int nextY = y + (int) Math.round((i + 1) * stripeH);
            g2.fillRect(x, sy, w, Math.max(1, nextY - sy));
        }
        int cantonW = (int) Math.round(w * 0.44);
        int cantonH = (int) Math.round(h * 0.56);
        g2.setColor(new Color(60, 59, 110));
        g2.fillRect(x, y, cantonW, cantonH);
        g2.setColor(Color.WHITE);
        for (int row = 2; row < cantonH - 1; row += 3) {
            for (int col = 2; col < cantonW - 1; col += 3) {
                g2.fillRect(x + col, y + row, 1, 1);
            }
        }
    }

    private static void drawColombia(Graphics2D g2, int x, int y, int w, int h) {
        int topH = h / 2;
        int midH = h / 4;
        g2.setColor(new Color(252, 209, 22));
        g2.fillRect(x, y, w, topH);
        g2.setColor(new Color(0, 56, 147));
        g2.fillRect(x, y + topH, w, midH);
        g2.setColor(new Color(206, 17, 38));
        g2.fillRect(x, y + topH + midH, w, h - topH - midH);
    }

    private static void drawFallbackFlag(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(70, 88, 120));
        g2.fillRect(x, y, w, h);
    }

    private static void drawCenteredStar(Graphics2D g2, double cx, double cy, double outerRadius) {
        double innerRadius = outerRadius * 0.42;
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * (Math.PI / 5);
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double px = cx + Math.cos(angle) * r;
            double py = cy + Math.sin(angle) * r;
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.closePath();
        g2.setColor(new Color(252, 209, 22));
        g2.fill(star);
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}|\\s+|_", "")
                .toLowerCase(Locale.ROOT);
    }
}
