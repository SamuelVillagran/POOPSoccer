package presentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
        RESUME, // Reanuda el partido desde el punto exacto donde se pausó
        RESTART, // Reinicia el marcador y vuelve a lanzar el partido actual
        MENU // Abandona la cancha actual y regresa al menú principal
    }

    private static final Color BTN_RESTART = new Color(225, 110, 12); // Naranja vibrante para la opción de reinicio
    private static final Color BTN_MENU = new Color(205, 42, 48); // Rojo intenso para salir al menú principal

    private Choice selectedChoice = Choice.RESUME; // Valor seguro por defecto si el diálogo se cierra con Escape

    private PauseDialog(Component owner, String scoreText) {
        super(SwingUtilities.getWindowAncestor(owner), ModalityType.APPLICATION_MODAL);
        ArcadeUIStyle.configureModalDialog(this);
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

    private JPanel buildCardPanel(String scoreText) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                ArcadeUIStyle.paintRoundedCard(g, getWidth(), getHeight(), 26, 3.0f);
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
        card.add(createActionButton("Continuar", ArcadeUIStyle.BTN_GREEN, Choice.RESUME), gbc);
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
                ArcadeUIStyle.paintPillSurface(g, getWidth(), getHeight(), 16, ArcadeUIStyle.SCORE_PILL_BG);
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
                ArcadeUIStyle.paintArcadeButton(g, this, baseColor, 16);
                super.paintComponent(g);
            }
        };
        ArcadeUIStyle.configureCleanButton(btn, new Font("Arial", Font.BOLD, 15), new Dimension(228, 40));
        btn.addActionListener(e -> {
            selectedChoice = choice; // Registra la elección pulsada y cierra el diálogo modal
            dispose();
        });
        return btn;
    }

    private void registerEscapeKey() {
        getRootPane().registerKeyboardAction(
                e -> {
                    selectedChoice = Choice.RESUME; // Reanuda al instante si se presiona Escape por segunda vez
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
}
