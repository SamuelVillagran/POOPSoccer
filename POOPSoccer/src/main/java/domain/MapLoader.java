package domain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds the board elements (players, ball, pitch and goals) from the map configuration file.
 */
public class MapLoader { // Responsabilidad de carga del mapa extraída de la fachada POOPSoccer para perfeccionar el principio S con ayuda de Gemini 4 Hydrogen
    /*Perfeccionado con Antigravity.*/

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
        public void setBall(Ball ball) { this.ball = ball; }
    }

    private static final String DEFAULT_PATH = "/positionElements/configuration.txt";
    private static final int FIRST_FREE_ID = 3; // Reserva los IDs 0, 1 y 2 para balón y jugadores
    private static final int TILE_SIZE = 50; // Tamaño de cada celda de la grilla de configuración del mapa
    private static final float RIGHT_SIDE_OFFSET_X = 5.0f; // Ajuste horizontal en píxeles para elementos del lado derecho
    private static final float GOAL_TOP_Y = 358.0f; // Coordenada Y superior del arco apoyado hasta el suelo (358 + 157 = 515)

    private final String path;

    public MapLoader() {
        this(DEFAULT_PATH);
    }

    public MapLoader(String path) {
        this.path = path;
        registerDefaultPlacers();
    }

    /**
     * Reads the configuration file and places every element. Errors are logged and the partial map is returned.
     */
    public MapData load(Player player1, Player player2) {
        MapData map = new MapData();
        int nextId = FIRST_FREE_ID;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("No se encontró el archivo de mapa: " + path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                for (int col = 0; col < tokens.length; col++) {
                    nextId = placeToken(map, tokens[col], col * TILE_SIZE, row * TILE_SIZE,
                            TILE_SIZE, nextId, player1, player2);
                }
                row++;
            }
        } catch (Exception e) {
            ErrorLogger.report(e); // Registra el error de carga de mapa mediante el puerto de logging
        }
        return map;
    }

    /**
     * Places one kind of element for a map token and returns the next free element id.
     */
    @FunctionalInterface
    public interface TokenPlacer {
        int place(MapData map, float posX, float posY, int tileSize, int nextId, Player player1, Player player2);
    }

    // Registro de tokens: soportar un elemento nuevo solo requiere registrar una entrada, sin tocar la lógica de carga
    private final Map<String, TokenPlacer> placers = new HashMap<>();

    /**
     * Registers or replaces a map token placer without modifying MapLoader.
     */
    public MapLoader registerPlacer(String token, TokenPlacer placer) {
        addPlacer(token, placer);
        return this;
    }

    private void addPlacer(String token, TokenPlacer placer) {
        if (token == null || token.isBlank()) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "token");
        }
        if (placer == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "placer");
        }
        placers.put(token, placer);
    }

    private void registerDefaultPlacers() {
        addPlacer("1", (map, x, y, tile, id, p1, p2)
                -> { placePlayer(map, 1, p1, x, y); return id; }); // Ubica al Jugador 1
        addPlacer("2", (map, x, y, tile, id, p1, p2)
                -> { placePlayer(map, 2, p2, x, y); return id; }); // Ubica al Jugador 2
        addPlacer("0", (map, x, y, tile, id, p1, p2)
                -> { placeBall(map, x + tile / 2, y); return id; }); // Centra el balón en la celda inicial
        addPlacer("4", (map, x, y, tile, id, p1, p2)
                -> { placePitch(map, x, y, id); return id + 1; }); // Tramo del poste o travesaño izquierdo
        addPlacer("5", (map, x, y, tile, id, p1, p2)
                -> { placePitch(map, x + RIGHT_SIDE_OFFSET_X, y, id); return id + 1; }); // Travesaño derecho
        addPlacer("6", (map, x, y, tile, id, p1, p2)
                -> { placeGoal(map, 1, x, GOAL_TOP_Y, id); return id + 1; }); // Arco izquierdo
        addPlacer("7", (map, x, y, tile, id, p1, p2)
                -> { placeGoal(map, 2, x + RIGHT_SIDE_OFFSET_X, GOAL_TOP_Y, id); return id + 1; }); // Arco derecho
    }

    private int placeToken(MapData map, String token, int posX, int posY, int tileSize, int nextId,
                           Player player1, Player player2) {
        TokenPlacer placer = placers.get(token);
        if (placer == null) return nextId; // Ignora tokens desconocidos o vacíos
        return placer.place(map, posX, posY, tileSize, nextId, player1, player2);
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