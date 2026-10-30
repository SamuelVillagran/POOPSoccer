package presentation;

import domain.GameMode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Pantalla del Menú Principal.
 */
public class MenuPanel extends JPanel {

    private JButton btnPvP;
    private JButton btnPvM;
    private JButton btnMvM;

    public MenuPanel(GameContainer container) {
        prepareElements();
        prepareActions(container);
    }

    private void prepareElements() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 60, 14, 60);

        btnPvP = createButton("Jugador vs Jugador (PvP)");
        btnPvM = createButton("Jugador vs Máquina (PvM)");
        btnMvM = createButton("Máquina vs Máquina (MvM)");

        gbc.gridy = 0;
        add(btnPvP, gbc);
        gbc.gridy = 1;
        add(btnPvM, gbc);
        gbc.gridy = 2;
        add(btnMvM, gbc);
    }

    private void prepareActions(GameContainer container) {
        btnPvP.addActionListener(e -> container.onModeSelected(GameMode.PVP));
        btnPvM.addActionListener(e -> container.onModeSelected(GameMode.PVM));
        btnMvM.addActionListener(e -> container.onModeSelected(GameMode.MVM));
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 17));
        btn.setBackground(new Color(150, 170, 225));
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(300, 52));
        return btn;
    }
}
