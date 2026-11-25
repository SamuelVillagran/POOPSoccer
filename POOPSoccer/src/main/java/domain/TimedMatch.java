package domain;

/**
 * MatchRule strategy that runs a countdown timer and activates sudden death on a tie at zero.
 */
public class TimedMatch implements MatchRule { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/

    private float remainingTimeSeconds;
    private boolean suddenDeath;

    public TimedMatch(int timeLimitMinutes) {
        this(timeLimitMinutes * 60.0f);
    }

    public TimedMatch(float durationSeconds) {
        this.remainingTimeSeconds = Math.max(0.0f, durationSeconds);
        this.suddenDeath = false;
    }

    @Override
    public void update(float deltaTime, Player player1, Player player2) {
        if (suddenDeath) {
            return;
        }
        remainingTimeSeconds -= deltaTime;
        if (remainingTimeSeconds <= 0.0f) {
            remainingTimeSeconds = 0.0f;
            if (player1 != null && player2 != null && player1.getGoals() == player2.getGoals()) {
                player1.applyEffect(new Sudden());
                player2.applyEffect(new Sudden());
                suddenDeath = true;
            }
        }
    }

    @Override
    public boolean isFinished(Player player1, Player player2) {
        if (remainingTimeSeconds <= 0.0f && !suddenDeath) {
            return true;
        }
        return suddenDeath && player1 != null && player2 != null && player1.getGoals() != player2.getGoals();
    }

    @Override
    public float getRemainingTime() {
        return remainingTimeSeconds;
    }

    @Override
    public boolean isSuddenDeath() {
        return suddenDeath;
    }
}
