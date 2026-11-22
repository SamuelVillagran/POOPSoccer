package presentation;

import java.awt.Dimension;

/**
 * Listener contract for navigation and match setup confirmation triggered from PlayerConfig.
 */
public interface PlayerConfigListener { /*Perfeccionado con Antigravity.*/

    void goToMenu();

    void setResolution(Dimension resolution);

    void onPlayerConfigConfirmed(String nameP1, String teamP1, String nameP2, String teamP2,
                                 String matchType, int matchLimit);
}
