package domain;

public class Player extends ItemSoccer {
    private static final int KICK_ACTIVE_FRAMES = 10; // Duración en frames de la animación y alcance de patada
    private static final int JUMP_HALF_FRAMES = 18; // Frames de subida del salto; el ciclo total dura el doble
    private static final int STUN_FRAMES = 180; // Duración del aturdimiento: 3 segundos exactos a 60 FPS
    private static final int KICK_COOLDOWN_FRAMES = 30; // Pausa de 0.5 segundos antes de permitir otra patada

    private static final float BASE_SPEED = 3.0f; // Velocidad horizontal base en píxeles por frame
    private static final float BASE_JUMP = 3.0f; // Estadística base usada en el cálculo de altura de salto
    private static final float BALL_KICK_FORCE = 3.0f; // Impulso constante aplicado al balón al patearlo
    private static final float BASE_KICK_DAMAGE = 10.0f; // Daño base de una patada antes del multiplicador
    private static final float MAX_POWER = 100.0f; // Capacidad total de la barra de carga de poder
    private static final int POWER_CHARGE_FRAMES = 600; // Tiempo de recarga completo: 10 segundos a 60 FPS
    private static final float POWER_RECHARGE_PER_FRAME = MAX_POWER / POWER_CHARGE_FRAMES; // Incremento por frame de la barra de poder
    private static final float GOAL_CONCEDED_POWER_BONUS = MAX_POWER * 0.25f; // Recarga el 25% de la barra cuando recibe un gol

    private int kickCooldown = 0; // Frames restantes antes de poder patear nuevamente
    private boolean kickAlreadyHit = false; // Evita que una misma patada golpee en varios frames consecutivos
    private transient boolean pendingKickStarted = false; // Señal temporal para reproducir sonido de patada al aire
    private transient boolean pendingKickBallImpact = false; // Señal temporal para reproducir sonido de golpe al balón

    private final PlayerProfile profile = new PlayerProfile(); // Perfil persistente con puntos, estadísticas e historial
    private String name; // Nombre visible del jugador en la interfaz
    private int goals = 0; // Goles anotados en el partido actual
    private boolean isStuned; // Indica si el jugador perdió toda su vida y está aturdido
    private int stunFramesLeft = 0; // Frames restantes para salir del aturdimiento
    private float currentHealth; // Vida actual antes de quedar aturdido
    private float currentPower = 0.0f; // Carga actual de la barra de poder (0 a 100)
    private boolean powerActive = false; // Indica si el jugador activó su aura de poder y espera tocar el balón
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
        this.currentHealth = getMaxLife();
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public void restoreFullHealth() {
        this.currentHealth = getMaxLife(); // Sincroniza la vida actual con el nivel de resistencia del perfil
    }

    public void addPoints(int amount) {
        profile.addPoints(amount);
    }

    public void move(char direction) {
        if (isStuned) return; // Bloquea toda acción mientras el jugador está aturdido
        if (effect != null && effect.getSpeedFactor() <= 0.0f && (direction == 'r' || direction == 'l')) {
            return; // Impide caminar si el efecto actual inmoviliza al jugador
        }
        int moveSpeed = Math.round(getSpeed());
        switch (direction) {
            case 'r' -> move(moveSpeed, 0);
            case 'l' -> move(-moveSpeed, 0);
            case 'j' -> jump();
            case 'k' -> kick();
            case 'p' -> activatePower();
            default -> {}
        }
    }

    private void kick() {
        if (effect != null && !effect.canKick()) return;
        if (kickFrames == 0 && kickCooldown == 0) {
            kickFrames = KICK_ACTIVE_FRAMES;
            kickCooldown = KICK_COOLDOWN_FRAMES;
            kickAlreadyHit = false; // Reinicia el permiso de impacto para la nueva patada
            pendingKickStarted = true;
        }
    }

    private void jump() {
        if (effect != null && !effect.canJump()) return;
        if (jumpFrames == 0) {
            jumpFrames = 1; // Inicia el conteo del salto desde el primer frame
        }
    }

    @Override
    public void update() {
        if (currentPower < MAX_POWER) {
            currentPower = Math.min(MAX_POWER, currentPower + POWER_RECHARGE_PER_FRAME); // Recarga la barra gradualmente hasta el 100%
        }
        updateEffect();
        if (updateStun()) return; // Detiene movimiento y acciones mientras dure el aturdimiento
        if (type != null) {
            type.action(); // Delega la decisión automática cuando el jugador pertenece a la máquina
        }
        updatePush();
        if (kickFrames > 0) kickFrames--;
        if (kickCooldown > 0) kickCooldown--;
        updateJump();
    }

    private void updateEffect() {
        if (effect != null) {
            effect.update(1.0f / 60.0f); // Descuenta un frame (1/60 s) del efecto temporal activo
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
            restoreFullHealth(); // Restaura la vida completa al despertar del aturdimiento
        }
        return true;
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
        if (isStuned) return; // Ignora daño adicional si el jugador ya se encuentra aturdido
        currentHealth = Math.max(0f, currentHealth - damage);
        if (currentHealth <= 0f) {
            isStuned = true;
            stunFramesLeft = STUN_FRAMES;
            jumpFrames = 0; // Cancela cualquier salto pendiente al caer aturdido
            kickFrames = 0;
            if (groundY >= 0.0f) {
                position.setPositions(position.getX(), groundY); // Devuelve el personaje al suelo al iniciar el aturdimiento
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

    public boolean isPowerFull() {
        return currentPower >= MAX_POWER - 0.001f;
    }

    @Override
    public boolean isPowerActive() {
        return powerActive;
    }

    public void activatePower() {
        if (isStuned || !isPowerFull() || powerActive) return;
        powerActive = true;
        currentPower = 0.0f; // Vacía la barra al instante para reiniciar su recarga de inmediato
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

    public String getActivePowerName() {
        if (team == null || !powerActive) return "Ninguno";
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
        if (isStuned) return "¡ATURDIDO!";
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
        return isStuned;
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
            pendingKickBallImpact = true;
            pendingKickStarted = false;
        }
    }

    public boolean consumePendingKickStarted() {
        boolean pending = pendingKickStarted;
        pendingKickStarted = false;
        return pending;
    }

    public boolean consumePendingKickBallImpact() {
        boolean pending = pendingKickBallImpact;
        pendingKickBallImpact = false;
        return pending;
    }

    public void applyEffect(Effect newEffect) {
        if (newEffect != null) {
            effect = newEffect; // Reemplaza el efecto anterior sin acumularlos
            if (newEffect.launchesIntoAir() && !isStuned && jumpFrames == 0) {
                jumpFrames = 1; // Eleva al rival automáticamente cuando el efecto lo lanza por los aires
            }
        }
    }
}
