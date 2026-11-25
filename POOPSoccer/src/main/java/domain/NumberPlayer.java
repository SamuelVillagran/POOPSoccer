package domain;

public enum NumberPlayer { /*Perfeccionado con Antigravity.*/
    PLAYER1(1), // Identifica al jugador ubicado a la izquierda que ataca hacia la derecha
    PLAYER2(2); // Identifica al jugador ubicado a la derecha que ataca hacia la izquierda

    private final int number; // Número entero usado para sprites, arcos y dirección de ataque

    NumberPlayer(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    /**
     * Parses a 1-based player slot number into its closed domain enum.
     */
    public static NumberPlayer fromNumber(int playerNumber) {
        for (NumberPlayer slot : values()) {
            if (slot.number == playerNumber) {
                return slot;
            }
        }
        throw new POOPSoccerException(POOPSoccerException.INVALID_PLAYER_SLOT + playerNumber);
    }
}
