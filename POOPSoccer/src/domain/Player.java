package domain;

public class Player extends ItemSoccer {
    public static final int STUNED_TIME = 3;
    private static final int KICK_ACTIVE_FRAMES = 10;
    private static final int JUMP_HALF_FRAMES = 18;
    private static final int STUN_FRAMES = 180; // 3s at 60FPS
    private static final int KICK_COOLDOWN_FRAMES = 30;
    private int kickCooldown = 0;

    private static final int MAX_STAT = 10;
    private static final int BASE_UPGRADE_COST = 500;
    private static final float BASE_SPEED = 3.0f;
    private static final float BASE_JUMP = 3.0f;
    private static final float BALL_KICK_FORCE = 3.0f;
    private static final float BASE_KICK_DAMAGE = 10.0f;
    private static final float MAX_POWER = 100.0f;
    private static final int POWER_CHARGE_FRAMES = 600; // 10s at 60FPS
    private static final float POWER_RECHARGE_PER_FRAME = MAX_POWER / POWER_CHARGE_FRAMES;
    private static final float GOAL_CONCEDED_POWER_BONUS = MAX_POWER * 0.25f; // +1/4 on goal received
    private boolean kickAlreadyHit = false;

    private int speedStat = 1;
    private int jumpStat = 1;
    private int kickStat = 1;
    private int survivalStat = 1;
    private int points = 0;

    private String name;
    private int goals = 0;
    private int speed;
    private boolean isStuned;
    private int stunFramesLeft = 0;
    private float currentHealth;
    private float currentPower = 0.0f;
    private boolean powerActive = false;
    private Team team;
    private int jumpFrames = 0;
    private int kickFrames = 0;
    private float groundY = -1.0f;

    private PlayerType type;
    private Effect effect;
    private Player rival;
    private NumberPlayer numberPlayer;

    public Player(PlayerType type, String name, Team team, NumberPlayer numberPlayer) {
        this.numberPlayer = numberPlayer;
        this.type = type;
        if (this.type != null) {
            this.type.setPlayer(this);
        }
        this.name = name;
        this.team = team;
        this.speed = Math.round(BASE_SPEED);
        this.currentHealth = getMaxLife();
    }

    public void move(char direction) throws InterruptedException {
        if (isStuned) return; // stunned players can't move
        if (effect != null && effect.getSpeedFactor() <= 0.0f && (direction == 'r' || direction == 'l')) {
            return;
        }
        this.speed = Math.round(getSpeed());
        char effectiveDir = direction;
        if (effect != null && effect.isDirectionInverted()) {
            if (direction == 'r') effectiveDir = 'l';
            else if (direction == 'l') effectiveDir = 'r';
        }
        switch (effectiveDir) {
            case 'r':
                move(this.speed, 0);
                break;
            case 'l':
                move(-this.speed, 0);
                break;
            case 'j':
                jump();
                break;
            case 'k':
                kick();
                break;
            case 'p':
                activatePower();
                break;
            default:
                break;
        }
    }

    private void kick() {
        if (effect != null && !effect.canKick()) return;
        if (kickFrames == 0 && kickCooldown == 0) {
            kickFrames = KICK_ACTIVE_FRAMES;
            kickCooldown = KICK_COOLDOWN_FRAMES;
            kickAlreadyHit = false;   // new kick, it can hurt again
        }

    }

    private void jump() throws InterruptedException {
        if (effect != null && !effect.canJump()) return;
        if (jumpFrames == 0) jumpFrames = 1;
    }

    public void update() {
        updatePowerCharge();
        updateEffect();
        if (updateStun()) return;
        updateTypeAction();
        updatePush();
        updateKick();
        updateJump();
    }

    private void updateTypeAction() {
        if (type != null) {
            // Ejecuta el comportamiento automático cuando el jugador es controlado por la máquina
            type.action();
        }
    }

    private void updatePowerCharge() {
        if (currentPower < MAX_POWER) {
            currentPower = Math.min(MAX_POWER, currentPower + POWER_RECHARGE_PER_FRAME);
        }
    }

    private void updateEffect() {
        if (effect != null) {
            effect.update(1.0f / 60.0f);
            if (effect.isFinished()) {
                effect = null;
            }
        }
    }

