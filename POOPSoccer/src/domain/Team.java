package domain;

public enum Team {// Idea inspirada por Claude Sonnet 4.6 IA, se consultó
    CANADA("Canada", new Freeze(), 3, 4, 3),
    COLOMBIA("Colombia", new Confusion(), 4, 3, 4),
    ALEMANIA("Alemania", new Shrink(),  3, 3, 4);

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

    public static Team fromDisplayName(String name) {//Ayudado con Claude Sonnet 4.6 IA
        if (name == null) throw new IllegalArgumentException("Equipo nulo");
        String wanted = normalize(name);
        for (Team team : values()) {
            if (normalize(team.displayName).equals(wanted)) return team;
        }
        throw new IllegalArgumentException("Equipo desconocido: [" + name + "]");
    }

    private static String normalize(String s) {//Ayudado con Claude Sonnet 4.6 IA
        return java.text.Normalizer.normalize(s.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
    }

    public String getDisplayName() { return displayName; }
    public Power getPower() { return POWER; }
    public int getSpeed() { return speed; }
    public int getJump() { return JUMP; }
    public int getForce() { return force; }
    public void setSpeed(int newSpeed) { speed = newSpeed; }


}
