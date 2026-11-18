package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
 * Shared arcade graphics and button styling helpers used across presentation dialogs and menus.
 */
final class ArcadeUIStyle {

    static final Color CARD_BG = new Color(18, 28, 48, 242); // Azul marino oscuro traslúcido del fondo de tarjeta
    static final Color CARD_BORDER = new Color(245, 198, 45); // Borde dorado característico estilo Head Soccer
    static final Color SCORE_PILL_BG = new Color(10, 16, 28, 220); // Píldora oscura para destacar marcadores y puntajes
    static final Color BTN_GREEN = new Color(18, 168, 88); // Verde esmeralda para acciones principales de confirmación

    private ArcadeUIStyle() {
    }

    static void configureModalDialog(JDialog dialog) {
        dialog.setUndecorated(true); // Oculta la barra nativa del sistema operativo para lucir la tarjeta arcade
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        try {
            dialog.setBackground(new Color(0, 0, 0, 0)); // Activa transparencia alrededor de las esquinas redondeadas
        } catch (UnsupportedOperationException ignored) {
            dialog.setBackground(new Color(18, 28, 48)); // Respaldo sólido si el sistema operativo no soporta transparencia
        }
    }

    static void paintRoundedCard(Graphics g, int width, int height, int arc, float borderWidth) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Suaviza las curvas de bordes y esquinas
        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, width, height, arc, arc);
        g2.setColor(CARD_BORDER);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRoundRect(1, 1, width - 3, height - 3, arc, arc);
        g2.dispose();
    }

    static void paintPillSurface(Graphics g, int width, int height, int arc, Color bg) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, width, height, arc, arc);
        g2.dispose();
    }

    static Color resolveHoverFill(AbstractButton btn, Color baseColor) {
        if (btn.getModel().isPressed()) {
            return baseColor.darker(); // Oscurece el color mientras el botón permanece presionado
        }
        return btn.getModel().isRollover() ? baseColor.brighter() : baseColor; // Ilumina al pasar el cursor encima
    }

    static void paintArcadeButton(Graphics g, AbstractButton btn, Color baseColor, int arc) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(resolveHoverFill(btn, baseColor));
        g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), arc, arc);
        g2.setColor(new Color(255, 255, 255, 130)); // Brillo interno sutil alrededor del botón redondeado
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawRoundRect(1, 1, btn.getWidth() - 3, btn.getHeight() - 3, arc, arc);
        g2.dispose();
    }

    static void configureCleanButton(JButton btn, Font font, Dimension size) {
        btn.setFont(font);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(size);
        btn.setContentAreaFilled(false); // Desactiva el fondo metálico predeterminado de Swing
        btn.setBorderPainted(false);
        btn.setFocusPainted(false); // Oculta el rectángulo punteado de foco estándar
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
