package presentation;

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

/**
 * Configuration screen for players, stats upgrades, match type, and pitch resolution.
 */
public class PlayerConfig extends JPanel {

    private String mode;
    private GameContainer container;
    private JTextField nameFieldP1;
    private JComboBox<String> countryComboP1;
    private JComboBox<String> machineProfileComboP1;
    private JTextField nameFieldP2;
    private JComboBox<String> countryComboP2;
    private JComboBox<String> machineProfileComboP2;

    // Configuración del tipo de partido
    private JComboBox<String> matchTypeCombo;
    private JLabel limitLabel;
    private JComboBox<Integer> goalLimitCombo;
    private JComboBox<Integer> timeLimitCombo;
    private JPanel limitComboHolder;
    private JComboBox<String> resolutionCombo;
    private Dimension[] screenResolutions;

    private JButton startBtn;
    private BufferedImage background;

    public PlayerConfig(GameContainer container, String mode) {
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

        // Configura los jugadores, el partido y el botón de inicio
        prepareViewSettingPlayer(formPanel);
        prepareViewMatchSettings(formPanel);
        add(formPanel, BorderLayout.CENTER);
        prepareViewStartButton();
    }

    private void prepareViewSettingPlayer(JPanel formPanel) {
        String[] possibleCountries = POOPSoccer.getGame().getAvailableTeams();
        String[] machineProfiles = POOPSoccer.getGame().getAvailableMachineProfiles();
        boolean isP1Machine = "MVM".equals(mode);
        boolean isP2Machine = "PVM".equals(mode) || "MVM".equals(mode);

        // Configura los controles del primer jugador pidiendo los datos a la fachada POOPSoccer
        JLabel titleP1 = new JLabel("Configuración Jugador 1:");
        titleP1.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleP1);

