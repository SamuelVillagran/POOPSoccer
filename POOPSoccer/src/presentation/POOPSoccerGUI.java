package presentation;

import javax.swing.SwingUtilities;

/**
 * Main entry point of the application in the presentation layer.
 */
public class POOPSoccerGUI {

    private static Window window;

    public POOPSoccerGUI() {
        try {
            this.window = new Window();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the main window of the game.
     */
    public void start() {
        if (window != null) {
            window.setVisible(true);
        }
    }

    /**
     * Main execution method that starts the UI on the Swing event dispatch thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            POOPSoccerGUI gui = new POOPSoccerGUI();
            gui.start();
        });
    }
}
