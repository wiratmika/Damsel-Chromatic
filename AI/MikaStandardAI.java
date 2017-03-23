import java.util.Random;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

public class MikaStandardAI {
    public static void main(String[] args) {
        try {
            Deque<String> arguments = new ArrayDeque<String>();
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            while (!input.equals("END")) {
                arguments.addLast(input);
                input = scanner.nextLine();
            }

            State state = new MikaStandardAI().new State(arguments);
            state.makeAction();
        } catch (Exception e) {
            printRandomAction();
        }
    }

    private static void printRandomAction() {
        // Some fuck-up happens, just print random movement
        Random random = new Random();

        switch (random.nextInt(6)) {
            case 0:
                System.out.println(">> MOVE RIGHT");
                break;
            case 1:
                System.out.println(">> MOVE LEFT");
                break;
            case 2:
                System.out.println(">> MOVE UP");
                break;
            case 3:
                System.out.println(">> MOVE DOWN");
                break;
            case 4:
                System.out.println(">> DROP BOMB");
                break;
            default:
                System.out.println(">> STAY");
                break;
        }
    }

    private static String checkForBomb() {
        // decide best directions.
        return null;
    }

    private static String searchForPowerup() {
        return null;
    }

    private static String huntForEnemy() {
        // todo: if enemy is within bomb range, drop bomb then lari
        // kill yg poinnya gede duls
        return null;
    }

    class State {
        private int turn;
        private int playerCount;
        private List<Player> playerList;
        private Board board;

        public State(Deque<String> arguments) {
            playerList = new ArrayList<Player>();

            // Turn
            String input = arguments.poll();
            turn = Integer.parseInt(input.split(" ")[1]);

            // Player count
            input = arguments.poll();
            playerCount = Integer.parseInt(input.split(" ")[1]);

            for (int i = 0; i < this.playerCount; i++) {
                String[] playerInfo = arguments.poll().split(" ");

                String name = playerInfo[1];
                String bombCountAndMaxString = playerInfo[2].substring(5);
                String[] bombSplitString = bombCountAndMaxString.split("\\/");
                int bombCount = Integer.parseInt(bombSplitString[0]);
                int maxBomb = Integer.parseInt(bombSplitString[1]);
                int bombRange = Integer.parseInt(playerInfo[3].substring(6));
                String status = playerInfo[4];
                int score = Integer.parseInt(playerInfo[5]);

                Player player = new Player(
                        name,
                        bombCount,
                        maxBomb,
                        bombRange,
                        status,
                        score
                );

                this.playerList.add(player);
            }

            input = arguments.poll();
            String[] boardSize = input.split(" ");

            board = new Board(arguments, Integer.parseInt(boardSize[1]), Integer.parseInt(boardSize[2]));

            String[] playerLocation = board.getPlayerLocation(playerCount);

            for (int i = 0; i < playerLocation.length; i++) {
                int x = Integer.parseInt(playerLocation[i].split(" ")[0]);
                int y = Integer.parseInt(playerLocation[i].split(" ")[1]);

                Player player = this.playerList.get(i);
                player.setX(x);
                player.setY(y);
            }
        }

        public int getPlayerIndex(String playerName) {
            for (int i = 0; i < this.playerList.size(); i++) {
                if (playerList.get(i).getName().equals(playerName)) {
                    return i;
                }
            }

            return -1;
        }

        public boolean[][] getPassableNodes() {
            return this.board.getPassableNodes();
        }

        public String 

        public List<BoardObject> getAllObjects(int x, int y) {
            return board.getAllObjects(x, y);
        }

        public List<BoardObject> getAllBombs() {
            return board.getAllBombs();
        }

        public List<BoardObject> getAllFlares() {
            return board.getAllFlares();
        }

        public List<BoardObject> getAllDestructibleWalls() {
            return board.getAllDestructibleWalls();
        }

        public List<BoardObject> getAllPowerups() {
            return board.getAllPowerups();
        }

        public List<BoardObject> getAllObjectsExceptIndestructibleWalls() {
            return board.getAllObjectsExceptIndestructibleWalls();
        }

