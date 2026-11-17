package domain;

public enum Team {
    COREA_DEL_SUR("Corea del Sur", new BlueAura(), 3, 3, 3),
    CAMEROON("Camerún", new Lightning(), 3, 3, 3),
    NIGERIA("Nigeria", new Tornado(), 3, 3, 3),
    USA("Estados Unidos", new Illusion(), 3, 3, 3),
    COLOMBIA("Colombia", new Illusion(), 4, 3, 4);

    private final String displayName;
    private final Power POWER;
    private int speed;
    private final int JUMP;
    private int force;

    Team(String displayName, Power power, int speed, int jump, int force) {
        this.displayName = displayName;
        this.POWER = power;
        this.speed = speed;
        this.JUMP = jump;
        this.force = force;
    }

    public static Team fromDisplayName(String name) {
        if (name == null) throw new IllegalArgumentException("Equipo nulo");
        String wanted = normalize(name);
        for (Team team : values()) {
            String normDisplay = normalize(team.displayName);
            String normEnum = normalize(team.name());
            if (normDisplay.equals(wanted) || normEnum.equals(wanted)
                    || wanted.contains(normDisplay) || wanted.contains(normEnum)) {
                return team;
            }
        }
        throw new IllegalArgumentException("Equipo desconocido: [" + name + "]");
    }

    private static String normalize(String s) {
        return java.text.Normalizer.normalize(s.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}|\\s+|_", "").toLowerCase();
    }

    public String getDisplayName() { return displayName; }
    public Power getPower() { return POWER; }
    public String getPowerName() { return POWER != null ? POWER.getName() : "Ninguno"; }
    public void usePower(Player owner, Player target) {
        if (POWER != null) {
            POWER.use(owner, target);
        }
    }
    public void launchPowerShot(Player owner, Ball ball) {
        if (POWER != null && ball != null) {
            ball.activatePowerShot(owner, POWER);
        }
    }
    public int getSpeed() { return speed; }
    public int getJump() { return JUMP; }
    public int getForce() { return force; }
    public void setSpeed(int newSpeed) { speed = newSpeed; }
}
