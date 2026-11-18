package domain;

public enum NumberPlayer {
    PLAYER1(1), // Identifica al jugador ubicado a la izquierda que ataca hacia la derecha
    PLAYER2(2); // Identifica al jugador ubicado a la derecha que ataca hacia la izquierda

    private final int number; // Número entero usado para sprites, arcos y dirección de ataque

    NumberPlayer(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
