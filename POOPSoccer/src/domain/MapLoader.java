package domain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;

/**
 * Builds the board elements (players, ball, pitch and goals) from the map configuration file.
 */
public class MapLoader { // Ayudado a hacer con Gemini 4 Hydrogen

    /**
     * Result of loading a map: board elements, goals by side and the main ball.
     */
    public static final class MapData {
        private final TreeMap<Integer, Element> elements = new TreeMap<>();
        private final TreeMap<Integer, Goal> goals = new TreeMap<>();
        private Ball ball;

        public TreeMap<Integer, Element> getElements() { return elements; }
        public TreeMap<Integer, Goal> getGoals() { return goals; }
        public Ball getBall() { return ball; }
    }

    private static final String DEFAULT_PATH = "/positionElements/configuration.txt";
    private static final int FIRST_FREE_ID = 3; // Reserva los IDs 0, 1 y 2 para balón y jugadores

    private final String path;

    public MapLoader() {
        this(DEFAULT_PATH);
    }

    public MapLoader(String path) {
        this.path = path;
    }

    /**
     * Reads the configuration file and places every element. Errors are logged and the partial map is returned.
     */
    public MapData load(Player player1, Player player2) {
        MapData map = new MapData();
        final int tileSize = 50; // Tamaño de cada celda de la grilla de configuración del mapa
        int nextId = FIRST_FREE_ID;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("No se encontró el archivo de mapa: " + path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                for (int col = 0; col < tokens.length; col++) {
                    nextId = placeToken(map, tokens[col], col * tileSize, row * tileSize,
                            tileSize, nextId, player1, player2);
                }
                row++;
            }
        } catch (Exception e) {
            Log.record(e); // Registra el error de carga de mapa en data/logs/
        }
        return map;
    }

    private int placeToken(MapData map, String token, int posX, int posY, int tileSize, int nextId,
                           Player player1, Player player2) {
        switch (token) {
            case "1" -> placePlayer(map, 1, player1, posX, posY); // Ubica al Jugador 1 en el tablero
            case "2" -> placePlayer(map, 2, player2, posX, posY); // Ubica al Jugador 2 en el tablero
            case "0" -> placeBall(map, posX + tileSize / 2, posY); // Centra el balón en la celda inicial
            case "4" -> placePitch(map, posX, posY, nextId++); // Agrega un tramo del poste o travesaño izquierdo
            case "5" -> placePitch(map, posX + 5.0f, posY, nextId++); // Agrega un tramo del travesaño derecho ajustado 5 px
            case "6" -> placeGoal(map, 1, posX, 358.0f, nextId++); // Crea el arco izquierdo apoyado hasta el suelo (358 + 157 = 515)
            case "7" -> placeGoal(map, 2, posX + 5.0f, 358.0f, nextId++); // Crea el arco derecho apoyado hasta el suelo
            default -> { }
        }
        return nextId;
    }

    private void placePlayer(MapData map, int id, Player player, float x, float y) {
        player.setPositions(x, y);
        player.setGroundY(y);
        map.elements.put(id, player);
    }

    private void placeBall(MapData map, float x, float y) {
        Ball ball = new Ball();
        ball.setPositions(x, y);
        map.ball = ball;
        map.elements.put(0, ball);
    }

    private void placePitch(MapData map, float x, float y, int id) {
        Pitch pitch = new Pitch();
        pitch.setPositions(x, y);
        map.elements.put(id, pitch);
    }

    private void placeGoal(MapData map, int goalKey, float x, float y, int id) {
        Goal goal = new Goal();
        map.goals.putIfAbsent(goalKey, goal);
        goal.setPositions(x, y);
        map.elements.put(id, goal);
    }
}