        public void makeAction() {
            String nextMove= null;

            nextMove = checkForBomb();

            if (nextMove != null) {
                System.out.println(nextMove);
                return;
            }

            nextMove = searchForPowerup();

            if (nextMove != null) {
                System.out.println(nextMove);
                return;
            }

            nextMove = huntForEnemy();
            System.out.println(nextMove);
        }

        public int getTurn() {
            return turn;
        }

        public void setTurn(int turn) {
            this.turn = turn;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public void setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
        }

        public List<Player> getPlayerList() {
            return playerList;
        }

        public void setPlayerList(List<Player> playerList) {
            this.playerList = playerList;
        }

        public Board getBoard() {
            return board;
        }

        public void setBoard(Board board) {
            this.board = board;
        }
    }

    private class Board {
        private int row;
        private int column;
        private BoardNode[][] nodes;

        Board(Deque<String> arguments, int row, int column) {
            this.nodes = new BoardNode[row][column];
            this.row = row;
            this.column = column;

            for (int i = 0; i < row; i++) {
                String input = arguments.poll();
                input = input.substring(1, input.length() - 1);
                String[] nodeStrings = input.split("]\\[");

                for (int j = 0; j < column; j++) {
                    nodes[i][j] = new BoardNode(nodeStrings[j], i, j);
                }
            }
        }

        String[] getPlayerLocation(int playerCount) {
            String[] playerLocation = new String[playerCount];

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    List<BoardObject> objects = getAllObjects(i, j);

                    for (BoardObject object : objects) {
                        if (object.getType().equals("PLAYER")) {
                            PlayerObject playerObject = (PlayerObject) object;
                            playerLocation[playerObject.getIndex()] = playerObject.getX() + " " + playerObject.getY();
                        }
                    }
                }
            }

