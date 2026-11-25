package domain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the board elements (players, ball, pitch and goals) from the map configuration file.
 */
public class MapLoader { // Responsabilidad de carga del mapa extraída de la fachada POOPSoccer para perfeccionar el principio S con ayuda de Gemini 4 Hydrogen
    /*Perfeccionado con Antigravity.*/

    /**
     * Result of loading a map: extra field elements, left/right goals, and the main ball.
     */
    public static final class MapData {
        private final List<Element> fieldElements = new ArrayList<>();
        private Goal leftGoal;
        private Goal rightGoal;
        private Ball ball;

        public List<Element> getFieldElements() { return Collections.unmodifiableList(fieldElements); }
        public void addFieldElement(Element element) {
            if (element != null) {
                fieldElements.add(element);
            }
        }
        public Goal getLeftGoal() { return leftGoal; }
        public Goal getRightGoal() { return rightGoal; }
        public List<Goal> getGoals() {
            List<Goal> goals = new ArrayList<>(2);
            if (leftGoal != null) goals.add(leftGoal);
            if (rightGoal != null) goals.add(rightGoal);
            return Collections.unmodifiableList(goals);
        }
        public Ball getBall() { return ball; }
    }

    private static final String DEFAULT_PATH = "/positionElements/configuration.txt";
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
     * Reads the configuration file and places every element.
     * Throws POOPSoccerException if the map resource is missing, unreadable, or players are null.
     */
    public MapData load(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER); // Impide construir un tablero sin ambos jugadores
        }
        if (path == null || path.isBlank()) {
            throw new POOPSoccerException(POOPSoccerException.MAP_LOAD_ERROR + path); // Rechaza rutas vacías antes de abrir recursos
        }
        MapData map = new MapData();
        int nextId = 0;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new POOPSoccerException(POOPSoccerException.MAP_LOAD_ERROR + path); // Falla de inmediato si falta el archivo del mapa
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
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
        } catch (POOPSoccerException e) {
            throw e; // Repropaga errores de dominio ya validados sin envolverlos dos veces
        } catch (Exception e) {
            ErrorLogger.report(e); // Registra el fallo inesperado de lectura y lo convierte en error de dominio con su causa raíz
            throw new POOPSoccerException(POOPSoccerException.MAP_LOAD_ERROR + path, e);
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
                -> { placePlayer(p1, x, y); return id; });
        addPlacer("2", (map, x, y, tile, id, p1, p2)
                -> { placePlayer(p2, x, y); return id; });
        addPlacer("0", (map, x, y, tile, id, p1, p2)
                -> { placeBall(map, x + tile / 2, y); return id; });
        addPlacer("4", (map, x, y, tile, id, p1, p2)
                -> { placePitch(map, x, y); return id + 1; });
        addPlacer("5", (map, x, y, tile, id, p1, p2)
                -> { placePitch(map, x + RIGHT_SIDE_OFFSET_X, y); return id + 1; });
        addPlacer("6", (map, x, y, tile, id, p1, p2)
                -> { placeGoal(map, true, x, GOAL_TOP_Y); return id; });
        addPlacer("7", (map, x, y, tile, id, p1, p2)
                -> { placeGoal(map, false, x + RIGHT_SIDE_OFFSET_X, GOAL_TOP_Y); return id; });
    }

    private int placeToken(MapData map, String token, int posX, int posY, int tileSize, int nextId,
                           Player player1, Player player2) {
        TokenPlacer placer = placers.get(token);
        if (placer == null) return nextId; // Ignora tokens desconocidos o vacíos
        return placer.place(map, posX, posY, tileSize, nextId, player1, player2);
    }

    private void placePlayer(Player player, float x, float y) {
        player.setPositions(x, y);
        player.setGroundY(y);
    }

    private void placeBall(MapData map, float x, float y) {
        Ball ball = new Ball();
        ball.setPositions(x, y);
        map.ball = ball;
    }

    private void placePitch(MapData map, float x, float y) {
        Pitch pitch = new Pitch();
        pitch.setPositions(x, y);
        map.addFieldElement(pitch);
    }

    private void placeGoal(MapData map, boolean isLeft, float x, float y) {
        Goal goal = new Goal();
        goal.setPositions(x, y);
        if (isLeft && map.leftGoal == null) {
            map.leftGoal = goal;
        } else if (!isLeft && map.rightGoal == null) {
            map.rightGoal = goal;
        }
    }
}