        nameFieldP1 = new JTextField(isP1Machine ? "Máquina 1" : "Jugador 1");
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP1);

        countryComboP1 = new JComboBox<>(possibleCountries);
        if (isP1Machine) {
            machineProfileComboP1 = new JComboBox<>(machineProfiles);
            addCountryAndProfileRow(formPanel, countryComboP1, machineProfileComboP1);
        } else {
            formPanel.add(new JLabel("País / Poder:"));
            formPanel.add(countryComboP1);
        }
        addStatsUpgradeRow(formPanel, 1);

        // Configura los controles del segundo jugador pidiendo países y perfiles a POOPSoccer
        JLabel titleP2 = new JLabel(!isP2Machine ? "Configuración Jugador 2:" : "Configuración Máquina (Jugador 2):");
        titleP2.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleP2);

        nameFieldP2 = new JTextField(!isP2Machine ? "Jugador 2" : "Máquina 2");
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP2);

        countryComboP2 = new JComboBox<>(possibleCountries);
        if (isP2Machine) {
            machineProfileComboP2 = new JComboBox<>(machineProfiles);
            addCountryAndProfileRow(formPanel, countryComboP2, machineProfileComboP2);
        } else {
            formPanel.add(new JLabel("País / Poder:"));
            formPanel.add(countryComboP2);
        }
        addStatsUpgradeRow(formPanel, 2);
    }

    private void addCountryAndProfileRow(JPanel formPanel, JComboBox<String> countryCombo, JComboBox<String> profileCombo) {
        // Agrupa país y perfil de máquina en dos columnas para no recargar la altura del formulario
        JPanel labelsRow = new JPanel(new GridLayout(1, 2, 8, 0));
        labelsRow.setOpaque(false);
        labelsRow.add(new JLabel("País / Poder:"));
        labelsRow.add(new JLabel("Perfil de máquina:"));
        formPanel.add(labelsRow);

        JPanel combosRow = new JPanel(new GridLayout(1, 2, 8, 0));
        combosRow.setOpaque(false);
        combosRow.add(countryCombo);
        combosRow.add(profileCombo);
        formPanel.add(combosRow);
    }

    private void addStatsUpgradeRow(JPanel formPanel, int playerNum) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel pointsLbl = new JLabel("Puntos: " + POOPSoccer.getGame().getPoints(playerNum) + "  ");
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
        JButton btn = new JButton();
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        updateStatButtonText(btn, playerNum, statKey, label);
        btn.addActionListener(e -> {
            if (!POOPSoccer.getGame().upgradeStat(playerNum, statKey)) {
                int cost = POOPSoccer.getGame().getUpgradeCost(playerNum, statKey);
                JOptionPane.showMessageDialog(this, "Necesitás " + cost + " puntos (o ya está al máximo nivel 10).");
            }
            pointsLbl.setText("Puntos: " + POOPSoccer.getGame().getPoints(playerNum) + "  ");
            updateStatButtonText(btn, playerNum, statKey, label);
        });
        return btn;
    }

    private void updateStatButtonText(JButton btn, int playerNum, String statKey, String label) {
        int lvl = POOPSoccer.getGame().getStatLevel(playerNum, statKey);
        int cost = POOPSoccer.getGame().getUpgradeCost(playerNum, statKey);
        btn.setText(label + " Nv." + lvl + " (+)");
        btn.setToolTipText(lvl < 10 ? "Mejorar por " + cost + " pts" : "Nivel máximo (10)");
    }

    private void prepareViewMatchSettings(JPanel formPanel) {
        JLabel titleMatch = new JLabel("Configuración del Partido:");
        titleMatch.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleMatch);

        // Consulta los tipos de partido disponibles directamente desde la fachada POOPSoccer
        matchTypeCombo = new JComboBox<>(POOPSoccer.getGame().getAvailableMatchTypes());
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
            boolean isPerGoals = matchTypeCombo.getSelectedIndex() == 0;
            limitLabel.setText(isPerGoals ? "Goles para ganar:" : "Minutos del partido:");
            limitComboHolder.removeAll();
            limitComboHolder.add(isPerGoals ? goalLimitCombo : timeLimitCombo, BorderLayout.CENTER);
            limitComboHolder.revalidate();
            limitComboHolder.repaint();
        });
    }

    private void prepareControllerStartButton() {
        startBtn.addActionListener(e -> {
            if (!validateMachineProfiles()) {
                return;
            }
            String p1Name = nameFieldP1.getText().trim();
            String p2Name = nameFieldP2.getText().trim();
            if (p1Name.isEmpty() || p2Name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar el nombre de ambos jugadores");
                return;
            }
            String p1Country = (String) countryComboP1.getSelectedItem();
            String p2Country = (String) countryComboP2.getSelectedItem();
            String selectedMatchType = (String) matchTypeCombo.getSelectedItem();
            int goalLimit = (Integer) goalLimitCombo.getSelectedItem();
            int timeLimit = (Integer) timeLimitCombo.getSelectedItem();
            container.setResolution(resolveSelectedDimension());
            container.onPlayerConfigConfirmed(p1Name, p1Country, p2Name, p2Country, selectedMatchType, goalLimit, timeLimit);
        });
    }

    private boolean validateMachineProfiles() {
        // Consulta a la fachada POOPSoccer si el perfil de máquina elegido está disponible
        if (machineProfileComboP1 != null && !POOPSoccer.getGame().isMachineProfileAvailable((String) machineProfileComboP1.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, "Tipo de máquina en construcción, elija máquina torpe");
            return false;
        }
        if (machineProfileComboP2 != null && !POOPSoccer.getGame().isMachineProfileAvailable((String) machineProfileComboP2.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, "Tipo de máquina en construcción, elija máquina torpe");
            return false;
        }
        return true;
    }

    private Dimension resolveSelectedDimension() {
        int index = resolutionCombo.getSelectedIndex();
        if (screenResolutions != null && index >= 0 && index < screenResolutions.length) {
            return screenResolutions[index];
        }
        return new Dimension(800, 600);
    }
}
