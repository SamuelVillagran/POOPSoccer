package presentation;

import javax.swing.SwingUtilities;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Punto de entrada principal (Main) de la aplicación en la capa de presentación.
 * 
 * Arquitectura:
 * - Esta clase coordina el arranque de la interfaz gráfica en el hilo de despacho de eventos de Swing (EDT).
 * - Delega la creación y visualización de la ventana principal a la clase {@link Window} (JFrame).
 * - Resuelve el problema arquitectónico de la versión original donde un JPanel se llamaba '...GUI'
 *   y la ventana secundaria contenía el método main.
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
     * Muestra la ventana principal del juego.
     */
    public void start() {
        if (window != null) {
            window.setVisible(true);
        }
    }

    /**
     * Método principal de ejecución.
     */
    public static void main(String[] args) { //Ayudado con Gemini Flash 3.8 IA
        SwingUtilities.invokeLater(() -> {
            POOPSoccerGUI gui = new POOPSoccerGUI();
            gui.start();
        });
    }
}
