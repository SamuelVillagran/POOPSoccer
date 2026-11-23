package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;

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
 * Arcade-styled modal dialog shown when a match ends, displaying the final score and points summary.
 */
public final class MatchSummaryDialog extends JDialog { // Implementado con Gemini Hydrogen 4
    /*Perfeccionado con Antigravity.*/

    private static final Color ROW_BG = new Color(26, 38, 62); // Fondo azul pizarra oscuro de las tarjetas de resumen
    private static final Color LOSER_BORDER = new Color(95, 130, 175); // Borde azul acero suave para el segundo puesto o empate

    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 21);
    private static final Font FONT_SCORE = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_WINNER = new Font("Arial", Font.BOLD, 13);
    private static final Font FONT_RUNNER_UP = new Font("Arial", Font.PLAIN, 13);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 15);

    private MatchSummaryDialog(Component owner, MatchResultSummary summary) {
        super(SwingUtilities.getWindowAncestor(owner), ModalityType.APPLICATION_MODAL);
        ArcadeUIStyle.configureModalDialog(this);
        setContentPane(buildCardPanel(summary));
        pack();
        setLocationRelativeTo(owner);
        registerCloseKeys();
    }

    /**
     * Opens the modal match summary dialog centered on the owner component and waits until dismissed.
     */
    public static void showDialog(Component owner, MatchResultSummary summary) {
        MatchSummaryDialog dialog = new MatchSummaryDialog(owner, summary);
        dialog.setVisible(true);
    }

    private JPanel buildCardPanel(MatchResultSummary summary) {
        JPanel card = createRoundedCardContainer();
        GridBagConstraints gbc = createBaseConstraints();

        gbc.gridy = 0;
        card.add(buildHeader(summary.isDraw()), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 14, 0);
        card.add(buildScoreBadge(summary.getGoalsP1() + " - " + summary.getGoalsP2()), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(buildOutcomeRowsPanel(summary), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(2, 0, 0, 0);
        card.add(wrapCenteredButton(createAcceptButton()), gbc);
        return card;
    }

    private JPanel createRoundedCardContainer() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                ArcadeUIStyle.paintRoundedCard(g, getWidth(), getHeight(), 26, 2.8f);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        return card;
    }

    private GridBagConstraints createBaseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 6, 0);
        return gbc;
    }

    private JPanel buildHeader(boolean isDraw) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        header.setOpaque(false);
        if (!isDraw) {
            header.add(createTrophyIconPanel(24)); // Muestra el trofeo dorado únicamente cuando existe un ganador
        }

        JLabel title = new JLabel("FIN DEL PARTIDO");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title);
        return header;
    }

    private JPanel buildScoreBadge(String scoreText) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);

        JLabel badge = new JLabel("Marcador: " + scoreText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                ArcadeUIStyle.paintPillSurface(g, getWidth(), getHeight(), 18, ArcadeUIStyle.SCORE_PILL_BG);
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SCORE);
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(5, 18, 5, 18));
        holder.add(badge);
        return holder;
    }

    private JPanel buildOutcomeRowsPanel(MatchResultSummary summary) {
        JPanel rows = new JPanel(new GridLayout(0, 1, 0, 10));
        rows.setOpaque(false);
        if (summary.isDraw()) {
            rows.add(buildResultRowCard("¡EMPATE!", false, false)); // Tarjeta única neutra en caso de empate
            return rows;
        }
        rows.add(buildResultRowCard(formatWinnerText(summary), true, true));
        rows.add(buildResultRowCard(formatLoserText(summary), false, false));
        return rows;
    }

    private String formatWinnerText(MatchResultSummary s) {
        return "¡Ganador: " + s.getWinnerName() + " (+" + s.getWinnerPointsAwarded()
                + " pts | Total: " + s.getWinnerTotalPoints() + " pts)!";
    }

    private String formatLoserText(MatchResultSummary s) {
        return "Perdedor: " + s.getLoserName() + " (+" + s.getLoserPointsAwarded()
                + " pts | Total: " + s.getLoserTotalPoints() + " pts)";
    }

    private JPanel buildResultRowCard(String lineText, boolean highlightBorder, boolean showTrophy) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 9)) {
            @Override
            protected void paintComponent(Graphics g) {
                paintResultRowSurface(g, getWidth(), getHeight(), highlightBorder);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        if (showTrophy) {
            row.add(createTrophyIconPanel(18));
        }
        row.add(createResultRowLabel(lineText, highlightBorder));
        return row;
    }

    private void paintResultRowSurface(Graphics g, int width, int height, boolean highlightBorder) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ROW_BG);
        g2.fillRoundRect(0, 0, width, height, 14, 14);
        g2.setColor(highlightBorder ? ArcadeUIStyle.CARD_BORDER : LOSER_BORDER);
        g2.setStroke(new BasicStroke(highlightBorder ? 1.8f : 1.2f));
        g2.drawRoundRect(1, 1, width - 3, height - 3, 14, 14);
        g2.dispose();
    }

    private JLabel createResultRowLabel(String lineText, boolean highlightText) {
        JLabel label = new JLabel(lineText, SwingConstants.CENTER);
        label.setFont(highlightText ? FONT_WINNER : FONT_RUNNER_UP);
        label.setForeground(highlightText ? Color.WHITE : new Color(215, 224, 238));
        return label;
    }

    private JPanel createTrophyIconPanel(int size) {
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                paintVectorTrophy(g, getWidth(), getHeight());
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(size, size));
        return icon;
    }

    private void paintVectorTrophy(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.scale(width / 24.0, height / 24.0); // Escala las coordenadas vectoriales de 24x24 al tamaño pedido
        g2.setColor(ArcadeUIStyle.CARD_BORDER);

        Path2D cup = new Path2D.Float();
        cup.moveTo(6, 4);
        cup.lineTo(18, 4);
        cup.lineTo(16.5, 12.5);
        cup.quadTo(12, 16, 7.5, 12.5);
        cup.closePath();
        g2.fill(cup);

        g2.fillRect(10, 14, 4, 4); // Pedestal del trofeo
        g2.fillRoundRect(7, 18, 10, 3, 2, 2); // Base rectangular redondeada

        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(3, 5, 4, 6, 90, 180); // Asa izquierda de la copa
        g2.drawArc(17, 5, 4, 6, -90, 180); // Asa derecha de la copa
        g2.dispose();
    }

    private JPanel wrapCenteredButton(JButton btn) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);
        holder.add(btn);
        return holder;
    }

    private JButton createAcceptButton() {
        JButton btn = new JButton("Aceptar") {
            @Override
            protected void paintComponent(Graphics g) {
                ArcadeUIStyle.paintArcadeButton(g, this, ArcadeUIStyle.BTN_GREEN, 14);
                super.paintComponent(g);
            }
        };
        ArcadeUIStyle.configureCleanButton(btn, FONT_BUTTON, new Dimension(148, 38));
        btn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btn); // Permite confirmar con la tecla Enter directamente
        return btn;
    }

    private void registerCloseKeys() {
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW // Permite cerrar rápidamente con la tecla Escape
        );
    }
}
