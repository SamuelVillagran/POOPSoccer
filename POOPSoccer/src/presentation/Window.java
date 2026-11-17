package presentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Ventana principal (JFrame) de la aplicación.
 * 
 * Responsabilidades:
 * 1. Configurar propiedades de la ventana (título, tamaño, posición, cierre).
 * 2. Gestionar la barra de menús (JMenuBar) y opciones de archivo (Guardar, Abrir, Salir).
 * 3. Contener el {@link GameContainer}, que administra las vistas mediante CardLayout.
 */
public class Window extends JFrame {

    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem optionNew;
    private JMenuItem optionSaveAs;
    private JMenuItem optionOpen;
    private JMenuItem optionImport;
    private JMenuItem optionExportAs;
    private JMenuItem optionExit;

    private GameContainer gameContainer;

    public Window() throws Exception {
        prepareView();
        prepareActions();
    }

    private void prepareView() throws Exception {
        setScreen();
        prepareViewMenu();
    }

    private void setScreen() throws Exception {
        setResizable(false);
        setTitle("The DOPO Hardest Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gameContainer = new GameContainer();
        add(gameContainer);
        pack();
        setLocationRelativeTo(null);
    }

    private void prepareViewMenu() {
        menuBar = new JMenuBar();
        menu = new JMenu("Archivo");

        optionNew = new JMenuItem("Nuevo");
        optionSaveAs = new JMenuItem("Guardar");
        optionOpen = new JMenuItem("Abrir");
        optionImport = new JMenuItem("Importar");
        optionExportAs = new JMenuItem("Exportar como");
        optionExit = new JMenuItem("Salir");

        menu.add(optionNew);
        menu.addSeparator();
        menu.add(optionSaveAs);
        menu.add(optionOpen);
        menu.addSeparator();
        menu.add(optionExportAs);
        menu.add(optionImport);
        menu.addSeparator();
        menu.add(optionExit);

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void prepareActions() {
        optionNew.addActionListener(e -> newAction());
        optionSaveAs.addActionListener(e -> saveAsAction());
        optionOpen.addActionListener(e -> openAction());
        optionExit.addActionListener(e -> exitAction());
    }

    private void newAction() {
        if (gameContainer != null) {
            gameContainer.restartFromLevel1();
        }
    }

    private void saveAsAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("DAT Files (*.dat)", "dat"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().endsWith(".dat")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".dat");
            }
            try {
                // Delegamos el guardado al contenedor principal y avisamos al usuario
                if (gameContainer != null) {
                    gameContainer.saveCurrentGame(selectedFile);
                }
                JOptionPane.showMessageDialog(this, "Partida guardada en: " + selectedFile.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar archivo:\n" + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("DAT Files (*.dat)", "dat"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                if (gameContainer != null) {
                    gameContainer.loadSavedGame(selectedFile);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir archivo:\n" + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exitAction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea salir del juego?", "Confirmación de salida",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

}
