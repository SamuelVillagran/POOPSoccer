package domain;

public class ScoreBoard {
    private float remainingTime;
    private boolean isFinished;
    private int goalsPy1;
    private int goalsPy2;

    public ScoreBoard() {
    }

    public ScoreBoard(int goalLimit) {

    }

    public void registerGoal(int player) {
    }

    public boolean isGameOver() {
        return false;
    }

    public Side checkWinner() {
        return Side.RIGTH;
    }

    public int getGoalsP1() {
        return goalsPy1;
    }

    public int getGoalsP2() {
        return goalsPy2;
    }

    public float getRemainingTime() {
        return remainingTime;
    }

}
