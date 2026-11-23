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
import java.util.List;
import java.util.Objects;

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
public final class PauseDialog extends JDialog { // Implementado con Gemini Hydrogen 4
    /*Perfeccionado con Antigravity.*/

    static final Color BTN_RESTART = new Color(225, 110, 12); // Naranja vibrante para la opción de reinicio
    static final Color BTN_MENU = new Color(205, 42, 48); // Rojo intenso para salir al menú principal

    private Runnable selectedAction;

    private PauseDialog(Component owner, String scoreText, List<PauseOption> options, Runnable onCancel) {
        super(owner != null ? SwingUtilities.getWindowAncestor(owner) : null, ModalityType.APPLICATION_MODAL);
        this.selectedAction = onCancel;
        ArcadeUIStyle.configureModalDialog(this);
        setContentPane(buildCardPanel(scoreText, options));
        pack();
        setLocationRelativeTo(owner);
        registerEscapeKey(onCancel);
    }

    /**
     * Builds the pause dialog without displaying it yet so callers or tests can configure or inspect it.
     */
    public static PauseDialog createDialog(Component owner, String scoreText,
                                           List<PauseOption> options, Runnable onCancel) {
        return new PauseDialog(owner, scoreText, options, onCancel);
    }

    /**
     * Opens the modal pause menu centered on the owner component and executes the chosen action.
     */
    public static void showDialog(Component owner, String scoreText,
                                  List<PauseOption> options, Runnable onCancel) {
        PauseDialog dialog = createDialog(owner, scoreText, options, onCancel);
        dialog.setVisible(true);
        dialog.executeSelectedAction();
    }

    public void executeSelectedAction() {
        if (selectedAction != null) {
            selectedAction.run();
        }
    }

    private JPanel buildCardPanel(String scoreText, List<PauseOption> options) {
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
        List<PauseOption> safeOptions = options != null ? List.copyOf(options) : List.of();
        for (int i = 0; i < safeOptions.size(); i++) {
            gbc.gridy = 2 + i;
            card.add(createActionButton(safeOptions.get(i)), gbc);
        }
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

    private JButton createActionButton(PauseOption option) {
        JButton btn = new JButton(option.getLabel()) {
            @Override
            protected void paintComponent(Graphics g) {
                ArcadeUIStyle.paintArcadeButton(g, this, option.getColor(), 16);
                super.paintComponent(g);
            }
        };
        ArcadeUIStyle.configureCleanButton(btn, new Font("Arial", Font.BOLD, 15), new Dimension(228, 40));
        btn.addActionListener(e -> {
            selectedAction = option.getAction();
            dispose();
        });
        return btn;
    }

    private void registerEscapeKey(Runnable onCancel) {
        getRootPane().registerKeyboardAction(
                e -> {
                    selectedAction = onCancel;
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /**
     * Declarative option entry rendered as a button inside the pause menu.
     */
    public static final class PauseOption {

        private final String label;
        private final Color color;
        private final Runnable action;

        public PauseOption(String label, Color color, Runnable action) {
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Pause option label cannot be blank.");
            }
            if (color == null) {
                throw new IllegalArgumentException("Pause option color cannot be null.");
            }
            if (action == null) {
                throw new IllegalArgumentException("Pause option action cannot be null.");
            }
            this.label = label;
            this.color = color;
            this.action = action;
        }

        public String getLabel() {
            return label;
        }

        public Color getColor() {
            return color;
        }

        public Runnable getAction() {
            return action;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PauseOption other)) {
                return false;
            }
            return Objects.equals(label, other.label)
                    && Objects.equals(color, other.color)
                    && Objects.equals(action, other.action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label, color, action);
        }

        @Override
        public String toString() {
            return "PauseOption[label=" + label
                    + ", color=" + color
                    + ", action=" + action + "]";
        }
    }
}
