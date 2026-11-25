package domain;

import java.util.List;

public class Player extends ItemSoccer { /*Perfeccionado con Antigravity.*/
    private static final int KICK_ACTIVE_FRAMES = 10; // Duración en frames de la animación y alcance de patada
    private static final int JUMP_HALF_FRAMES = 18; // Frames de subida del salto; el ciclo total dura el doble
    private static final int KICK_COOLDOWN_FRAMES = 30; // Pausa de 0.5 segundos antes de permitir otra patada
    private static final float FRAME_DELTA_TIME = 1.0f / 60.0f; // Paso temporal fijo por frame (60 FPS)

    private static final float BASE_SPEED = 3.0f; // Velocidad horizontal base en píxeles por frame
    private static final float BASE_JUMP = 3.0f; // Estadística base usada en el cálculo de altura de salto
    private static final float BALL_KICK_FORCE = 3.0f; // Impulso constante aplicado al balón al patearlo
    private static final float BASE_KICK_DAMAGE = 10.0f; // Daño base de una patada antes del multiplicador

    private int kickCooldown = 0; // Frames restantes antes de poder patear nuevamente
    private boolean kickAlreadyHit = false; // Evita que una misma patada golpee en varios frames consecutivos
    private transient boolean kickPerformed = false; // Señal temporal de patada ejecutada en el último paso
    private transient boolean kickBallContact = false; // Señal temporal de impacto del pie contra el balón

    private final PlayerProfile profile = new PlayerProfile(); // Perfil persistente con puntos, estadísticas e historial
    private final PlayerHealth health; // Estado de salud y aturdimiento encapsulado
    private final PowerCharge powerCharge = new PowerCharge(); // Barra de recarga y aura de poder encapsulada
    private String name; // Nombre visible del jugador en la interfaz
    private int goals = 0; // Goles anotados en el partido actual
    private Team team; // País seleccionado que determina aspecto y tiro de poder
    private int jumpFrames = 0; // Contador de avance dentro de la parábola de salto
    private int kickFrames = 0; // Contador regresivo mientras el pie permanece extendido
    private float groundY = -1.0f; // Coordenada Y exacta del suelo para aterrizar con precisión

    private Type type; // Estrategia de control: humano (Normal) o IA (Machine)
    private Effect effect; // Efecto rival activo sobre este jugador (empuje, rayo, etc.)
    private NumberPlayer numberPlayer; // Posición del jugador en la cancha (1 izquierda, 2 derecha)

    public Player(Type type, String name, Team team, NumberPlayer numberPlayer) {
        this.numberPlayer = numberPlayer;
        this.type = type;
        if (this.type != null) {
            this.type.setPlayer(this);
        }
        this.name = name;
        this.team = team;
        this.health = new PlayerHealth(profile.getMaxLife());
    }

    public boolean upgradeStat(PlayerStat stat) {
        if (profile.upgradeStat(stat)) {
            restoreFullHealth();
            return true;
        }
        return false;
    }

    public void applyProfileSnapshot(PlayerProfileSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        profile.applySnapshot(snapshot);
        restoreFullHealth();
    }

    public void resetProfile() {
        profile.reset();
        restoreFullHealth();
    }

    public void copyProfileFrom(Player previous) {
        if (previous == null) {
            return;
        }
        profile.copyFrom(previous.profile);
        restoreFullHealth();
    }

    public void recordMatchResult(boolean won, int goalsScored, int goalsConceded) {
        profile.recordMatchResult(won, goalsScored, goalsConceded);
    }

    public PlayerProfileSnapshot toProfileSnapshot() {
        return profile.toSnapshot(name);
    }

    public int getPoints() {
        return profile.getPoints();
    }

    public int getMatchesPlayed() {
        return profile.getMatchesPlayed();
    }

    public int getWins() {
        return profile.getWins();
    }

    public int getGoalsFor() {
        return profile.getGoalsFor();
    }

    public int getGoalsAgainst() {
        return profile.getGoalsAgainst();
    }

    public int getStatLevel(PlayerStat stat) {
        return profile.getStatLevel(stat);
    }

    public int getUpgradeCost(PlayerStat stat) {
        return profile.getUpgradeCost(stat);
    }

    public String getTeamDisplayName() {
        return team != null ? team.getDisplayName() : "";
    }

    public int getDecoyBallCount() {
        return team != null ? team.getDecoyBallCount() : 0;
    }

    public float getDecoyDurationSeconds() {
        return team != null ? team.getDecoyDurationSeconds() : 0.0f;
    }

