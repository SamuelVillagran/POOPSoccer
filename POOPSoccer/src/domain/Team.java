package domain;

public enum Team {
    COREA_DEL_SUR("Corea del Sur", new BlueAura()),
    CAMEROON("Camerún", new Lightning()),
    NIGERIA("Nigeria", new Tornado()),
    USA("Estados Unidos", new Illusion()),
    COLOMBIA("Colombia", new Illusion());

    private final String displayName;
    private final Power POWER;

    Team(String displayName, Power power) {
        this.displayName = displayName;
        this.POWER = power;
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
    public void launchPowerShot(Player owner, Ball ball) {
        if (POWER != null && ball != null) {
            ball.activatePowerShot(owner, POWER);
        }
    }
}
