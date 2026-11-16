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
    private Ball ball;
    private int goalLimit;

    private int currentLevel;
    private CollisionChecker cChecker;
    private Map<Integer, Element> elements;
    private Map<Integer, Goal> goalPlayers;

    private POOPSoccer() {
        elements = new TreeMap<>();
        cChecker = CollisionChecker.getInstance();
        goalPlayers = new TreeMap<>();
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
        this.goalLimit = goalLimit;
        this.isPaused = false;

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
        if (isPaused) return;

        if (player1 != null) {
            player1.update();
            cChecker.resolvePlayerEnvironmentCollisions(player1, elements.values());
        }
        if (player2 != null) {
            player2.update();
            cChecker.resolvePlayerEnvironmentCollisions(player2, elements.values());
        }
        if (ball != null) {
            ball.update();
            cChecker.resolvePlayerBallInteraction(player1, ball);
            cChecker.resolvePlayerBallInteraction(player2, ball);
            cChecker.resolveBallEnvironmentCollisions(ball, elements.values());
        }
        checkGoal();
    }

    // Movimiento y acciones Jugador 1
    public void movePlayer1(char direction) {
        if (player1 != null && !isPaused) {
            try {
                player1.move(direction);
                cChecker.resolvePlayerEnvironmentCollisions(player1, elements.values());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Side checkWinner() {
        if (cChecker.isGoal(ball, goalPlayers.get(1)) ) {
            return Side.RIGTH;
        } else if (cChecker.isGoal(ball, goalPlayers.get(2))) {
            return Side.LEFT;
        }
        return null;
    }

    public void checkGoal() {
        if (cChecker.isGoal(ball, goalPlayers.get(1))) {
            player2.doGoal(); // Gol en arco izquierdo -> suma el de la DERECHA
            resetRound();
        } else if (cChecker.isGoal(ball, goalPlayers.get(2))) {
            player1.doGoal(); // Gol en arco derecho -> suma el de la IZQUIERDA
            resetRound();
        }
    }

    public Player getWinner() {
        if (player1.getGoals() > player2.getGoals()) return player1;
        if (player2.getGoals() > player1.getGoals()) return player2;
        return null; // Empate
    }

    void resetRound() {
        // 1. Frenar y recolocar balón en el centro
        if (ball != null) {
            ball.setVelocity(0.0f, 0.0f);
            ball.setPositions(400.0f, 250.0f); // o las coordenadas iniciales de tu mapa
        }
        // 2. Devolver a los jugadores a sus posiciones de saque
        if (player1 != null) {
            player1.setPositions(200.0f, 515.0f - player1.getHeight());
        }
        if (player2 != null) {
            player2.setPositions(600.0f, 515.0f - player2.getHeight());
        }
    }

    void resetMatch () {
        game = null;
        game = new POOPSoccer();
    }

    public boolean resumeGame() {
        return isPaused;
    }

    public int getGoalsP1() {
        return player1.getGoals();
    }

    public int getGoalsP2() {
        return player2.getGoals();
    }

    // Movimiento y acciones Jugador 2
    public void movePlayer2(char direction) {
        if (player2 != null && !isPaused) {
            try {
                player2.move(direction);
                cChecker.resolvePlayerEnvironmentCollisions(player2, elements.values());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isGameFinished() {
        return player1.getGoals() == goalLimit || player2.getGoals() == goalLimit;
    }

    public float getRemainingTime() {
        return 0.0f;
    }


    public Map<Integer, Element> getElementsToDraw() {
        return elements;
    }

    public void setElements() {
        final int TILE_SIZE = 50;
        final String path = "/positionElements/configuration.txt";
        int nextId = 3;
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("No se encontró el archivo de nivel: " + path);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;
            // 1 y 2 reservados para jugadores
            int posX = 0, posY = 0, indexElements = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");

                for (int col = 0; col < tokens.length; col++) {
                    String token = tokens[col];
                    posX = col * TILE_SIZE;
                    posY = row * TILE_SIZE;
                    indexElements = 3 + elements.size();
                    switch (token) {
                        case "1":
                            player1.setPositions(posX, posY);
                            player1.setGroundY(posY);
                            elements.put(1, player1);
                            break;
                        case "2":
                            player2.setPositions(posX, posY);
                            player2.setGroundY(posY);
                            elements.put(2, player2);
                            break;
                        case "0":
                            this.ball = new Ball();
                            this.ball.setPositions(posX+TILE_SIZE/2, posY);
                            elements.put(0, this.ball);
                            break;
                        case ".":
                            break;
                        case "4":
                            Pitch pLeft = new Pitch(Side.LEFT);
                            pLeft.setPositions(posX, posY);
                            elements.put(nextId++, pLeft);
                            break;
                        case "5":
                            Pitch pRight = new Pitch(Side.RIGTH);
                            pRight.setPositions(posX+5.0f, posY);
                            elements.put(nextId++, pRight);
                            break;
                        case "6":
                            Goal goalL = new Goal(Side.LEFT, player1);
                            if (goalPlayers.get(1) == null) goalPlayers.put(1, goalL);
                            goalL.setPositions(posX, posY);
                            elements.put(nextId++, goalL);
                            break;
                        case "7":
                            Goal goalR = new Goal(Side.RIGTH, player2);
                            if (goalPlayers.get(2) == null) goalPlayers.put(2, goalR);
                            goalR.setPositions(posX+5.0f, posY);
                            elements.put(nextId++, goalR);
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
    }

    public Player getPlayer1() { return player1; }

    public Player getPlayer2() { return player2; }

    public Ball getBall() { return ball; };

}
