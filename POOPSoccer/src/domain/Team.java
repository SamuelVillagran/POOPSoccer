package domain;

public enum Team { /*Perfeccionado con Antigravity.*/
    COREA_DEL_SUR("Corea del Sur", new BlueAura()), // País con tiro veloz y recto que empuja al rival
    CAMEROON("Camerún", new Lightning()), // País con rayo eléctrico que ralentiza y bloquea saltos
    NIGERIA("Nigeria", new Tornado()), // País con remolino rasante que lanza al rival por los aires
    USA("Estados Unidos", new Illusion()), // País con ilusiones ópticas de múltiples balones
    COLOMBIA("Colombia", new Illusion()); // País adicional con tiro de poder Ilusión

    private final String displayName; // Nombre legible mostrado en menús y marcador
    private final String spriteKey; // Clave normalizada sin tildes ni espacios para buscar imágenes PNG
    private final Power power; // Tiro de poder único asociado a esta selección nacional

    Team(String displayName, Power power) {
        this.displayName = displayName;
        this.spriteKey = normalize(displayName);
        this.power = power;
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
                .replaceAll("\\p{M}|\\s+|_", "").toLowerCase(); // Remueve acentos, espacios y guiones bajos
    }

    public String getDisplayName() { return displayName; }
    public String getSpriteKey() { return spriteKey; }
    public Power getPower() { return power; }
    public String getPowerName() { return power != null ? power.getName() : ""; }
    public void launchPowerShot(Player owner, Ball ball) {
        if (power != null && ball != null) {
            ball.activatePowerShot(owner, power);
        }
    }
}
