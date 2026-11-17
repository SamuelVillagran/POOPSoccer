package domain;

import java.util.Random;

/**
 * Clumsy machine player that chooses its movements at random.
 */
public class Machine extends PlayerType {
    private static final char[] POSSIBLE_ACTIONS = { 'l', 'r', 'j', 'k', 'p' };
    private static final int DECISION_FRAMES = 15;

    private final Random random = new Random();
    private char currentDirection = 'l';
    private int framesUntilNextDecision = 0;

    public Machine() {
    }

    @Override
    public void action() {
        if (player == null) return;
        // Cuando termina el ciclo actual, elige una nueva acción al azar
        if (framesUntilNextDecision <= 0) {
            chooseRandomAction();
            framesUntilNextDecision = DECISION_FRAMES;
        }
        framesUntilNextDecision--;
        executeCurrentMove();
    }

    private void chooseRandomAction() {
        char chosen = POSSIBLE_ACTIONS[random.nextInt(POSSIBLE_ACTIONS.length)];
        if (chosen == 'l' || chosen == 'r') {
            // Mantiene la dirección horizontal durante varios frames para avanzar de verdad
            currentDirection = chosen;
        } else {
            // Ejecuta salto, patada o poder en el instante en que sale elegido
            triggerMove(chosen);
        }
    }

    private void executeCurrentMove() {
        triggerMove(currentDirection);
    }

    private void triggerMove(char moveCode) {
        try {
            player.move(moveCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