    private boolean updateStun() {
        if (!isStuned) return false;
        stunFramesLeft--;
        if (stunFramesLeft <= 0) {
            isStuned = false;
            currentHealth = getMaxLife();
        }
        return true;
    }

    private void updatePush() {
        if (effect != null && effect.isPushed()) {
            move(-getAttackDirection() * Math.round(BASE_SPEED * 1.6f), 0);
        }
    }

    private void updateKick() {
        if (kickFrames > 0) kickFrames--;
        if (kickCooldown > 0) kickCooldown--;
    }

    private void updateJump() {
        if (jumpFrames <= 0) return;
        float jumpStep = 4.5f + (getJumpStat() * 0.6f);
        move(0, (int) (jumpFrames <= JUMP_HALF_FRAMES ? -jumpStep : jumpStep));
        jumpFrames++;

        if (jumpFrames > JUMP_HALF_FRAMES * 2) {
            jumpFrames = 0;
            if (groundY >= 0.0f) {
                position.setPositions(position.getX(), groundY);
            }
        }
    }

    /**
     * Applies damage to this player. If health drops to zero, triggers stun.
     */
    public void takeDamage(float damage) {
        if (isStuned) return;   // already stunned, can't be hit again
        currentHealth = Math.max(0f, currentHealth - damage);
        if (currentHealth <= 0f) {
            isStuned = true;
            stunFramesLeft = STUN_FRAMES;
            // cancel whatever he was doing and put him back on the ground
            jumpFrames = 0;
            kickFrames = 0;
            if (groundY >= 0.0f) {
                position.setPositions(position.getX(), groundY);
            }
        }
    }

