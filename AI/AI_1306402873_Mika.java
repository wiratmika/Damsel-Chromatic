import java.util.Random;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class AI_1306402873_Mika {
    static int stayCount;
    static boolean bombDropped;
    static String lastMove;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        stayCount = 0;
        bombDropped = false;

        while (true) {
            Deque<String> arguments = new ArrayDeque<String>();

            try {
                String input = scanner.nextLine();

                while (!input.equals("END")) {
                    arguments.addLast(input);
                    input = scanner.nextLine();
                }

                State state = new AI_1306402873_Mika().new State(arguments);
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                printRandomAction();
            }
        }
    }

    private static void printRandomAction() {
        // Some fuck-up happens, just print random movement
        Random random = new Random();

        switch (random.nextInt(6)) {
            case 0:
                lastMove = ">> MOVE RIGHT";
                System.out.println(">> MOVE RIGHT");
                break;
            case 1:
                lastMove = ">> MOVE LEFT";
                System.out.println(">> MOVE LEFT");
                break;
            case 2:
                lastMove = ">> MOVE UP";
                System.out.println(">> MOVE UP");
                break;
            case 3:
                lastMove = ">> MOVE DOWN";
                System.out.println(">> MOVE DOWN");
                break;
            case 4:
                lastMove = ">> DROP BOMB";
                System.out.println(">> DROP BOMB");
                break;
            default:
                System.out.println(">> STAY");
                break;
        }
    }

    private class State {
        private int turn;
        private int playerCount;
        private List<Player> playerList;
        private Board board;
        private int row;
        private int column;
        private boolean[][] traversalFlag;

        public State(Deque<String> arguments) {
            playerList = new ArrayList<>();
            String input = arguments.poll();
            turn = Integer.parseInt(input.split(" ")[1]);
            input = arguments.poll();
            playerCount = Integer.parseInt(input.split(" ")[1]);

            for (int i = 0; i < this.playerCount; i++) {
                String[] playerInfo = arguments.poll().split(" ");
                String name = playerInfo[1];
                String bombString = playerInfo[2].substring(5);
                String[] bombInfo = bombString.split("\\/");
                int bombCount = Integer.parseInt(bombInfo[0]);
                int maxBomb = Integer.parseInt(bombInfo[1]);
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

            row = Integer.parseInt(boardSize[1]);
            column = Integer.parseInt(boardSize[2]);
            board = new Board(arguments, row, column);
            traversalFlag = new boolean[row][column];

            String[] playerLocation = board.getPlayerLocation(playerCount);

            for (int i = 0; i < playerLocation.length; i++) {
                int x = Integer.parseInt(playerLocation[i].split(" ")[0]);
                int y = Integer.parseInt(playerLocation[i].split(" ")[1]);

                Player player = this.playerList.get(i);
                player.setX(x);
                player.setY(y);
            }

            scanDangerZones();
            makeAction();
        }

        void scanDangerZones() {
            board.clearDangerZones();
            List<BoardObject> bombs = getAllBombs();

            for (BoardObject bombObject : bombs) {
                Bomb bomb = (Bomb) bombObject;
                int x = bomb.getX();
                int y = bomb.getY();

                BoardObject here = new BoardObject("DANGER_ZONE", x, y, true, "DANGER_ZONE_ANYWHERE_IS_FINE");
                board.putObject(x, y, here);

                for (int i = 1; i <= bomb.getPower(); i++) {
                    if (!isOutOfBounds(x - i, y)) {
                        BoardObject up = new BoardObject("DANGER_ZONE", x - i, y, true, "DANGER_ZONE_UP");
                        board.putObject(x - i, y, up);
                    }

                    if (!isOutOfBounds(x + i, y)) {
                        BoardObject down = new BoardObject("DANGER_ZONE", x + i, y, true, "DANGER_ZONE_DOWN");
                        board.putObject(x + i, y, down);
                    }

                    if (!isOutOfBounds(x, y - i)) {
                        BoardObject left = new BoardObject("DANGER_ZONE", x, y - i, true, "DANGER_ZONE_LEFT");
                        board.putObject(x, y - i, left);
                    }

                    if (!isOutOfBounds(x, y + i)) {
                        BoardObject right = new BoardObject("DANGER_ZONE", x, y + i, true, "DANGER_ZONE_RIGHT");
                        board.putObject(x, y + i, right);
                    }
                }
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

        boolean[][] getPassableNodes() {
            return this.board.getPassableNodes();
        }

        boolean isOutOfBounds(int x, int y) {
            boolean xOutOfBound = x < 0 || x + 1 > row;
            boolean yOutOfBound = y < 0 || y + 1 > column;
            return xOutOfBound || yOutOfBound;
        }

        String getRandomDirection() {
            Random random = new Random();

            switch (random.nextInt(4)) {
                case 0:
                    return ">> MOVE RIGHT";
                case 1:
                    return ">> MOVE LEFT";
                case 2:
                    return ">> MOVE UP";
                default:
                    return ">> MOVE DOWN";
            }
        }

        boolean isValidAndSafe(int x, int y) {
            if (isOutOfBounds(x, y))
                return false;

            return getPassableNodes()[x][y];
        }

        boolean isNearbyDestructibleWall() {
            Player player = getMyPlayer();
            List<BoardObject> destructibleWalls = new ArrayList<>();
            int x = player.getX();
            int y = player.getY();

            if (!isOutOfBounds(x - 1, y)) {
                destructibleWalls.addAll(board.getObjectsOfType(x - 1, y, "DESTRUCTIBLE_WALL"));
                destructibleWalls.addAll(board.getObjectsOfType(x - 1, y, "DESTRUCTIBLE_WALL_WITH_POWERUP"));
            }

            if (!isOutOfBounds(x + 1, y)) {
                destructibleWalls.addAll(board.getObjectsOfType(x + 1, y, "DESTRUCTIBLE_WALL"));
                destructibleWalls.addAll(board.getObjectsOfType(x + 1, y, "DESTRUCTIBLE_WALL_WITH_POWERUP"));
            }

            if (!isOutOfBounds(x, y - 1)) {
                destructibleWalls.addAll(board.getObjectsOfType(x, y - 1, "DESTRUCTIBLE_WALL"));
                destructibleWalls.addAll(board.getObjectsOfType(x, y - 1, "DESTRUCTIBLE_WALL_WITH_POWERUP"));
            }

            if (!isOutOfBounds(x, y + 1)) {
                destructibleWalls.addAll(board.getObjectsOfType(x, y + 1, "DESTRUCTIBLE_WALL"));
                destructibleWalls.addAll(board.getObjectsOfType(x, y + 1, "DESTRUCTIBLE_WALL_WITH_POWERUP"));
            }

            return !destructibleWalls.isEmpty();
        }

        boolean isNearbyEnemy() {
            Player player = getMyPlayer();
            List<BoardObject> potentialEnemies = new ArrayList<>();
            int x = player.getX();
            int y = player.getY();

            for (int i = 1; i <= player.getBombRange(); i++) {
                if (!isOutOfBounds(x - i, y))
                    potentialEnemies.addAll(board.getObjectsOfType(x - i, y, "PLAYER"));
                if (!isOutOfBounds(x + i, y))
                    potentialEnemies.addAll(board.getObjectsOfType(x + i, y, "PLAYER"));
                if (!isOutOfBounds(x, y - i))
                    potentialEnemies.addAll(board.getObjectsOfType(x, y - i, "PLAYER"));
                if (!isOutOfBounds(x, y + 1))
                    potentialEnemies.addAll(board.getObjectsOfType(x, y + 1, "PLAYER"));
            }

            return !potentialEnemies.isEmpty();
        }

        List<String> getAvailableMoves(int x, int y) {
            List<String> availableMoves = new ArrayList<String>();

            if (isValidAndSafe(x - 1, y))
                availableMoves.add(">> MOVE UP");

            if (isValidAndSafe(x + 1, y))
                availableMoves.add(">> MOVE DOWN");

            if (isValidAndSafe(x, y - 1))
                availableMoves.add(">> MOVE LEFT");

            if (isValidAndSafe(x, y + 1))
                availableMoves.add(">> MOVE RIGHT");

            return availableMoves;
        }

        List<BoardObject> getAllObjects(int x, int y) {
            return board.getAllObjects(x, y);
        }

        List<BoardObject> getAllBombs() {
            return board.getAllBombs();
        }

        List<BoardObject> getAllFlares() {
            return board.getAllFlares();
        }

        List<BoardObject> getAllDestructibleWalls() {
            return board.getAllDestructibleWalls();
        }

        List<BoardObject> getAllPowerups() {
            return board.getAllPowerups();
        }

        List<BoardObject> getAllObjectsExceptIndestructibleWalls() {
            return board.getAllObjectsExceptIndestructibleWalls();
        }

        Player getMyPlayer() {
            for (Player player : playerList) {
                if (player.getName().equals("AI_1306402873_Mika"))
                    return player;
            }

            return null;
        }

        public void makeAction() {
            if (bombDropped) {
                if (lastMove.equals(">> MOVE UP"))
                    printMovement(">> MOVE DOWN");
                else if (lastMove.equals(">> MOVE DOWN"))
                    printMovement(">> MOVE UP");
                else if (lastMove.equals(">> MOVE LEFT"))
                    printMovement(">> MOVE RIGHT");
                else if (lastMove.equals(">> MOVE RIGHT"))
                    printMovement(">> MOVE LEFT");

                bombDropped = false;
            }

            if (stayCount > 10) {
                printMovement(getRandomDirection());
                stayCount = 0;
            }

            String nextMove;
            Player myPlayer = getMyPlayer();
            nextMove = checkForBomb(myPlayer.getX(), myPlayer.getY());

            if (nextMove != null) {
                printMovement(nextMove);
                return;
            }

            nextMove = searchForPowerup();

            if (nextMove != null) {
                printMovement(nextMove);
                return;
            }

            nextMove = huntForEnemy();

            if (nextMove != null) {
                printMovement(nextMove);
                return;
            }

            stayCount++;
            System.out.println(">> STAY");
        }

        // Return null if safe, String of best direction otherwise
        String checkForBomb(int x, int y) {
            boolean upIsSafe = isValidAndSafe(x - 1, y);
            boolean downIsSafe = isValidAndSafe(x + 1, y);
            boolean leftIsSafe = isValidAndSafe(x, y - 1);
            boolean rightIsSafe = isValidAndSafe(x, y + 1);

            if (!board.getObjectsOfType(x, y, "BOMB").isEmpty()) {
                // Running anywhere is fine
                if (upIsSafe)
                    return ">> MOVE UP";
                if (downIsSafe)
                    return ">> MOVE DOWN";
                if (leftIsSafe)
                    return ">> MOVE LEFT";
                if (rightIsSafe)
                    return ">> MOVE RIGHT";
            }

            List<BoardObject> dangers = board.getObjectsOfType(x, y, "DANGER_ZONE");

            if (dangers.isEmpty())
                return null;

            for (BoardObject danger : dangers) {
                if (danger.getName().equals("DANGER_ZONE_UP"))
                    downIsSafe = false;
                if (danger.getName().equals("DANGER_ZONE_DOWN"))
                    upIsSafe = false;
                if (danger.getName().equals("DANGER_ZONE_LEFT"))
                    rightIsSafe = false;
                if (danger.getName().equals("DANGER_ZONE_RIGHT"))
                    leftIsSafe = false;
            }

            if (upIsSafe)
                return ">> MOVE UP";
            if (downIsSafe)
                return ">> MOVE DOWN";
            if (leftIsSafe)
                return ">> MOVE LEFT";
            if (rightIsSafe)
                return ">> MOVE RIGHT";

            return getRandomDirection();
        }

        String searchForPowerup() {
            Player player = getMyPlayer();
            int x = player.getX();
            int y = player.getY();

            clearTraversalFlag();

            if (searchForPowerupRecursively(x - 1, y))
                return ">> MOVE UP";
            if (searchForPowerupRecursively(x + 1, y))
                return ">> MOVE DOWN";
            if (searchForPowerupRecursively(x, y - 1))
                return ">> MOVE LEFT";
            if (searchForPowerupRecursively(x, y + 1))
                return ">> MOVE RIGHT";

            return null;
        }

        boolean searchForPowerupRecursively(int x, int y) {
            if (isOutOfBounds(x, y))
                return false;

            traversalFlag[x][y] = true;

            if (!board.getObjectsOfType(x, y, "POWERUP").isEmpty())
                return true;

            boolean found = false;

            if (!isOutOfBounds(x - 1, y) && !traversalFlag[x - 1][y]) {
                found = found || searchForPowerupRecursively(x - 1, y);
            }

            if (!isOutOfBounds(x + 1, y) && !traversalFlag[x + 1][y]) {
                found = found || searchForPowerupRecursively(x + 1, y);
            }

            if (!isOutOfBounds(x, y - 1) && !traversalFlag[x][y - 1]) {
                found = found || searchForPowerupRecursively(x, y - 1);
            }

            if (!isOutOfBounds(x, y + 1) && !traversalFlag[x][y + 1]) {
                found = found || searchForPowerupRecursively(x, y + 1);
            }

            return found;
        }

        void clearTraversalFlag() {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    traversalFlag[i][j] = false;
                }
            }
        }

        String huntForEnemy() {
            Player myPlayer = getMyPlayer();
            int x = myPlayer.getX();
            int y = myPlayer.getY();

            if (isNearbyDestructibleWall() && canDropBomb()) {
                bombDropped = true;
                return ">> DROP BOMB";
            }

            if (isNearbyEnemy() && canDropBomb()) {
                bombDropped = true;
                return ">> DROP BOMB";
            }

            Player nearestEnemy = findNearestEnemy();
            if (nearestEnemy != null) {
                if (nearestEnemy.getX() < x && isValidAndSafe(x - 1, y))
                    return ">> MOVE UP";
                if (nearestEnemy.getX() > x && isValidAndSafe(x + 1, y))
                    return ">> MOVE DOWN";
                if (nearestEnemy.getX() < x && isValidAndSafe(x, y - 1))
                    return ">> MOVE LEFT";
                if (nearestEnemy.getX() < x && isValidAndSafe(x, y + 1))
                    return ">> MOVE RIGHT";
            }

            return null;
        }

        Player findNearestEnemy() {
            if (playerList.isEmpty())
                return null;

            Player nearestEnemy = playerList.get(0);
            Player myPlayer = getMyPlayer();

            for (int i = 1; i < playerList.size(); i++) {
                Player newPlayer = playerList.get(i);
                int currentDistance = manhattanDistance(myPlayer.getX(), myPlayer.getY(), nearestEnemy.getX(), nearestEnemy.getY());
                int newDistance = manhattanDistance(myPlayer.getX(), myPlayer.getY(), newPlayer.getX(), newPlayer.getY());

                if (newDistance < currentDistance)
                    nearestEnemy = newPlayer;
            }

            return nearestEnemy;
        }

        int manhattanDistance(int x1, int y1, int x2, int y2) {
            return Math.abs(x1 - x2) + Math.abs(y1 - y2);
        }

        void printMovement(String movement) {
            if (!movement.equals(">> DROP BOMB"))
                lastMove = movement;
            System.out.println(movement);
        }

        boolean canDropBomb() {
            Player player = getMyPlayer();

            return player.getBombCount() > 0;
        }

        int getTurn() {
            return turn;
        }

        void setTurn(int turn) {
            this.turn = turn;
        }

        int getPlayerCount() {
            return playerCount;
        }

        void setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
        }

        List<Player> getPlayerList() {
            return playerList;
        }

        void setPlayerList(List<Player> playerList) {
            this.playerList = playerList;
        }

        Board getBoard() {
            return board;
        }

        void setBoard(Board board) {
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

        List<BoardObject> getObjectsOfType(int x, int y, String type) {
            List<BoardObject> searchedObjects = new ArrayList<BoardObject>();
            List<BoardObject> objects = getAllObjects(x, y);

            for (BoardObject object : objects) {
                if (object.getType().equals(type))
                    searchedObjects.add(object);
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

        void putObject(int x, int y, BoardObject object) {
            nodes[x][y].getObjectList().add(object);
        }

        void clearDangerZones() {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    nodes[i][j].setObjectList(nodes[i][j].getObjectList().stream().filter(o -> !o.getType().equals("DANGER_ZONE")).collect(Collectors.toList()));
                }
            }
        }
    }

    class BoardNode {
        List<BoardObject> objectList;

        public BoardNode(String nodeString, int x, int y) {
            objectList = new ArrayList<>();
            nodeString = nodeString.trim();
            String[] objectStrings = nodeString.split(";");

            for (String objectString : objectStrings) {
                if (objectString.isEmpty()) {
                    // no-op
                } else if (AI_1306402873_Mika.Helper.isNumeric(objectString)) {
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

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        int getBombCount() {
            return bombCount;
        }

        void setBombCount(int bombCount) {
            this.bombCount = bombCount;
        }

        int getMaxBomb() {
            return maxBomb;
        }

        void setMaxBomb(int maxBomb) {
            this.maxBomb = maxBomb;
        }

        int getBombRange() {
            return bombRange;
        }

        void setBombRange(int bombRange) {
            this.bombRange = bombRange;
        }

        String getStatus() {
            return status;
        }

        void setStatus(String status) {
            this.status = status;
        }

        int getScore() {
            return score;
        }

        void setScore(int score) {
            this.score = score;
        }

        int getX() {
            return x;
        }

        void setX(int x) {
            this.x = x;
        }

        int getY() {
            return y;
        }

        void setY(int y) {
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
