package presentation;

import domain.GameMode;
import domain.MatchType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**Ayudado a organizar con Claude Sonnet 4.6
 * Pantalla de ConfiguraciÃ³n de Jugadores y Tipo de Partido.
 */
public class PlayerConfig extends JPanel {

    private GameMode mode;
    private GameContainer container;
    private JTextField nameFieldP1;
    private JComboBox<String> countryComboP1;
    private JTextField nameFieldP2;
    private JComboBox<String> countryComboP2;

    // ConfiguraciÃ³n del tipo de partido
    private JComboBox<MatchType> matchTypeCombo;
    private JComboBox<Integer> goalLimitCombo;
    private JComboBox<Integer> timeLimitCombo;

    private JButton startBtn;
    private BufferedImage background;

    public PlayerConfig(GameContainer container, GameMode mode) {
        this.mode = mode;
        this.container = container;
        loadBackground();
        prepareView();
        prepareController();
    }

    private void loadBackground() {
        try {
            InputStream is = getClass().getResourceAsStream("/menu/playerconfig.jpg");
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

    private void prepareView() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.setBackground(new Color(255, 255, 255, 120));

        // 1. ConfiguraciÃ³n de Jugadores
        prepareViewSettingPlayer(formPanel);

        // 2. ConfiguraciÃ³n del Partido (Por Goles vs Supervivencia)
        prepareViewMatchSettings(formPanel);

        add(formPanel, BorderLayout.CENTER);

        // 3. BotÃ³n Iniciar
        prepareViewStartButton();
    }

    private void prepareViewSettingPlayer(JPanel formPanel) {
        JLabel titleP1 = new JLabel("ConfiguraciÃ³n Jugador 1:");
        titleP1.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleP1);

        nameFieldP1 = new JTextField("Jugador 1");
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP1);

        String[] possibleCountries = java.util.Arrays.stream(domain.Team.values())
                .map(domain.Team::getDisplayName)
                .toArray(String[]::new);
        countryComboP1 = new JComboBox<>(possibleCountries);
        formPanel.add(new JLabel("PaÃ­s / Poder:"));
        formPanel.add(countryComboP1);

        // Si es PvP o MvM, agregar configuraciÃ³n de Jugador 2
        if (mode == GameMode.PVP || mode == GameMode.MVM) {
            JLabel titleP2 = new JLabel("ConfiguraciÃ³n Jugador 2:");
            titleP2.setFont(new Font("Arial", Font.BOLD, 15));
            formPanel.add(titleP2);

            nameFieldP2 = new JTextField(mode == GameMode.MVM ? "MÃ¡quina 2" : "Jugador 2");
            formPanel.add(new JLabel("Nombre:"));
            formPanel.add(nameFieldP2);

            countryComboP2 = new JComboBox<>(possibleCountries);
            formPanel.add(new JLabel("PaÃ­s / Poder:"));
            formPanel.add(countryComboP2);
        }
    }

    private void prepareViewMatchSettings(JPanel formPanel) {
        JLabel titleMatch = new JLabel("Configuracion del Partido:");
        titleMatch.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleMatch);

        // Both modes support per-goals and per-time
        matchTypeCombo = new JComboBox<>(new MatchType[] { MatchType.PER_GOALS, MatchType.PER_TIME });
        formPanel.add(new JLabel("Tipo de partido:"));
        formPanel.add(matchTypeCombo);

        // Goal limit (3, 5 or 7) - only used for PER_GOALS
        goalLimitCombo = new JComboBox<>(new Integer[] { 3, 5, 7 });
        formPanel.add(new JLabel("Goles para ganar (si es por goles):"));
        formPanel.add(goalLimitCombo);

        // Time limit in minutes - only used for PER_TIME
        timeLimitCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 5 });
        timeLimitCombo.setVisible(false);
        formPanel.add(new JLabel("Minutos (si es por tiempo):"));
        formPanel.add(timeLimitCombo);
    }

    private void prepareViewStartButton() {
        startBtn = new JButton("Iniciar Partido");
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        startBtn.setBackground(new Color(40, 140, 40));
        startBtn.setForeground(Color.WHITE);
        startBtn.setPreferredSize(new Dimension(200, 45));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(startBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void prepareController() {
        prepareControllerStartButton();
        // Si selecciona Supervivencia, se desactiva el lÃ­mite de goles
        matchTypeCombo.addActionListener(e -> {
            boolean isPerGoals = matchTypeCombo.getSelectedItem() == MatchType.PER_GOALS;
            goalLimitCombo.setVisible(isPerGoals);
            timeLimitCombo.setVisible(!isPerGoals);
        });
    }

    private void prepareControllerStartButton() {
        startBtn.addActionListener(e -> {
            String p1Name = nameFieldP1.getText().trim();
            if (p1Name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Jugador 1");
                return;
            }
            String p1Country = (String) countryComboP1.getSelectedItem();
            MatchType selectedMatchType = (MatchType) matchTypeCombo.getSelectedItem();
            int goalLimit = (Integer) goalLimitCombo.getSelectedItem();
            int timeLimit = (Integer) timeLimitCombo.getSelectedItem();

            if (mode == GameMode.PVP || mode == GameMode.MVM) {
                String p2Name = nameFieldP2.getText().trim();
                if (p2Name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Jugador 2");
                    return;
                }
                String p2Country = (String) countryComboP2.getSelectedItem();
                container.onPlayerConfigConfirmed(p1Name, p1Country, p2Name, p2Country, selectedMatchType, goalLimit, timeLimit);
            } else {
                container.onPlayerConfigConfirmed(p1Name, p1Country, selectedMatchType, goalLimit, timeLimit);
            }
        });
    }
}
