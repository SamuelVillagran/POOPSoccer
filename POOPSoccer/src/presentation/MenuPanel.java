package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Main menu screen where the user selects the game mode.
 */
public class MenuPanel extends JPanel {

    private JButton btnPvP;
    private JButton btnPvM;
    private JButton btnMvM;
    private BufferedImage background;

    public MenuPanel(GameContainer container) {
        loadBackground();
        prepareElements();
        prepareActions(container);
    }

    private void loadBackground() {
        try {
            InputStream is = getClass().getResourceAsStream("/menu/main.jpg");
            if (is != null) {
                background = ImageIO.read(is);
            }
        } catch (Exception e) {
            background = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
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
        // Envía el identificador de la modalidad al contenedor sin acoplarse a enums del dominio
        btnPvP.addActionListener(e -> container.onModeSelected("PVP"));
        btnPvM.addActionListener(e -> container.onModeSelected("PVM"));
        btnMvM.addActionListener(e -> container.onModeSelected("MVM"));
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