    public void restoreFullHealth() {
        health.restoreFull(getMaxLife()); // Sincroniza la vida actual con el nivel de resistencia del perfil
    }

    public void addPoints(int amount) {
        profile.addPoints(amount);
    }

    /**
     * Executes an action command on this player unless stunned.
     */
    public void execute(PlayerAction action) {
        if (isStuned() || action == null) return; // Bloquea toda acción mientras el jugador está aturdido
        action.execute(this);
    }

    public void moveLeft() {
        moveHorizontal(-1);
    }

    public void moveRight() {
        moveHorizontal(1);
    }

    private void moveHorizontal(int directionSign) {
        if (effect != null && effect.getSpeedFactor() <= 0.0f) return; // Impide caminar si el efecto actual inmoviliza al jugador
        move(directionSign * Math.round(getSpeed()), 0);
    }

    public void kick() {
        if (effect != null && !effect.canKick()) return;
        if (kickFrames == 0 && kickCooldown == 0) {
            kickFrames = KICK_ACTIVE_FRAMES;
            kickCooldown = KICK_COOLDOWN_FRAMES;
            kickAlreadyHit = false; // Reinicia el permiso de impacto para la nueva patada
            kickPerformed = true;
        }
    }

    public void jump() {
        if (effect != null && !effect.canJump()) return;
        if (jumpFrames == 0) {
            jumpFrames = 1; // Inicia el conteo del salto desde el primer frame
        }
    }

    @Override
    public void update() {
        powerCharge.rechargeStep();
        updateEffect();
        if (health.updateStun(getMaxLife())) return; // Detiene movimiento y acciones mientras dure el aturdimiento
        if (type != null) {
            type.action(); // Delega la decisión automática cuando el jugador pertenece a la máquina
        }
        updatePush();
        if (kickFrames > 0) kickFrames--;
        if (kickCooldown > 0) kickCooldown--;
        updateJump();
    }

    private void updateEffect() {
        Effect activeEffect = this.effect;
        if (activeEffect != null) {
            activeEffect.update(FRAME_DELTA_TIME); // Descuenta un frame (1/60 s) del efecto temporal activo
            activeEffect.onTick(this, FRAME_DELTA_TIME);
            if (this.effect == activeEffect && activeEffect.isFinished()) {
                this.effect = null;
                activeEffect.onExpire(this);
            }
        }
    }

    private void updatePush() {
        if (effect != null && effect.isPushed()) {
            move(-getAttackDirection() * Math.round(BASE_SPEED * 1.6f), 0); // Empuja al jugador hacia su propio arco
        }
    }

    private void updateJump() {
        if (jumpFrames <= 0) return;
        float jumpStep = 4.5f + (getJumpStat() * 0.6f); // Calcula el desplazamiento vertical según la estadística de salto
        move(0, (int) (jumpFrames <= JUMP_HALF_FRAMES ? -jumpStep : jumpStep)); // Sube en la primera mitad y desciende en la segunda
        jumpFrames++;

        if (jumpFrames > JUMP_HALF_FRAMES * 2) {
            jumpFrames = 0;
            if (groundY >= 0.0f) {
                position.setPositions(position.getX(), groundY); // Alinea los pies exactamente con la altura del césped
            }
        }
    }

    /**
     * Applies damage to this player. If health drops to zero, triggers stun.
     */
    public void takeDamage(float damage) {
        if (health.takeDamage(damage)) {
            jumpFrames = 0; // Cancela cualquier salto pendiente al caer aturdido
            kickFrames = 0;
            if (groundY >= 0.0f) {
                position.setPositions(position.getX(), groundY); // Devuelve el personaje al suelo al iniciar el aturdimiento
            }
        }
    }

    /**
     * Restores up to the given health amount, clamped to maxLife, unless stunned.
     */
    public void heal(float amount) {
        health.heal(amount, getMaxLife());
    }

    /**
     * Adds charge to this player's power bar.
     */
    public void addPowerCharge(float amount) {
        powerCharge.addCharge(amount);
    }

    /**
     * Drains charge from this player's power bar.
     */
    public void drainPowerCharge(float amount) {
        powerCharge.drainCharge(amount);
    }

    /** Returns health as a 0.0–1.0 fraction for the HUD bar. */
    public float getHealthPercent() {
        return health.getPercent(getMaxLife());
    }

    public float getCurrentHealth() {
        return health.getCurrentHealth();
    }

    /** Returns power charge as a 0.0–1.0 fraction for the HUD bar. */
    public float getPowerPercent() {
        return powerCharge.getPercent();
    }

