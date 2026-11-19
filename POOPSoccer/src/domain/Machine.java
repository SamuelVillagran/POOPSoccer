package domain;

import java.util.Random;

/**
 * Clumsy machine player that chooses its movements at random.
 */
public class Machine extends Type {
    private static final char[] POSSIBLE_ACTIONS = { 'l', 'r', 'j', 'k', 'p' }; // Acciones válidas: izquierda, derecha, salto, patada y poder
    private static final int DECISION_FRAMES = 15; // Cambia de decisión cada 15 frames (cuatro veces por segundo)

    private final Random random = new Random(); // Generador pseudoaleatorio para elegir movimientos
    private char currentDirection = 'l'; // Mantiene la última dirección horizontal elegida entre decisiones
    private int framesUntilNextDecision = 0; // Cuenta regresiva hasta sortear la siguiente acción

    public Machine() {
    }

    @Override
    public void action() {
        if (player == null) throw new POOPSoccerException(POOPSoccerException.PLAYER_NOT_ASSIGNED);
        if (framesUntilNextDecision <= 0) {
            chooseRandomAction(); // Sortea una nueva acción al agotarse el intervalo actual
            framesUntilNextDecision = DECISION_FRAMES;
        }
        framesUntilNextDecision--;
        executeCurrentMove();
    }

    private void chooseRandomAction() {
        char chosen = POSSIBLE_ACTIONS[random.nextInt(POSSIBLE_ACTIONS.length)];
        if (chosen == 'l' || chosen == 'r') {
            currentDirection = chosen; // Conserva el sentido horizontal durante el intervalo para desplazarse
        } else {
            player.move(chosen); // Ejecuta salto, patada o poder en el instante en que resulta elegido
        }
    }

    private void executeCurrentMove() {
        player.move(currentDirection); // Aplica el desplazamiento horizontal sostenido hacia la dirección activa
    }
}
