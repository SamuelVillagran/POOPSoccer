package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Arcade-styled modal pause dialog shown over the pitch when pressing Escape.
 */
public class PauseDialog extends JDialog { // Implementado con Gemini Hydrogen 4

    public enum Choice {
        RESUME,
        RESTART,
        MENU
    }

    private static final Color CARD_BG = new Color(18, 28, 48, 242);
    private static final Color CARD_BORDER = new Color(245, 198, 45);
    private static final Color SCORE_PILL_BG = new Color(10, 16, 28, 220);
    private static final Color BTN_RESUME = new Color(18, 168, 88);
    private static final Color BTN_RESTART = new Color(225, 110, 12);
    private static final Color BTN_MENU = new Color(205, 42, 48);

    private Choice selectedChoice = Choice.RESUME;

    private PauseDialog(Component owner, String scoreText) {
        super(SwingUtilities.getWindowAncestor(owner), ModalityType.APPLICATION_MODAL);
        configureWindow();
        setContentPane(buildCardPanel(scoreText));
        pack();
        setLocationRelativeTo(owner);
        registerEscapeKey();
    }

    /**
     * Opens the modal pause menu centered on the owner component and returns the user's choice.
     */
    public static Choice showDialog(Component owner, String scoreText) {
        PauseDialog dialog = new PauseDialog(owner, scoreText);
        dialog.setVisible(true);
        return dialog.selectedChoice;
    }

    private void configureWindow() {
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // Aplica transparencia por píxel solo si la plataforma lo soporta
        try {
            setBackground(new Color(0, 0, 0, 0));
        } catch (UnsupportedOperationException ignored) {
            setBackground(new Color(18, 28, 48));
        }
    }

    private JPanel buildCardPanel(String scoreText) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 26, 26);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 26, 22, 26));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 8, 0);

        JLabel titleLbl = new JLabel("JUEGO EN PAUSA", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 21));
        titleLbl.setForeground(Color.WHITE);
        gbc.gridy = 0;
        card.add(titleLbl, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(buildScoreBadge(scoreText), gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 2;
        card.add(createActionButton("Continuar", BTN_RESUME, Choice.RESUME), gbc);
        gbc.gridy = 3;
        card.add(createActionButton("Reiniciar", BTN_RESTART, Choice.RESTART), gbc);
        gbc.gridy = 4;
        card.add(createActionButton("Volver al menú", BTN_MENU, Choice.MENU), gbc);
        return card;
    }

    private JPanel buildScoreBadge(String scoreText) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);

        JLabel badge = new JLabel(scoreText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SCORE_PILL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Arial", Font.BOLD, 16));
        badge.setForeground(Color.WHITE);
        badge.setPreferredSize(new Dimension(92, 30));
        holder.add(badge);
        return holder;
    }

    private JButton createActionButton(String text, Color baseColor, Choice choice) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                // Dibuja el botón estilo arcade con brillo al pasar el cursor
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? baseColor.darker()
                        : getModel().isRollover() ? baseColor.brighter() : baseColor;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 110));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(228, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            selectedChoice = choice;
            dispose();
        });
        return btn;
    }

    private void registerEscapeKey() {
        // Permite reanudar rápidamente presionando Escape nuevamente dentro del diálogo
        getRootPane().registerKeyboardAction(
                e -> {
                    selectedChoice = Choice.RESUME;
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
}