    public boolean isPowerFull() {
        return powerCharge.isFull();
    }

    @Override
    public boolean isPowerActive() {
        return powerCharge.isActive();
    }

    public void activatePower() {
        if (!isStuned()) {
            powerCharge.activate();
        }
    }

    /** Rule 1: When a player receives a goal, their power bar increases by 1/4 (25%). */
    public void onGoalConceded() {
        powerCharge.onGoalConceded();
    }

    public void triggerActivePowerOnBallHit(Ball ball) {
        if (!powerCharge.consumeActive()) return;
        if (team != null && ball != null) {
            team.launchPowerShot(this, ball);
        }
    }

    public String getActivePowerName() {
        if (team == null || !powerCharge.isActive()) return "";
        return team.getPowerName();
    }

    public String getDisplayInfo() {
        if (team == null) return name;
        return name + " (" + team.getDisplayName() + ")";
    }

    private void move(int deltaX, int deltaY) {
        position.setPositions(position.getX() + deltaX, position.getY() + deltaY);
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
        return (numberPlayer != null && numberPlayer.getNumber() == 1) ? 1 : -1;
    }

    public float getSpeed() {
        float factor = (effect != null) ? effect.getSpeedFactor() : 1.0f;
        return BASE_SPEED * profile.getStatBonusMultiplier(PlayerStat.SPEED) * factor; // Aumenta 10% por nivel y aplica efecto activo
    }

    public int getForce() {
        return Math.round(BALL_KICK_FORCE);
    }

    public float getKickMultiplier() {
        return profile.getStatBonusMultiplier(PlayerStat.KICK); // Suma 10% al daño de golpes por cada nivel sobre 1
    }

    public float getMaxLife() {
        return profile.getMaxLife();
    }

    public float getJumpStat() {
        return BASE_JUMP * profile.getStatBonusMultiplier(PlayerStat.JUMP); // Incrementa el alcance del salto en 10% por nivel
    }

    public void setGroundY(float groundY) {
        this.groundY = groundY;
    }

    @Override
    public float getWidth() {
        return 60.0f; // Ancho del hitbox y sprite del jugador en píxeles
    }

    @Override
    public float getHeight() {
        return 70.0f; // Alto del hitbox y sprite del jugador en píxeles
    }

    @Override
    public String getVariantName() {
        int playerNum = numberPlayer == null ? 0 : numberPlayer.getNumber();
        if (team == null || playerNum == 0) {
            return "normal";
        }
        String suffix = isKicking() ? "_kick" : ""; // Añade sufijo de animación cuando el jugador extiende el pie
        return team.getSpriteKey() + playerNum + suffix; // Resuelve el sprite con la clave precalculada del país
    }

    @Override
    public String getStatusText() {
        return (effect != null && !effect.isFinished()) ? effect.getClass().getSimpleName() : ""; // Muestra el nombre del efecto activo
    }

    @Override
    public boolean isMovible() {
        return true;
    }

    public void doGoal() {
        goals++;
    }

    public int getGoals() {
        return goals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public boolean isStuned() {
        return health.isStunned();
    }

    /** Damage = 10 x kick multiplier. Only the first frame of the kick that reaches the rival counts. */
    public void hitWithKick(Player target) {
        if (!isKicking() || kickAlreadyHit) return;
        target.takeDamage(BASE_KICK_DAMAGE * getKickMultiplier()); // Aplica el daño solo en el primer frame que conecta
        kickAlreadyHit = true;
    }

    /** Called when the kick touched the ball: per the spec, then it only pushes the ball. */
    public void markKickUsed() {
        if (!kickAlreadyHit) {
            kickAlreadyHit = true;
            kickBallContact = true;
            kickPerformed = false;
        }
    }

    /**
     * Appends pending gameplay interaction events produced by this player and clears internal flags.
     */
    public void drainMatchEvents(List<MatchEvent> out) {
        if (out == null) {
            return;
        }
        if (kickBallContact) {
            out.add(MatchEvent.BALL_KICKED);
        } else if (kickPerformed) {
            out.add(MatchEvent.KICK_SWING);
        }
        kickBallContact = false;
        kickPerformed = false;
    }

    public void applyEffect(Effect newEffect) {
        if (newEffect != null) {
            effect = newEffect; // Reemplaza el efecto anterior sin acumularlos
            newEffect.onApply(this);
            if (newEffect.launchesIntoAir() && !isStuned() && jumpFrames == 0) {
                jumpFrames = 1; // Eleva al rival automáticamente cuando el efecto lo lanza por los aires
            }
        }
    }
}
