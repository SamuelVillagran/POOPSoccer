package presentation;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Configuration screen for players, stats upgrades, match type, and pitch resolution.
 */
public class PlayerConfig extends JPanel {

    private String mode;
    private GameContainer container;
    private final GameController controller;
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

    private JButton backBtn;
    private JButton startBtn;
    private BufferedImage background;

    public PlayerConfig(GameContainer container, GameController controller, String mode) {
        this.mode = mode;
        this.container = container;
        this.controller = controller;
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
        setLayout(new BorderLayout(8, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 40, 10, 40));
        prepareViewTopBar();
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
        String[] possibleCountries = controller.getAvailableTeams();
        String[] machineProfiles = controller.getAvailableMachineProfiles();
        boolean isP1Machine = "MVM".equals(mode);
        boolean isP2Machine = "PVM".equals(mode) || "MVM".equals(mode);

        // Configura los controles del primer jugador consultando su último perfil activo al controlador
        JLabel titleP1 = new JLabel(isP1Machine ? "Configuración Máquina (Jugador 1):" : "Configuración Jugador 1:");
        titleP1.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleP1);

        nameFieldP1 = new JTextField(controller.getPlayerName(1));
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP1);

        countryComboP1 = new JComboBox<>(possibleCountries);
        if (isP1Machine) {
            machineProfileComboP1 = new JComboBox<>(machineProfiles);
            configureMachineNameField(nameFieldP1, machineProfileComboP1);
            addCountryAndProfileRow(formPanel, countryComboP1, machineProfileComboP1);
        } else {
            formPanel.add(new JLabel("País / Poder:"));
            formPanel.add(countryComboP1);
        }
        addStatsUpgradeRow(formPanel, 1, nameFieldP1);

        // Configura los controles del segundo jugador solicitando países y perfiles al controlador
        JLabel titleP2 = new JLabel(!isP2Machine ? "Configuración Jugador 2:" : "Configuración Máquina (Jugador 2):");
        titleP2.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleP2);

        nameFieldP2 = new JTextField(controller.getPlayerName(2));
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP2);

        countryComboP2 = new JComboBox<>(possibleCountries);
        if (isP2Machine) {
            machineProfileComboP2 = new JComboBox<>(machineProfiles);
            configureMachineNameField(nameFieldP2, machineProfileComboP2);
            addCountryAndProfileRow(formPanel, countryComboP2, machineProfileComboP2);
        } else {
            formPanel.add(new JLabel("País / Poder:"));
            formPanel.add(countryComboP2);
        }
        addStatsUpgradeRow(formPanel, 2, nameFieldP2);
    }

    private void configureMachineNameField(JTextField nameField, JComboBox<String> profileCombo) {
        // Bloquea la edición manual del nombre de la máquina y lo fija según el perfil elegido
        nameField.setEditable(false);
        nameField.setText("CPU " + profileCombo.getSelectedItem());
        profileCombo.addActionListener(e -> nameField.setText("CPU " + profileCombo.getSelectedItem()));
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

    private void addStatsUpgradeRow(JPanel formPanel, int playerNum, JTextField nameField) {
        // Carga el perfil guardado del jugador según el nombre escrito en el campo de texto
        controller.selectPlayerProfile(playerNum, nameField.getText());
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel pointsLbl = new JLabel(formatProfileSummary(playerNum));
        pointsLbl.setFont(new Font("Arial", Font.BOLD, 12));
        row.add(pointsLbl, BorderLayout.WEST);

        String[] stats = controller.getAvailableStats();
        JPanel statsBar = new JPanel(new GridLayout(1, stats.length, 6, 0));
        statsBar.setOpaque(false);
        JButton[] buttons = new JButton[stats.length];
        for (int i = 0; i < stats.length; i++) {
            buttons[i] = createStatButton(playerNum, stats[i], pointsLbl);
            statsBar.add(buttons[i]);
        }
        bindProfileNameListener(nameField, playerNum, pointsLbl, buttons, stats);
        row.add(statsBar, BorderLayout.CENTER);
        formPanel.add(row);
    }

    private String formatProfileSummary(int playerNum) {
        return "Puntos: " + controller.getPlayerPoints(playerNum)
                + " | PJ: " + controller.getPlayerMatchesPlayed(playerNum)
                + " (G: " + controller.getPlayerWins(playerNum) + ")  ";
    }

    private void bindProfileNameListener(JTextField nameField, int playerNum, JLabel pointsLbl,
                                         JButton[] buttons, String[] stats) {
        // Reacciona únicamente cuando cambia el texto del nombre para recargar el perfil desde la base de datos
        Runnable refreshProfile = () -> {
            controller.selectPlayerProfile(playerNum, nameField.getText());
            pointsLbl.setText(formatProfileSummary(playerNum));
            for (int i = 0; i < stats.length; i++) {
                updateStatButtonText(buttons[i], playerNum, stats[i]);
            }
        };
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshProfile.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshProfile.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshProfile.run();
            }
        });
    }

    private JButton createStatButton(int playerNum, String statName, JLabel pointsLbl) {
        JButton btn = new JButton();
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        updateStatButtonText(btn, playerNum, statName);
        btn.addActionListener(e -> {
            if (!controller.upgradePlayerStat(playerNum, statName)) {
                int cost = controller.getPlayerUpgradeCost(playerNum, statName);
                JOptionPane.showMessageDialog(this, "Necesitás " + cost + " puntos (o ya está al máximo nivel 10).");
            }
            pointsLbl.setText(formatProfileSummary(playerNum));
            updateStatButtonText(btn, playerNum, statName);
        });
        return btn;
    }

    private void updateStatButtonText(JButton btn, int playerNum, String statName) {
        int lvl = controller.getPlayerStatLevel(playerNum, statName);
        int cost = controller.getPlayerUpgradeCost(playerNum, statName);
        btn.setText(statName + " Nv." + lvl + " (+)");
        btn.setToolTipText(lvl < 10 ? "Mejorar por " + cost + " pts" : "Nivel máximo (10)");
    }

    private void prepareViewMatchSettings(JPanel formPanel) {
        JLabel titleMatch = new JLabel("Configuración del Partido:");
        titleMatch.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(titleMatch);

        // Consulta los tipos de partido disponibles directamente desde el controlador
        matchTypeCombo = new JComboBox<>(controller.getAvailableMatchTypes());
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

    private void prepareViewTopBar() {
        backBtn = createBackArrowButton();
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(backBtn);
        add(topBar, BorderLayout.NORTH);
    }

    private JButton createBackArrowButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                // Dibuja un botón circular traslúcido y una flecha vectorizada con antialiasing
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hovered = getModel().isRollover() || getModel().isPressed();
                g2.setColor(hovered ? new Color(25, 25, 35, 210) : new Color(25, 25, 35, 155));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 255, 255, hovered ? 230 : 160));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = getHeight() / 2;
                Path2D arrow = new Path2D.Float();
                arrow.moveTo(24, cy);
                arrow.lineTo(12, cy);
                arrow.moveTo(18, cy - 6);
                arrow.lineTo(12, cy);
                arrow.lineTo(18, cy + 6);
                g2.draw(arrow);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(38, 34));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Volver al menú");
        return btn;
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
        backBtn.addActionListener(e -> container.goToMenu());
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
        // Consulta al controlador si el perfil de máquina elegido está disponible
        if (machineProfileComboP1 != null && !controller.isMachineProfileAvailable((String) machineProfileComboP1.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, "Tipo de máquina en construcción, elija máquina torpe");
            return false;
        }
        if (machineProfileComboP2 != null && !controller.isMachineProfileAvailable((String) machineProfileComboP2.getSelectedItem())) {
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
