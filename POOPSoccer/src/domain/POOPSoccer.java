package domain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class POOPSoccer {
    private static POOPSoccer game;
    private boolean isPaused;
    private Player player1, player2;

    private int currentLevel;
    private CollisionChecker cChecker;
    private ScoreBoard infoMatch;
    private Map elements;

    private POOPSoccer() {
        infoMatch = new ScoreBoard();
        elements = new TreeMap<Integer, Element>();
    }

    public static POOPSoccer getGame() {
        if (game == null) {
            game = new POOPSoccer();
        }
        return game;
    }

    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        this.isPaused = false;
        this.infoMatch = new ScoreBoard(goalLimit);

        // Mapear los strings de equipo a enums del dominio
        Team t1 = Team.fromDisplayName(teamP1);
        Team t2 = Team.fromDisplayName(teamP2);

        // Asignar el tipo de jugador según la modalidad de juego
        PlayerType typeP1 = (gameMode == GameMode.MVM) ? new Machine() : new Normal();
        PlayerType typeP2 = (gameMode == GameMode.PVP) ? new Normal() : new Machine();

        String nameP2Final = (nameP2 != null && !nameP2.isBlank()) ? nameP2 : "CPU";
        if (gameMode == GameMode.MVM) {
            player1 = new Player(typeP2, nameP1, t1, NumberPlayer.PLAYER1);
            player2 = new Player(typeP1, nameP2Final, t2, NumberPlayer.PLAYER2);
        } else if (gameMode == GameMode.PVP) {
            player1 = new Player(typeP2, nameP1, t1, NumberPlayer.PLAYER1);
            player2 = new Player(typeP2, nameP2Final, t2, NumberPlayer.PLAYER2);
        }
        // Crear instancias del dominio


        // Registrar los elementos en el mapa de renderizado/lógica
        elements.clear();
        setElements();
    }

    public void update() {
        if (player1 != null) player1.update();
        if (player2 != null) player2.update();
    }

    // Movimiento y acciones Jugador 1
    public void movePlayer1(char direction) {
        if (player1 != null) {
            try {
                player1.move(direction);
            } catch (InterruptedException e) {
                // Se restaura el estado de interrupción del hilo
                Thread.currentThread().interrupt();
            }
        }
    }

    // Movimiento y acciones Jugador 2
    public void movePlayer2(char direction) {
        if (player2 != null) {
            try {
                player2.move(direction);
            } catch (InterruptedException e) {
                // Se restaura el estado de interrupción del hilo
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isGameOver() {
        return infoMatch.isGameOver();
    }

    public Side checkWinner() {
        return infoMatch.checkWinner();
    }

    public int getGoalsP1() {
        return infoMatch.getGoalsP1();
    }

    public int getGoalsP2() {
        return infoMatch.getGoalsP2();
    }

    public float getRemainingTime() {
        return infoMatch.getRemainingTime();
    }

    public boolean isGameFinished() {
        return false;
    }

    public void resumeGame() {
        isPaused = true;
    }

    public Map<Integer, Element> getElementsToDraw() {
        return elements;
    }

    public void setElements() {
        final int TILE_WIDTH = 40;
        final int TILE_HEIGHT = 75;
        final String path = "/positionElements/configuration.txt";
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("No se encontró el archivo de nivel: " + path);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;
            // 1 y 2 reservados para jugadores
            int elementIdCounter = elements.size();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");

                for (int col = 0; col < tokens.length; col++) {
                    String token = tokens[col];
                    int posX = col * TILE_WIDTH;
                    int posY = row * TILE_HEIGHT;

                    switch (token) {
                        case "1":
                            // game.getPlayer1().getPosition().setPositions(posX, posY);
                            elements.put(Integer.valueOf(token), game.getPlayer1());
                            break;
                        case "2":
                            // game.getPlayer2().getPosition().setPositions(posX, posY);
                            elements.put(Integer.valueOf(token), game.getPlayer2());

                            break;
                        case "B":
                            Ball ball = new Ball();
                            // ball.getPosition().setPositions(posX, posY);
                            elements.put(0, ball);
                            break;
                        case ".":
                            break;
                        default:
                            // Espacio vacío, no hacemos nada
                            break;
                    }
                }
                row++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setPositionElements();
    }

    public void setPositionElements() {
        final String path = "/positionElements/configuration.txt";
        final int TILE_SIZE = 50;
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int row = 0;
            String line;

            while ((line = br.readLine()) != null) {
                // Separar por cualquier cantidad de espacios
                String[] tokens = line.trim().split("\\s+");

                for (int col = 0; col < tokens.length; col++) {
                    String token = tokens[col];
                    int posX = col * TILE_SIZE;
                    int posY = row * TILE_SIZE;
                    if (!token.equals(".")) setPositionToElement((Element) elements.get(Integer.valueOf(token)), posX, posY);
                }
                row++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPositionToElement(Element elem, int x, int y) {
        if (elem != null && elem.getPosition() != null) {
            elem.getPosition().setPositions(x, y);
        }
    }

    public Player getPlayer1() { return player1; }

    public Player getPlayer2() { return player2; }

}
