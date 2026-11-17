package presentation;

import domain.GameMode;
import domain.MatchType;
import domain.POOPSoccer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
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

    // Configuración del tipo de partido
    private JComboBox<MatchType> matchTypeCombo;
    private JLabel limitLabel;
    private JComboBox<Integer> goalLimitCombo;
    private JComboBox<Integer> timeLimitCombo;
    private JPanel limitComboHolder;
    private JComboBox<String> resolutionCombo;
    private Dimension[] screenResolutions;

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
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBackground(new Color(255, 255, 255, 130));
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        // 1. Configuración de Jugadores
        prepareViewSettingPlayer(formPanel);

        // 2. Configuración del Partido
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

        String[] possibleCountries = POOPSoccer.getGame().getAvailableTeams();
        countryComboP1 = new JComboBox<>(possibleCountries);
        formPanel.add(new JLabel("País / Poder:"));
        formPanel.add(countryComboP1);
        addStatsUpgradeRow(formPanel, 1);

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
            addStatsUpgradeRow(formPanel, 2);
        }
    }

    private void addStatsUpgradeRow(JPanel formPanel, int playerNum) {
        POOPSoccer game = POOPSoccer.getGame();
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel pointsLbl = new JLabel("Puntos: " + game.getPoints(playerNum) + "  ");
        pointsLbl.setFont(new Font("Arial", Font.BOLD, 12));
        row.add(pointsLbl, BorderLayout.WEST);

        JPanel statsBar = new JPanel(new GridLayout(1, 4, 6, 0));
        statsBar.setOpaque(false);
        String[] keys = { "speed", "jump", "kick", "survival" };
        String[] labels = { "Vel", "Salto", "Patada", "Vida" };
        for (int i = 0; i < keys.length; i++) {
            statsBar.add(createStatButton(playerNum, keys[i], labels[i], pointsLbl));
        }
        row.add(statsBar, BorderLayout.CENTER);
        formPanel.add(row);
    }

    private JButton createStatButton(int playerNum, String statKey, String label, JLabel pointsLbl) {
        POOPSoccer game = POOPSoccer.getGame();
        JButton btn = new JButton();
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        updateStatButtonText(btn, playerNum, statKey, label);
        btn.addActionListener(e -> {
            if (!game.upgradeStat(playerNum, statKey)) {
                int cost = game.getUpgradeCost(playerNum, statKey);
                JOptionPane.showMessageDialog(this, "Necesitás " + cost + " puntos (o ya está al máximo nivel 10).");
            }
            pointsLbl.setText("Puntos: " + game.getPoints(playerNum) + "  ");
            updateStatButtonText(btn, playerNum, statKey, label);
        });
        return btn;
    }

    private void updateStatButtonText(JButton btn, int playerNum, String statKey, String label) {
        POOPSoccer game = POOPSoccer.getGame();
        int lvl = game.getStatLevel(playerNum, statKey);
        int cost = game.getUpgradeCost(playerNum, statKey);
        btn.setText(label + " Nv." + lvl + " (+)");
        btn.setToolTipText(lvl < 10 ? "Mejorar por " + cost + " pts" : "Nivel máximo (10)");
    }

    private void prepareViewMatchSettings(JPanel formPanel) {
        JLabel titleMatch = new JLabel("Configuración del Partido:");
        titleMatch.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleMatch);

        matchTypeCombo = new JComboBox<>(new MatchType[] { MatchType.PER_GOALS, MatchType.PER_TIME });
        formPanel.add(new JLabel("Tipo de partido:"));
        formPanel.add(matchTypeCombo);

        limitLabel = new JLabel("Goles para ganar:");
        formPanel.add(limitLabel);

        goalLimitCombo = new JComboBox<>(new Integer[] { 3, 5, 7 });
        timeLimitCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 5 });
        limitComboHolder = new JPanel(new BorderLayout());
        limitComboHolder.setOpaque(false);
        limitComboHolder.add(goalLimitCombo, BorderLayout.CENTER);
        formPanel.add(limitComboHolder);

        screenResolutions = buildScreenResolutions();
        resolutionCombo = new JComboBox<>(new String[] {
                "Pequeña (75% - 4:3)",
                "Estándar (90% - 4:3)",
                "Grande (100% - 4:3)"
        });
        resolutionCombo.setSelectedIndex(1);
        formPanel.add(new JLabel("Resolución de la cancha:"));
        formPanel.add(resolutionCombo);
    }

    private Dimension[] buildScreenResolutions() {
        Rectangle usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int maxPitchW = usableBounds.width - 40;
        int maxPitchH = usableBounds.height - 110; // leaves room for HUD (68px) and window title bar

        // Preserve 4:3 pitch aspect ratio bounded by the player's actual screen
        int baseH = Math.min(maxPitchH, (maxPitchW * 3) / 4);
        double[] scales = { 0.75, 0.90, 1.00 };
        Dimension[] dims = new Dimension[3];
        for (int i = 0; i < scales.length; i++) {
            int h = Math.max(520, (int) Math.round(baseH * scales[i]));
            int w = (int) Math.round(h * (4.0 / 3.0));
            dims[i] = new Dimension(w, h);
        }
        return dims;
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
        matchTypeCombo.addActionListener(e -> {
            boolean isPerGoals = matchTypeCombo.getSelectedItem() == MatchType.PER_GOALS;
            limitLabel.setText(isPerGoals ? "Goles para ganar:" : "Minutos del partido:");
            limitComboHolder.removeAll();
            limitComboHolder.add(isPerGoals ? goalLimitCombo : timeLimitCombo, BorderLayout.CENTER);
            limitComboHolder.revalidate();
            limitComboHolder.repaint();
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
            container.setResolution(resolveSelectedDimension());

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

    private Dimension resolveSelectedDimension() {
        int index = resolutionCombo.getSelectedIndex();
        if (screenResolutions != null && index >= 0 && index < screenResolutions.length) {
            return screenResolutions[index];
        }
        return new Dimension(800, 600);
    }
}