            return playerLocation;
        }

        boolean[][] getPassableNodes() {
            boolean[][] passableNodes = new boolean[row][column];

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    List<BoardObject> objects = getAllObjects(i, j);

                    boolean isPassable = true;

                    for (BoardObject object : objects) {
                        if (!object.isPassable()) {
                            isPassable = false;
                        }
                    }

                    passableNodes[i][j] = isPassable;
                }
            }

            return passableNodes;
        }

        List<BoardObject> getAllObjects(int x, int y) {
            return nodes[x][y].getObjectList();
        }

        List<BoardObject> getObjectsOfType(String type) {
            List<BoardObject> searchedObjects = new ArrayList<BoardObject>();

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    List<BoardObject> objects = getAllObjects(i, j);

                    for (BoardObject object : objects) {
                        if (object.getType().equals(type))
                            searchedObjects.add(object);
                    }
                }
            }

            return searchedObjects;
        }

        List<BoardObject> getAllBombs() {
            return getObjectsOfType("BOMB");
        }

        List<BoardObject> getAllFlares() {
            return getObjectsOfType("FLARE");
        }

        List<BoardObject> getAllDestructibleWalls() {
            List<BoardObject> destructibleWalls = getObjectsOfType("DESTRUCTIBLE_WALL");
            destructibleWalls.addAll(getObjectsOfType("DESTRUCTIBLE_WALL_WITH_POWERUP"));
            return destructibleWalls;
        }

        List<BoardObject> getAllPowerups() {
            return getObjectsOfType("POWERUP");
        }

        List<BoardObject> getAllObjectsExceptIndestructibleWalls() {
            return null;
        }
    }

    class BoardNode {
        List<BoardObject> objectList;

        public BoardNode(String nodeString, int x, int y) {
            this.objectList = new ArrayList<BoardObject>();
            nodeString = nodeString.trim();
            String[] objectStrings = nodeString.split(";");

            for (String objectString : objectStrings) {
                if (objectString.isEmpty()) {
                    // no-op
                } else if (MikaStandardAI.Helper.isNumeric(objectString)) {
                    objectList.add(new PlayerObject(x, y, objectString, Integer.parseInt(objectString)));
                } else if (objectString.charAt(0) == 'B') {
                    int timer = Integer.parseInt(objectString.substring(objectString.length() - 1));
                    int power = Integer.parseInt(objectString.substring(1, objectString.length() - 1));
                    objectList.add(new Bomb(x, y, objectString, timer, power));
                } else if (objectString.charAt(0) == 'F') {
                    int timer = Integer.parseInt(objectString.substring(1));
                    objectList.add(new Flare(x, y, objectString, timer));
                } else if (objectString.equals("###")) {
                    objectList.add(new BoardObject("INDESTRUCTIBLE_WALL", x, y, false, objectString));
                } else if (objectString.equals("XXX")) {
                    objectList.add(new BoardObject("DESTRUCTIBLE_WALL", x, y, false, objectString));
                } else if (objectString.equals("XBX") || objectString.equals("XPX")) {
                    objectList.add(new BoardObject("DESTRUCTIBLE_WALL_WITH_POWERUP", x, y, false, objectString));
                } else if (objectString.equals("+P") || objectString.equals("+B")) {
                    objectList.add(new BoardObject("POWERUP", x, y, true, objectString));
                }
            }
        }

        public List<BoardObject> getObjectList() {
            return objectList;
        }

        public void setObjectList(List<BoardObject> objectList) {
            this.objectList = objectList;
        }
    }

    class BoardObject {
        private String type;
        private int x;
        private int y;
        private boolean isPassable;
        private String name;

        public BoardObject(String type, int x, int y, boolean isPassable, String name) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.isPassable = isPassable;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isPassable() {
            return isPassable;
        }

        public void setPassable(boolean passable) {
            isPassable = passable;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class Player {
        private String name;
        private int bombCount;
        private int maxBomb;
        private int bombRange;
        private String status;
        private int score;

        private int x;
        private int y;

        public Player(String name, int bombCount, int maxBomb, int bombRange, String status, int score) {
            this.name = name;
            this.bombCount = bombCount;
            this.maxBomb = maxBomb;
            this.bombRange = bombRange;
            this.status = status;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBombCount() {
            return bombCount;
        }

        public void setBombCount(int bombCount) {
            this.bombCount = bombCount;
        }

        public int getMaxBomb() {
            return maxBomb;
        }

        public void setMaxBomb(int maxBomb) {
            this.maxBomb = maxBomb;
        }

        public int getBombRange() {
            return bombRange;
        }

        public void setBombRange(int bombRange) {
            this.bombRange = bombRange;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "Player{" +
                    "name='" + name + '\'' +
                    ", bombCount=" + bombCount +
                    ", maxBomb=" + maxBomb +
                    ", bombRange=" + bombRange +
                    ", status='" + status + '\'' +
                    ", score=" + score +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    class PlayerObject extends BoardObject {
        private int index;

        public PlayerObject(int x, int y, String name, int index) {
            super("PLAYER", x, y, true, name);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    private class Bomb extends BoardObject {
        private int timer;
        private int power;

        public Bomb(int x, int y, String name, int timer, int power) {
            super("BOMB", x, y, false, name);
            this.timer = timer;
            this.power = power;
        }

        public int getTimer() {
            return timer;
        }

        public void setTimer(int timer) {
            this.timer = timer;
        }

        public int getPower() {
            return power;
        }

        public void setPower(int power) {
            this.power = power;
        }
    }

    private class Flare extends BoardObject {
        private int timer;

        public Flare(int x, int y, String name, int timer) {
            super("FLARE", x, y, false, name);
            this.timer = timer;
        }

        public int getTimer() {
            return timer;
        }

        public void setTimer(int timer) {
            this.timer = timer;
        }
    }

    private static class Helper {
        static boolean isNumeric(String str) {
            return str.matches("-?\\d+(\\.\\d+)?");
        }
    }
}

//static class Helper {
//    public boolean isValidAndSafe {
        // make sure setiap gerakan gaada flare
        // kalo gerak atas bawah kanan kiri, bukan temboque
        // msh dalem arena
//    }
//}
