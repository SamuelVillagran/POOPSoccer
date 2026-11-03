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
 * Pantalla de Configuración de Jugadores y Tipo de Partido.
 */
public class PlayerConfig extends JPanel {

    private GameMode mode;
    private GameContainer container;
    private JTextField nameFieldP1;
    private JComboBox<String> countryComboP1;
    private JTextField nameFieldP2;
    private JComboBox<String> countryComboP2;

    // Configuración del tipo de partido
    private JComboBox<MatchType> matchTypeCombo;
    private JComboBox<Integer> goalLimitCombo;

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

        // 1. Configuración de Jugadores
        prepareViewSettingPlayer(formPanel);

        // 2. Configuración del Partido (Por Goles vs Supervivencia)
        prepareViewMatchSettings(formPanel);

        add(formPanel, BorderLayout.CENTER);

        // 3. Botón Iniciar
        prepareViewStartButton();
    }

    private void prepareViewSettingPlayer(JPanel formPanel) {
        JLabel titleP1 = new JLabel("Configuración Jugador 1:");
        titleP1.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleP1);

        nameFieldP1 = new JTextField("Jugador 1");
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP1);

        String[] possibleCountries = new String[] { "Canadá", "Colombia", "Alemania" };
        countryComboP1 = new JComboBox<>(possibleCountries);
        formPanel.add(new JLabel("País / Poder:"));
        formPanel.add(countryComboP1);

        // Si es PvP o MvM, agregar configuración de Jugador 2
        if (mode == GameMode.PVP || mode == GameMode.MVM) {
            JLabel titleP2 = new JLabel("Configuración Jugador 2:");
            titleP2.setFont(new Font("Arial", Font.BOLD, 15));
            formPanel.add(titleP2);

            nameFieldP2 = new JTextField(mode == GameMode.MVM ? "Máquina 2" : "Jugador 2");
            formPanel.add(new JLabel("Nombre:"));
            formPanel.add(nameFieldP2);

            countryComboP2 = new JComboBox<>(possibleCountries);
            formPanel.add(new JLabel("País / Poder:"));
            formPanel.add(countryComboP2);
        }
    }

    private void prepareViewMatchSettings(JPanel formPanel) {// Hecho con Gemini Flash 3.8
        JLabel titleMatch = new JLabel("Configuración del Partido:");
        titleMatch.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleMatch);

        // Regla: Supervivencia solo disponible en modalidad PvsM
        if (mode == GameMode.PVM) {
            matchTypeCombo = new JComboBox<>(new MatchType[] { MatchType.PER_GOALS, MatchType.SURVIVAL });
        } else {
            matchTypeCombo = new JComboBox<>(new MatchType[] { MatchType.PER_GOALS });
        }
        formPanel.add(new JLabel("Tipo de partido:"));
        formPanel.add(matchTypeCombo);

        // Límite de goles (3, 5 o 7 según especificación)
        goalLimitCombo = new JComboBox<>(new Integer[] { 3, 5, 7 });
        formPanel.add(new JLabel("Goles para ganar (si es por goles):"));
        formPanel.add(goalLimitCombo);
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
        // Si selecciona Supervivencia, se desactiva el límite de goles
        matchTypeCombo.addActionListener(e -> {
            boolean isPerGoals = matchTypeCombo.getSelectedItem() == MatchType.PER_GOALS;
            goalLimitCombo.setEnabled(isPerGoals);
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

            if (mode == GameMode.PVP || mode == GameMode.MVM) {
                String p2Name = nameFieldP2.getText().trim();
                if (p2Name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Jugador 2");
                    return;
                }
                String p2Country = (String) countryComboP2.getSelectedItem();
                container.onPlayerConfigConfirmed(p1Name, p1Country, p2Name, p2Country, selectedMatchType, goalLimit);
            } else {
                container.onPlayerConfigConfirmed(p1Name, p1Country, selectedMatchType, goalLimit);
            }
        });
    }
}
