package domain;

public enum NumberPlayer {
    PLAYER1(1),
    PLAYER2(2);

    private int number;

    NumberPlayer(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
