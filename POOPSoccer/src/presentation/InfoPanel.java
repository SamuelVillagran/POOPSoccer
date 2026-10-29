package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Barra superior de estado (HUD / Marcador del partido).
 */
public class InfoPanel extends JPanel {

    private static final Font FONT_HUD = new Font("Arial", Font.BOLD, 15);

    private JLabel lblPlayer1;
    private JLabel lblScore;
    private JLabel lblTimer;
    private JLabel lblPlayer2;

    public InfoPanel() {
        setPreferredSize(new Dimension(800, 50));
        setBackground(new Color(25, 25, 30));
        setLayout(new FlowLayout(FlowLayout.CENTER, 35, 12));

        lblPlayer1 = makeLabel("Jugador 1: 0", Color.CYAN);
        lblScore = makeLabel("Marcador: 0 - 0", Color.YELLOW);
        lblTimer = makeLabel("Tiempo: 60s", Color.WHITE);
        lblPlayer2 = makeLabel("Jugador 2: 0", Color.ORANGE);

        add(lblPlayer1);
        add(lblScore);
        add(lblTimer);
        add(lblPlayer2);
    }

    private JLabel makeLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(FONT_HUD);
        return label;
    }

    public void updateMatchInfo(String p1Info, String score, float timeRemaining, String p2Info) {
        lblPlayer1.setText(p1Info);
        lblScore.setText(score);
        lblTimer.setText(String.format("Tiempo: %.1fs", Math.max(timeRemaining, 0f)));
        lblPlayer2.setText(p2Info);
    }
}
