package presentation;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Configuration screen for players, stats upgrades, match type, and pitch resolution.
 */
public class PlayerConfig extends JPanel {//Ayudado a perfeccionar con Gemini Hydrogen 4
    /*Perfeccionado con Antigravity.*/

    private static final Color CARD_BG = new Color(18, 28, 48, 232); // Fondo azul marino traslúcido de la tarjeta principal
    private static final Color CARD_BORDER = ArcadeUIStyle.CARD_BORDER; // Borde dorado compartido con los diálogos arcade
    private static final Color P1_TITLE_COLOR = new Color(70, 210, 250); // Azul cian identificador de Jugador 1
    private static final Color P2_TITLE_COLOR = new Color(255, 165, 50); // Naranja vibrante identificador de Jugador 2
    private static final Color MATCH_TITLE_COLOR = new Color(245, 198, 45); // Dorado para el bloque de reglas del partido
    private static final Color FIELD_BG = new Color(26, 38, 62); // Fondo oscuro unificado para campos y desplegables activos
    private static final Color FIELD_LOCKED_BG = new Color(52, 62, 84); // Tono pizarra para campos bloqueados (ej. CPU)
    private static final Color FIELD_DISABLED_FG = new Color(185, 195, 210); // Texto gris claro cuando una casilla no es editable
    private static final Color FIELD_BORDER = new Color(95, 130, 175); // Contorno azul acero de cada caja de entrada
    private static final Color COMBO_HOVER_BG = new Color(34, 88, 130); // Resalte de fila seleccionada al abrir un desplegable
    private static final Color PILL_BG = new Color(12, 18, 32, 225); // Fondo oscuro de la píldora de puntos e historial
    private static final Color P1_BTN_BG = new Color(14, 82, 110); // Color base de los botones de mejora del Jugador 1
    private static final Color P2_BTN_BG = new Color(195, 95, 12); // Color base de los botones de mejora del Jugador 2

    private static final Font FONT_SECTION = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Arial", Font.PLAIN, 13);

    private static final List<ResolutionOption> DEFAULT_RESOLUTIONS = List.of(
            new ResolutionOption("Pequeña (1/4 de pantalla)", 0.25, 0.35),
            new ResolutionOption("Mediana (1/2 de pantalla)", 0.50, 0.60),
            new ResolutionOption("Grande (3/4 de pantalla)", 0.75, 0.78)
    );

    private final GameModeOption mode;
    private final PlayerConfigListener listener;
    private final GameController controller;
    private final List<ResolutionOption> resolutionOptions;

    private JTextField nameFieldP1;
    private JComboBox<String> countryComboP1;
    private JComboBox<String> machineProfileComboP1;
    // Desplegable deshabilitado temporalmente para el perfil de estilo del Jugador 1
    /* private JComboBox<String> styleComboP1; */
    private JTextField nameFieldP2;
    private JComboBox<String> countryComboP2;
    private JComboBox<String> machineProfileComboP2;
    // Desplegable deshabilitado temporalmente para el perfil de estilo del Jugador 2
    /* private JComboBox<String> styleComboP2; */

    private JComboBox<MatchTypeOption> matchTypeCombo;
    private JLabel limitLabel;
    private JComboBox<Integer> limitCombo;
    private JPanel limitComboHolder;
    private JComboBox<ResolutionOption> resolutionCombo;

    private JButton backBtn;
    private JButton startBtn;
    private static BufferedImage background; // Imagen de fondo compartida y cacheada en memoria

    protected PlayerConfig(PlayerConfigListener listener, GameController controller, GameModeOption mode,
                           List<ResolutionOption> resolutions) {
        this.mode = mode != null ? mode : new GameModeOption("PVM", "Jugador vs Máquina (PvM)", false, true);
        this.listener = listener;
        this.controller = controller;
        this.resolutionOptions = (resolutions != null && !resolutions.isEmpty())
                ? List.copyOf(resolutions)
                : DEFAULT_RESOLUTIONS;
    }

