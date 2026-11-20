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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Main menu screen where the user selects the game mode.
 */
public class MenuPanel extends JPanel { /*Perfeccionado con Antigravity.*/

    private BufferedImage background; // Ilustración principal de fondo cargada desde /menu/main.jpg

    public MenuPanel(MenuPanelListener listener, List<GameModeOption> modeOptions) {
        loadBackground();
        prepareElements(listener, modeOptions);
    }

    private void loadBackground() {
        try (InputStream is = getClass().getResourceAsStream("/menu/main.jpg")) {
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

    private void prepareElements(MenuPanelListener listener, List<GameModeOption> modeOptions) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 60, 14, 60);

        List<GameModeOption> safeModes = modeOptions != null ? List.copyOf(modeOptions) : List.of();
        for (int i = 0; i < safeModes.size(); i++) {
            GameModeOption option = safeModes.get(i);
            JButton modeButton = createButton(option.displayName());
            if (listener != null) {
                modeButton.addActionListener(e -> listener.onModeSelected(option));
            }
            gbc.gridy = i;
            add(modeButton, gbc);
        }
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
