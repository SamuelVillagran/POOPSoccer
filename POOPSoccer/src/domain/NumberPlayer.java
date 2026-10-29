package domain;

public enum NumberPlayer {
    PLAYER1(1, Side.LEFT),
    PLAYER2(2, Side.RIGTH);

    private int number;
    private Side initialSide;

    NumberPlayer(int number, Side initialSide) {
        this.number = number;
        this.initialSide = initialSide;
    }

    public int getNumber() {
        return 0;
    }

    public Side getInitialSide() {
        return null;
    }
}
