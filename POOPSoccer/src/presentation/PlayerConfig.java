package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Pantalla de Configuración de Jugadores (Nombres, Equipos/Países).
 */
public class PlayerConfig extends JPanel {

    private GameMode mode;
    private JTextField nameFieldP1;
    private JComboBox<String> countryComboP1;
    private JTextField nameFieldP2;
    private JComboBox<String> countryComboP2;
    private JButton startBtn;

    public PlayerConfig(GameContainer container, GameMode mode) {
        this.mode = mode;
        prepareView(container);
    }

    private void prepareView(GameContainer container) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        // Configuración Jugadores
        prepareViewSettingPlayer(formPanel);

        // Botón Iniciar
        prepareViewStartButton(container);
    }

    private void prepareViewSettingPlayer(JPanel formPanel) {
        JLabel titleP1 = new JLabel("Configuración Jugador 1:");
        titleP1.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(titleP1);

        nameFieldP1 = new JTextField("Jugador 1");
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameFieldP1);

        countryComboP1 = new JComboBox<>(new String[] { "Canadá", "Colombia", "Argentina", "Alemania" });
        formPanel.add(new JLabel("País / Poder:"));
        formPanel.add(countryComboP1);

        // Si es PvP, agregar configuración de Jugador 2
        if (mode == GameMode.PVP || mode == GameMode.MVM) {
            JLabel titleP2 = new JLabel("Configuración Jugador 2:");
            titleP2.setFont(new Font("Arial", Font.BOLD, 16));
            formPanel.add(titleP2);

            nameFieldP2 = new JTextField(mode == GameMode.MVM ? "Máquina 2" : "Jugador 2");
            formPanel.add(new JLabel("Nombre:"));
            formPanel.add(nameFieldP2);

            countryComboP2 = new JComboBox<>(new String[] { "Colombia", "Canadá", "Argentina", "Alemania" });
            formPanel.add(new JLabel("País / Poder:"));
            formPanel.add(countryComboP2);
        }

        add(formPanel, BorderLayout.CENTER);
    }

    private void prepareViewStartButton(GameContainer container) {
        startBtn = new JButton("Iniciar Partido");
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        startBtn.setBackground(new Color(40, 140, 40));
        startBtn.setForeground(Color.WHITE);
        startBtn.setPreferredSize(new Dimension(200, 45));

        startBtn.addActionListener(e -> {
            String p1Name = nameFieldP1.getText().trim();
            if (p1Name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Jugador 1");
                return;
            }
            String p1Country = (String) countryComboP1.getSelectedItem();

            if (mode == GameMode.PVP || mode == GameMode.MVM) {
                String p2Name = nameFieldP2.getText().trim();
                if (p2Name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Jugador 2");
                    return;
                }
                String p2Country = (String) countryComboP2.getSelectedItem();
                container.onPlayerConfigConfirmed(p1Name, p1Country, p2Name, p2Country);
            } else {
                container.onPlayerConfigConfirmed(p1Name, p1Country);
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(startBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