    /** Returns health as a 0.0–1.0 fraction for the HUD bar. */
    public float getHealthPercent() {
        return currentHealth / getMaxLife();
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    /** Returns power charge as a 0.0–1.0 fraction for the HUD bar. */
    public float getPowerPercent() {
        return currentPower / MAX_POWER;
    }

    public float getCurrentPower() {
        return currentPower;
    }

    public float getMaxPower() {
        return MAX_POWER;
    }

    public boolean isPowerFull() {
        return currentPower >= MAX_POWER - 0.001f;
    }

    public boolean isPowerActive() {
        return powerActive;
    }

    public void activatePower() {
        if (isStuned || !isPowerFull() || powerActive) return;
        powerActive = true;
        currentPower = 0.0f;
    }

    /** Rule 1: When a player receives a goal, their power bar increases by 1/4 (25%). */
    public void onGoalConceded() {
        currentPower = Math.min(MAX_POWER, currentPower + GOAL_CONCEDED_POWER_BONUS);
    }

    public void triggerActivePowerOnBallHit(Ball ball) {
        if (!powerActive) return;
        powerActive = false;
        if (team != null && ball != null) {
            team.launchPowerShot(this, ball);
        }
    }

    public void triggerActivePowerOnBallHit() {
        if (!powerActive) return;
        powerActive = false;
        if (team != null && rival != null) {
            team.usePower(this, rival);
        }
    }

    public boolean hasActiveEffect() {
        return effect != null && !effect.isFinished();
    }

    public String getActiveEffectName() {
        return hasActiveEffect() ? effect.getClass().getSimpleName().replace("Effect", "") : "";
    }

    public String getActivePowerName() {
        if (team == null || !powerActive) return "Ninguno";
        return team.getPowerName();
    }

    public String getTeamDisplayName() {
        return team != null ? team.getDisplayName() : "";
    }

    public String getDisplayInfo() {
        if (team == null) return name;
        return name + " (" + team.getDisplayName() + ")";
    }

    private void move(int deltaX, int deltaY) {
        position.setPositions(position.getX()+deltaX,
                position.getY()+deltaY);
    }

    public boolean isKicking() {
        return kickFrames > 0;
    }

    public boolean isJumping() {
        return jumpFrames > 0;
    }

    /**
     * +1 if Player 1 (attacks right), -1 if Player 2 (attacks left).
     */
    public int getAttackDirection() {
        return getNumberPlayerInt() == 1 ? 1 : -1;
    }

    public float getSpeed() {
        float factor = (effect != null) ? effect.getSpeedFactor() : 1.0f;
        return BASE_SPEED * (1 + 0.10f * (speedStat - 1)) * factor;
    }

    public int getForce() {
        return Math.round(BALL_KICK_FORCE);
    }

    public float getKickMultiplier() {
        return 1 + 0.10f * (kickStat - 1);
    }

    public float getMaxLife() {
        return 100 * (1 + 0.10f * (survivalStat - 1));
    }

    public float getJumpStat() {
        return BASE_JUMP * (1 + 0.10f * (jumpStat - 1));
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        if (amount > 0) points += amount;
    }

    public int getStatLevel(String stat) {
        return switch (stat.toLowerCase()) {
            case "speed" -> speedStat;
            case "jump" -> jumpStat;
            case "kick" -> kickStat;
            case "survival" -> survivalStat;
            default -> 1;
        };
    }

    public int getUpgradeCost(String stat) {
        int level = getStatLevel(stat);
        return BASE_UPGRADE_COST * (int) Math.pow(2, level - 1);
    }

    public boolean upgradeStat(String stat) {
        int level = getStatLevel(stat);
        int cost = getUpgradeCost(stat);
        if (level >= MAX_STAT || points < cost) return false;
        points -= cost;
        incrementStat(stat);
        currentHealth = getMaxLife();
        return true;
    }

    private void incrementStat(String stat) {
        switch (stat.toLowerCase()) {
            case "speed" -> speedStat++;
            case "jump" -> jumpStat++;
            case "kick" -> kickStat++;
            case "survival" -> survivalStat++;
        }
    }

    public void copyProgressFrom(Player previous) {
        if (previous == null) return;
        this.points = previous.points;
        this.speedStat = previous.speedStat;
        this.jumpStat = previous.jumpStat;
        this.kickStat = previous.kickStat;
        this.survivalStat = previous.survivalStat;
        this.currentHealth = getMaxLife();
    }

    public void setGroundY(float groundY) {
        this.groundY = groundY;
    }

    public float getGroundY() {
        return groundY;
    }

    @Override
    public float getWidth() {
        return 60.0f;
    }

    @Override
    public float getHeight() {
        return 70.0f;
    }

    @Override
    public String getVariantName() {
        if (getTeamDisplayName().isEmpty() || getNumberPlayerInt() == 0) {
            return "normal";
        }
        String teamKey = java.text.Normalizer.normalize(getTeamDisplayName().trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}|\\s+", "").toLowerCase();
        String suffix = isKicking() ? "_kick" : "";
        return teamKey + getNumberPlayerInt() + suffix;
    }

    @Override
    public boolean hasPowerAura() {
        return isPowerActive();
    }

    @Override
    public boolean isStunnedStatus() {
        return isStuned();
    }

    @Override
    public String getStatusText() {
        if (isStuned) return "¡ATURDIDO!";
        return getActiveEffectName();
    }

    @Override
    public boolean isMovible(){
        return true;
    }

    public void doGoal() {
        goals++;
    }

    public int getGoals() {
        return goals;
    }

    public void usePower(Effect power) {
        this.effect = power;
    }

    public String getName() {
        return name;
    }

    public Team getTeam() {
        return team;
    }

    public int getNumberPlayerInt() {
        return numberPlayer == null ? 0 : numberPlayer.getNumber();
    }

    public Player getRival() {
        return rival;
    }

    public void setRival(Player rival) {
        this.rival = rival;
    }

    public boolean isStuned() {
        return isStuned;
    }

    /** Damage = 10 x kick multiplier. Only the first frame of the kick that reaches the rival counts.
     */
    public void hitWithKick(Player target) {
        if (!isKicking() || kickAlreadyHit) return;
        target.takeDamage(BASE_KICK_DAMAGE * getKickMultiplier());
        kickAlreadyHit = true;
    }

    /** Called when the kick touched the ball: per the spec, then it only pushes the ball.
     */
    public void markKickUsed() {
        kickAlreadyHit = true;
    }

    public void applyEffect(Effect newEffect) {   // Rule 8: new effect replaces previous one
        if (newEffect != null) {
            effect = newEffect;
            if (newEffect.launchesIntoAir() && !isStuned && jumpFrames == 0) {
                jumpFrames = 1;
            }
        }
    }
}
