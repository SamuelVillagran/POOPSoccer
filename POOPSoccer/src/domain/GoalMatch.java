package domain;

/**
 * MatchRule strategy that ends the match when either player reaches the goal limit.
 */
public class GoalMatch implements MatchRule { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/

    private final int goalLimit;

    public GoalMatch(int goalLimit) {
        this.goalLimit = Math.max(1, goalLimit);
    }

    public int getGoalLimit() {
        return goalLimit;
    }

    @Override
    public boolean isFinished(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            return false;
        }
        return player1.getGoals() >= goalLimit || player2.getGoals() >= goalLimit;
    }
}