    public static PlayerConfig create(PlayerConfigListener listener, GameController controller, GameModeOption mode) {
        return create(listener, controller, mode, DEFAULT_RESOLUTIONS);
    }

    public static PlayerConfig create(PlayerConfigListener listener, GameController controller, GameModeOption mode,
                                      List<ResolutionOption> resolutions) {
        PlayerConfig panel = new PlayerConfig(listener, controller, mode, resolutions);
        panel.initializeView();
        return panel;
    }

    protected void initializeView() {
        preloadBackground();
        prepareView();
        prepareController();
    }

    /**
     * Loads and caches the player configuration background image once for all instances.
     */
    public static synchronized void preloadBackground() {
        if (background != null) {
            return;
        }
        try (InputStream is = PlayerConfig.class.getResourceAsStream("/menu/playerconfig.jpg")) {
            if (is != null) {
                background = ImageIO.read(is);
            }
        } catch (Exception e) {
            background = null;
        }
    }

    // Deshabilitado temporalmente: creaba el botón desplegable de perfil del jugador y conectaba su listener al controlador
    /*
    private JComboBox<String> createProfileStyleCombo(int playerNum) {
        // Solicita al controlador la lista de estilos disponibles para poblar el desplegable
        String[] rawStyles = controller.getAvaliableStyles();
        // Evita errores si el controlador devuelve null usando un arreglo vacío como respaldo
        String[] styles = rawStyles != null ? rawStyles : new String[0];
        // Construye el JComboBox con el mismo estilo visual que los demás desplegables
        JComboBox<String> styleCombo = createStyledComboBox(styles);
        // Registra el listener después de cargar las opciones para reaccionar solo a cambios del usuario
        styleCombo.addActionListener(e -> onProfileStyleSelected(playerNum, styleCombo));
        return styleCombo;
    }

    // Maneja el cambio de opción en el desplegable de perfil y notifica al controlador qué estilo eligió el jugador
    private void onProfileStyleSelected(int playerNum, JComboBox<String> styleCombo) {
        // Obtiene el texto de la opción actualmente elegida en el desplegable
        String selected = (String) styleCombo.getSelectedItem();
        if (selected != null) {
            // Envía al controlador el número de jugador y el perfil seleccionado
            controller.selectPlayerProfile(playerNum, selected);
        }
    }
    */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }

    private void prepareView() {
        configureRootPanel();
        prepareViewTopBar();
        JPanel formCard = createTranslucentFormCard();
        GridBagConstraints gbc = createFormConstraints();
        prepareViewPlayers(formCard, gbc);
        prepareViewMatchSettings(formCard, gbc);
        add(wrapCenteredCard(formCard), BorderLayout.CENTER);
        prepareViewStartButton();
    }

    private void configureRootPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(8, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 44, 12, 44));
    }

    private JPanel createTranslucentFormCard() {
        JPanel formCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                paintFormCardSurface(g, getWidth(), getHeight());
            }
        };
        formCard.setOpaque(false);
        formCard.setBorder(BorderFactory.createEmptyBorder(14, 22, 16, 22));
        return formCard;
    }

    private void paintFormCardSurface(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create(); // Pinta la tarjeta oscura traslúcida con borde dorado estilo Head Soccer
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, width, height, 22, 22);
        g2.setColor(CARD_BORDER);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(1, 1, width - 3, height - 3, 22, 22);
        g2.dispose();
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        return gbc;
    }

    private JPanel wrapCenteredCard(JPanel formCard) {
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.weightx = 1.0;
        wrapGbc.fill = GridBagConstraints.HORIZONTAL;
        centerWrapper.add(formCard, wrapGbc);
        return centerWrapper;
    }

    private void prepareViewPlayers(JPanel formCard, GridBagConstraints gbc) {
        addPlayerSection(formCard, gbc, 1, mode.isMachine(1), P1_TITLE_COLOR, P1_BTN_BG, 0);
        addPlayerSection(formCard, gbc, 2, mode.isMachine(2), P2_TITLE_COLOR, P2_BTN_BG, 4);
    }

    private void addPlayerSection(JPanel formCard, GridBagConstraints gbc, int playerNum,
                                  boolean isMachine, Color titleColor, Color btnFill, int startRow) {
        gbc.gridy = startRow;
        gbc.insets = new Insets(startRow == 0 ? 4 : 10, 0, 4, 0);
        formCard.add(createSectionHeader(buildPlayerTitle(playerNum, isMachine), titleColor), gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        JTextField nameField = createStyledTextField(controller.getPlayerName(playerNum), true);
        JComboBox<String> countryCombo = createStyledComboBox(controller.getAvailableTeams());
        JComboBox<String> profileCombo = isMachine ? createStyledComboBox(controller.getAvailableMachineProfiles()) : null;
        // Deshabilitado temporalmente: construía el desplegable de perfil del jugador para este jugador
        /* JComboBox<String> styleCombo = createProfileStyleCombo(playerNum); */
        storePlayerFields(playerNum, nameField, countryCombo, profileCombo);

        addPlayerIdentityRows(formCard, gbc, startRow + 1, nameField, countryCombo, profileCombo);
        gbc.gridy = startRow + 3;
        formCard.add(createStatsSection(playerNum, nameField, btnFill, titleColor), gbc);
    }

    private String buildPlayerTitle(int playerNum, boolean isMachine) {
        String role = isMachine ? "Máquina (Jugador " + playerNum + ")" : "Jugador " + playerNum;
        return "Configuración " + role + ":";
    }

    // Guarda las referencias de los campos y desplegables de cada jugador
    private void storePlayerFields(int playerNum, JTextField nameField,
                                   JComboBox<String> countryCombo, JComboBox<String> profileCombo) {
        if (playerNum == 1) {
            nameFieldP1 = nameField;
            countryComboP1 = countryCombo;
            machineProfileComboP1 = profileCombo;
            // Almacenaba la referencia del desplegable de perfil del Jugador 1
            /* styleComboP1 = styleCombo; */
        } else {
            nameFieldP2 = nameField;
            countryComboP2 = countryCombo;
            machineProfileComboP2 = profileCombo;
            // Almacenaba la referencia del desplegable de perfil del Jugador 2
            /* styleComboP2 = styleCombo; */
        }
    }

    // Organiza las filas de identidad del jugador (el desplegable de perfil queda comentado temporalmente)
    private void addPlayerIdentityRows(JPanel formCard, GridBagConstraints gbc, int baseRow,
                                       JTextField nameField, JComboBox<String> countryCombo,
                                       JComboBox<String> profileCombo) {
        if (profileCombo != null) {
            configureMachineNameField(nameField, profileCombo);
            gbc.gridy = baseRow;
            formCard.add(createInlineFieldRow("Nombre:", nameField), gbc);
            // Ubicaba el desplegable de perfil junto al nombre cuando el jugador es máquina
            /* formCard.add(createTwoColumnRow("Nombre:", nameField, "Perfil del jugador:", styleCombo), gbc); */
            gbc.gridy = baseRow + 1;
            formCard.add(createTwoColumnRow("País / Poder:", countryCombo, "Perfil de máquina:", profileCombo), gbc);
            return;
        }
        gbc.gridy = baseRow;
        formCard.add(createTwoColumnRow("Nombre:", nameField, "País / Poder:", countryCombo), gbc);
        // Agregaba el desplegable de perfil debajo de nombre y país cuando el jugador es humano
        /*
        gbc.gridy = baseRow + 1;
        formCard.add(createInlineFieldRow("Perfil del jugador:", styleCombo), gbc);
        */
    }

    private JLabel createSectionHeader(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(FONT_SECTION);
        label.setForeground(color);
        return label;
    }

    private JPanel createInlineFieldRow(String labelText, Component field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(createDarkLabel(labelText), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JPanel createTwoColumnRow(String labelLeft, Component inputLeft,
                                      String labelRight, Component inputRight) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(createInlineFieldRow(labelLeft, inputLeft));
        row.add(createInlineFieldRow(labelRight, inputRight));
        return row;
    }

    private void configureMachineNameField(JTextField nameField, JComboBox<String> profileCombo) {
        nameField.setEditable(false); // Bloquea la edición manual de la máquina y lo fija según el perfil elegido
        nameField.setBackground(FIELD_LOCKED_BG);
        nameField.setForeground(FIELD_DISABLED_FG);
        nameField.setText("CPU " + profileCombo.getSelectedItem());
        profileCombo.addActionListener(e -> nameField.setText("CPU " + profileCombo.getSelectedItem()));
    }

    private JPanel createStatsSection(int playerNum, JTextField nameField, Color btnFill, Color btnAccent) {
        controller.selectPlayerProfile(playerNum, nameField.getText());
        JPanel containerPanel = new JPanel(new BorderLayout(0, 6));
        containerPanel.setOpaque(false);

        JLabel pointsLbl = createPointsPillLabel(playerNum);
        containerPanel.add(wrapPillBadge(pointsLbl), BorderLayout.NORTH);

        String[] stats = controller.getAvailableStats();
        JButton[] buttons = new JButton[stats.length];
        containerPanel.add(createStatButtonsBar(playerNum, stats, pointsLbl, buttons, btnFill, btnAccent),
                BorderLayout.CENTER);
        bindProfileNameListener(nameField, playerNum, pointsLbl, buttons, stats);
        return containerPanel;
    }

    private JPanel wrapPillBadge(JLabel pointsLbl) {
        JPanel pillHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pillHolder.setOpaque(false);
        pillHolder.add(pointsLbl);
        return pillHolder;
    }

    private JPanel createStatButtonsBar(int playerNum, String[] stats, JLabel pointsLbl,
                                        JButton[] buttons, Color btnFill, Color btnAccent) {
        JPanel statsBar = new JPanel(new GridLayout(1, stats.length, 8, 0));
        statsBar.setOpaque(false);
        for (int i = 0; i < stats.length; i++) {
            buttons[i] = createStatButton(playerNum, stats[i], pointsLbl, buttons, stats, btnFill, btnAccent);
            statsBar.add(buttons[i]);
        }
        return statsBar;
    }

    private JLabel createPointsPillLabel(int playerNum) {
        JLabel badge = new JLabel(formatProfileSummary(playerNum), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                paintPillBadgeSurface(g, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_LABEL);
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        return badge;
    }

    private void paintPillBadgeSurface(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PILL_BG);
        g2.fillRoundRect(0, 0, width, height, 16, 16);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 16, 16);
        g2.dispose();
    }

    private String formatProfileSummary(int playerNum) {
        return "Puntos: " + controller.getPlayerPoints(playerNum)
                + " | PJ: " + controller.getPlayerMatchesPlayed(playerNum)
                + " (G: " + controller.getPlayerWins(playerNum) + ")";
    }

    private void bindProfileNameListener(JTextField nameField, int playerNum, JLabel pointsLbl,
                                         JButton[] buttons, String[] stats) {
        Runnable refreshProfile = () -> { // Recarga el perfil desde la base de datos cada vez que cambia el nombre
            controller.selectPlayerProfile(playerNum, nameField.getText());
            pointsLbl.setText(formatProfileSummary(playerNum));
            refreshStatButtons(buttons, playerNum, stats);
        };
        nameField.getDocument().addDocumentListener(createSimpleDocListener(refreshProfile));
    }

    private DocumentListener createSimpleDocListener(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    private void refreshStatButtons(JButton[] buttons, int playerNum, String[] stats) {
        for (int i = 0; i < stats.length; i++) {
            updateStatButtonText(buttons[i], playerNum, stats[i]);
        }
    }

    private JButton createStatButton(int playerNum, String statName, JLabel pointsLbl,
                                     JButton[] allButtons, String[] stats, Color fillColor, Color accentColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                paintStatButtonSurface(g, this, playerNum, statName, fillColor, accentColor);
                super.paintComponent(g);
            }
        };
        ArcadeUIStyle.configureCleanButton(btn, FONT_LABEL, new Dimension(120, 30));
        updateStatButtonText(btn, playerNum, statName);
        btn.addActionListener(e -> handleStatUpgradeClick(playerNum, statName, pointsLbl, allButtons, stats));
        return btn;
    }

    private void paintStatButtonSurface(Graphics g, AbstractButton btn, int playerNum,
                                        String statName, Color fillColor, Color accentColor) {
        Graphics2D g2 = (Graphics2D) g.create(); // Pinta cada botón de estadística con brillo si la mejora está disponible
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean affordable = isStatUpgradeAffordable(playerNum, statName);
        Color bg = ArcadeUIStyle.resolveHoverFill(btn, fillColor);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), 16, 16);
        g2.setColor(affordable ? accentColor : new Color(255, 255, 255, 90)); // Resalta el borde si los puntos alcanzan
        g2.setStroke(new BasicStroke(affordable ? 2.0f : 1.2f));
        g2.drawRoundRect(1, 1, btn.getWidth() - 3, btn.getHeight() - 3, 16, 16);
        g2.dispose();
    }

    private boolean isStatUpgradeAffordable(int playerNum, String statName) {
        return controller.getPlayerStatLevel(playerNum, statName) < controller.getMaxStatLevel()
                && controller.getPlayerPoints(playerNum) >= controller.getPlayerUpgradeCost(playerNum, statName);
    }

    private void handleStatUpgradeClick(int playerNum, String statName, JLabel pointsLbl,
                                        JButton[] allButtons, String[] stats) {
        if (!controller.upgradePlayerStat(playerNum, statName)) {
            int cost = controller.getPlayerUpgradeCost(playerNum, statName);
            int maxLevel = controller.getMaxStatLevel();
            JOptionPane.showMessageDialog(this,
                    "Necesitas " + cost + " puntos (o ya está al máximo nivel " + maxLevel + ").");
        }
        pointsLbl.setText(formatProfileSummary(playerNum));
        refreshStatButtons(allButtons, playerNum, stats);
    }

    private void updateStatButtonText(JButton btn, int playerNum, String statName) {
        int lvl = controller.getPlayerStatLevel(playerNum, statName);
        int cost = controller.getPlayerUpgradeCost(playerNum, statName);
        int maxLevel = controller.getMaxStatLevel();
        btn.setText(statName + " Nv." + lvl + " (+)");
        btn.setToolTipText(lvl < maxLevel ? "Mejorar por " + cost + " pts" : "Nivel máximo (" + maxLevel + ")");
        btn.repaint();
    }

    private void prepareViewMatchSettings(JPanel formCard, GridBagConstraints gbc) {
        addMatchHeader(formCard, gbc);
        initMatchCombos();
        addMatchSettingsGrid(formCard, gbc);
    }

    private void addMatchHeader(JPanel formCard, GridBagConstraints gbc) {
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 0, 4, 0);
        formCard.add(createSectionHeader("Configuración del Partido:", MATCH_TITLE_COLOR), gbc);
    }

    private void initMatchCombos() {
        List<MatchTypeOption> matchTypes = controller.getAvailableMatchTypes();
        MatchTypeOption[] typeArray = matchTypes != null
                ? matchTypes.toArray(MatchTypeOption[]::new)
                : new MatchTypeOption[0];
        matchTypeCombo = createStyledComboBox(typeArray);
        limitLabel = createDarkLabel("");
        limitCombo = createStyledComboBox(new Integer[0]);
        limitComboHolder = new JPanel(new BorderLayout());
        limitComboHolder.setOpaque(false);
        limitComboHolder.add(limitCombo, BorderLayout.CENTER);
        onMatchTypeChanged();

        resolutionCombo = createStyledComboBox(resolutionOptions.toArray(ResolutionOption[]::new));
        if (resolutionOptions.size() > 1) {
            resolutionCombo.setSelectedIndex(1);
        } else if (!resolutionOptions.isEmpty()) {
            resolutionCombo.setSelectedIndex(0);
        }
    }

    private void addMatchSettingsGrid(JPanel formCard, GridBagConstraints gbc) {
        JPanel matchGrid = new JPanel(new GridLayout(1, 3, 12, 0));
        matchGrid.setOpaque(false);
        matchGrid.add(createStackedColumn(createDarkLabel("Tipo de partido:"), matchTypeCombo));
        matchGrid.add(createStackedColumn(limitLabel, limitComboHolder));
        matchGrid.add(createStackedColumn(createDarkLabel("Resolución de la cancha:"), resolutionCombo));

        gbc.gridy = 9;
        gbc.insets = new Insets(2, 0, 2, 0);
        formCard.add(matchGrid, gbc);
    }

    private JPanel createStackedColumn(JLabel label, Component input) {
        JPanel col = new JPanel(new BorderLayout(0, 4));
        col.setOpaque(false);
        col.add(label, BorderLayout.NORTH);
        col.add(input, BorderLayout.CENTER);
        return col;
    }

    private JLabel createDarkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField createStyledTextField(String initialText, boolean editable) {
        JTextField field = new JTextField(initialText);
        field.setFont(FONT_INPUT);
        field.setForeground(editable ? Color.WHITE : FIELD_DISABLED_FG);
        field.setDisabledTextColor(FIELD_DISABLED_FG);
        field.setCaretColor(Color.WHITE);
        field.setSelectionColor(COMBO_HOVER_BG);
        field.setSelectedTextColor(Color.WHITE);
        field.setBackground(editable ? FIELD_BG : FIELD_LOCKED_BG);
        field.setEditable(editable);
        field.setPreferredSize(new Dimension(160, 28));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return field;
    }

    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_INPUT);
        combo.setForeground(Color.WHITE);
        combo.setBackground(FIELD_BG);
        combo.setOpaque(true);
        combo.setPreferredSize(new Dimension(160, 28));
        combo.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1, true));
        combo.setUI(new DarkComboBoxUI());
        combo.setRenderer(createDarkComboRenderer());
        return combo;
    }

    private JButton createDarkComboArrowButton() {
        JButton arrow = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                paintComboArrowButton(g, this);
            }
        };
        arrow.setPreferredSize(new Dimension(24, 24));
        arrow.setBorder(BorderFactory.createEmptyBorder());
        arrow.setContentAreaFilled(false);
        arrow.setBorderPainted(false);
        arrow.setFocusPainted(false);
        arrow.setOpaque(true);
        return arrow;
    }

    private void paintComboArrowButton(Graphics g, JButton arrow) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean enabled = arrow.getParent() == null || arrow.getParent().isEnabled();
        g2.setColor(enabled ? FIELD_BG : FIELD_LOCKED_BG);
        g2.fillRect(0, 0, arrow.getWidth(), arrow.getHeight());
        drawGoldDropdownTriangle(g2, arrow.getWidth() / 2, arrow.getHeight() / 2, enabled);
        g2.dispose();
    }

    private void drawGoldDropdownTriangle(Graphics2D g2, int cx, int cy, boolean enabled) {
        g2.setColor(enabled ? CARD_BORDER : FIELD_DISABLED_FG);
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(cx - 4.0, cy - 2.0);
        triangle.lineTo(cx + 4.0, cy - 2.0);
        triangle.lineTo(cx, cy + 3.5);
        triangle.closePath();
        g2.fill(triangle);
    }

    private DefaultListCellRenderer createDarkComboRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                boolean enabled = list == null || list.isEnabled();
                setOpaque(true);
                setBackground(resolveComboRowBackground(index == -1, isSelected, enabled));
                setForeground(enabled ? Color.WHITE : FIELD_DISABLED_FG);
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                return this;
            }
        };
    }

    private Color resolveComboRowBackground(boolean closedDisplay, boolean isSelected, boolean enabled) {
        if (closedDisplay) {
            return enabled ? FIELD_BG : FIELD_LOCKED_BG;
        }
        return isSelected ? COMBO_HOVER_BG : FIELD_BG;
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
                paintBackArrowSurface(g, this);
            }
        };
        ArcadeUIStyle.configureCleanButton(btn, FONT_LABEL, new Dimension(38, 34));
        btn.setToolTipText("Volver al menú");
        return btn;
    }

    private void paintBackArrowSurface(Graphics g, AbstractButton btn) {
        Graphics2D g2 = (Graphics2D) g.create(); // Dibuja un botón redondeado traslúcido y flecha vectorizada con antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hovered = btn.getModel().isRollover() || btn.getModel().isPressed();
        g2.setColor(hovered ? new Color(25, 25, 35, 210) : new Color(25, 25, 35, 155));
        g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), 18, 18);
        g2.setColor(hovered ? CARD_BORDER : new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, btn.getWidth() - 1, btn.getHeight() - 1, 18, 18);
        drawBackArrowStroke(g2, btn.getHeight() / 2);
        g2.dispose();
    }

    private void drawBackArrowStroke(Graphics2D g2, int cy) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D arrow = new Path2D.Float();
        arrow.moveTo(24, cy);
        arrow.lineTo(12, cy);
        arrow.moveTo(18, cy - 6);
        arrow.lineTo(12, cy);
        arrow.lineTo(18, cy + 6);
        g2.draw(arrow);
    }

    private void prepareViewStartButton() {
        startBtn = createStartButton();
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 34, 0)); // Deja margen inferior para no tapar el copyright
        bottomPanel.add(startBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createStartButton() {
        JButton btn = new JButton("Iniciar Partido") {
            @Override
            protected void paintComponent(Graphics g) {
                ArcadeUIStyle.paintArcadeButton(g, this, ArcadeUIStyle.BTN_GREEN, 18);
                super.paintComponent(g);
            }
        };
        ArcadeUIStyle.configureCleanButton(btn, new Font("Arial", Font.BOLD, 16), new Dimension(215, 42));
        return btn;
    }

    private void prepareController() {
        backBtn.addActionListener(e -> listener.goToMenu());
        startBtn.addActionListener(e -> onStartMatchClicked());
        matchTypeCombo.addActionListener(e -> onMatchTypeChanged());
    }

    private void onMatchTypeChanged() {
        MatchTypeOption selected = (MatchTypeOption) matchTypeCombo.getSelectedItem();
        limitCombo.removeAllItems();
        boolean hasParameter = selected != null && selected.hasParameter();
        if (hasParameter) {
            limitLabel.setText(selected.getParameterLabel());
            for (Integer option : selected.getParameterOptions()) {
                limitCombo.addItem(option);
            }
        } else {
            limitLabel.setText("");
        }
        limitLabel.setVisible(hasParameter);
        limitCombo.setVisible(hasParameter);
        limitComboHolder.revalidate();
        limitComboHolder.repaint();
    }

    private void onStartMatchClicked() {
        if (!validateMachineProfiles()) {
            return;
        }
        String p1Name = nameFieldP1.getText().trim();
        String p2Name = nameFieldP2.getText().trim();
        if (p1Name.length() < 3 || p2Name.length() < 3) { // Exige que ambos nombres tengan un mínimo de 3 caracteres
            JOptionPane.showMessageDialog(this, "El nombre de cada jugador debe tener al menos 3 letras");
            return;
        }
        launchConfiguredMatch(p1Name, p2Name);
    }

    private void launchConfiguredMatch(String p1Name, String p2Name) {
        String p1Country = (String) countryComboP1.getSelectedItem();
        String p2Country = (String) countryComboP2.getSelectedItem();
        MatchTypeOption selectedOption = (MatchTypeOption) matchTypeCombo.getSelectedItem();
        String selectedMatchType = selectedOption != null ? selectedOption.getDisplayName() : "";
        int matchLimit = (selectedOption != null && selectedOption.hasParameter() && limitCombo.getSelectedItem() != null)
                ? (Integer) limitCombo.getSelectedItem()
                : 0;
        listener.setResolution(resolveSelectedDimension());
        listener.onPlayerConfigConfirmed(p1Name, p1Country, p2Name, p2Country, selectedMatchType, matchLimit);
    }

    private boolean validateMachineProfiles() {
        return isMachineProfileChoiceValid(machineProfileComboP1)
                && isMachineProfileChoiceValid(machineProfileComboP2);
    }

    private boolean isMachineProfileChoiceValid(JComboBox<String> profileCombo) {
        if (profileCombo != null && !controller.isMachineProfileAvailable((String) profileCombo.getSelectedItem())) { // Verifica disponibilidad antes de iniciar
            List<String> available = new ArrayList<>();
            String[] profiles = controller.getAvailableMachineProfiles();
            if (profiles != null) {
                for (String profile : profiles) {
                    if (controller.isMachineProfileAvailable(profile)) {
                        available.add(profile);
                    }
                }
            }
            String message = available.isEmpty()
                    ? "Tipo de máquina en construcción."
                    : "Tipo de máquina en construcción. Opciones disponibles: " + String.join(", ", available);
            JOptionPane.showMessageDialog(this, message);
            return false;
        }
        return true;
    }

    private Dimension resolveSelectedDimension() {
        ResolutionOption selected = (ResolutionOption) resolutionCombo.getSelectedItem();
        if (selected == null) {
            selected = DEFAULT_RESOLUTIONS.get(1);
        }
        return selected.toDimension(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private final class DarkComboBoxUI extends BasicComboBoxUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            comboBox.setBackground(FIELD_BG);
            comboBox.setForeground(Color.WHITE);
            applyDarkListColors(listBox);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            g.setColor(c.isEnabled() ? FIELD_BG : FIELD_LOCKED_BG); // Pinta fondo completo para evitar destellos blancos del LAF
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            super.paint(g, c);
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(comboBox.isEnabled() ? FIELD_BG : FIELD_LOCKED_BG);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            Component c = prepareClosedValueComponent(); // Renderiza el valor cerrado sin aplicar el resaltado claro de foco del sistema
            currentValuePane.paintComponent(
                    g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, c instanceof JPanel);
        }

        private Component prepareClosedValueComponent() {
            Component c = comboBox.getRenderer().getListCellRendererComponent(
                    listBox, comboBox.getSelectedItem(), -1, false, false);
            c.setFont(comboBox.getFont());
            c.setBackground(comboBox.isEnabled() ? FIELD_BG : FIELD_LOCKED_BG);
            c.setForeground(comboBox.isEnabled() ? Color.WHITE : FIELD_DISABLED_FG);
            return c;
        }

        @Override
        protected JButton createArrowButton() {
            return createDarkComboArrowButton();
        }

        @Override
        protected ComboPopup createPopup() {
            return new DarkComboPopup(comboBox);
        }
    }

    private static final class DarkComboPopup extends BasicComboPopup {

        private DarkComboPopup(JComboBox<Object> owner) {
            super(owner);
        }

        @Override
        protected void configureList() {
            super.configureList();
            applyDarkListColors(list);
        }

        @Override
        protected void configureScroller() {
            super.configureScroller();
            scroller.setBackground(FIELD_BG);
            scroller.getViewport().setBackground(FIELD_BG);
            scroller.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        }

        @Override
        protected void configurePopup() {
            super.configurePopup();
            setBackground(FIELD_BG);
            setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1));
        }
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = FIELD_BORDER;
            trackColor = FIELD_BG;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroSizeButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroSizeButton();
        }

        private JButton createZeroSizeButton() {
            JButton empty = new JButton();
            empty.setPreferredSize(new Dimension(0, 0));
            empty.setMinimumSize(new Dimension(0, 0));
            empty.setMaximumSize(new Dimension(0, 0));
            return empty;
        }
    }

    private static void applyDarkListColors(JList<?> list) {
        if (list == null) {
            return;
        }
        list.setBackground(FIELD_BG);
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(COMBO_HOVER_BG);
        list.setSelectionForeground(Color.WHITE);
    }
}
