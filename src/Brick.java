import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Brick {
    private int[] x;
    private int[] y;
    private int[] z;
    private int supportersCount;
    private List<Brick> resters = new ArrayList<>();
    private static int[][] table = new int[10][10];
    private static List<Brick> allBricks = new ArrayList<>();
    private static Map<Integer, List<Brick>> bricksByLowerZ = new HashMap<>();
    private static Map<Integer, List<Brick>> bricksByUpperZ = new HashMap<>();

    public Brick(String line) {
        int[] tempArray = Arrays.stream(line.split("[~,]")).mapToInt(Integer::parseInt).toArray();
        x = new int[]{tempArray[0], tempArray[3]};
        y = new int[]{tempArray[1], tempArray[4]};
        z = new int[]{tempArray[2], tempArray[5]};
        Arrays.sort(x);
        Arrays.sort(y);
        Arrays.sort(z);
    }

    public void drop() {
        int base = 0;
        int height = z[1] - z[0] + 1;
        for (int i = x[0]; i <= x[1]; i++) {
            for (int j = y[0]; j <= y[1]; j++) {
                if (table[i][j] > base) base = table[i][j];
            }
        }
        z[0] = base + 1;
        z[1] = base + height;
        for (int i = x[0]; i <= x[1]; i++) {
            for (int j = y[0]; j <= y[1]; j++) {
                table[i][j] = z[1];
            }
        }
        if (bricksByLowerZ.containsKey(z[0])) {
            bricksByLowerZ.get(z[0]).add(this);
        } else bricksByLowerZ.put(z[0], new ArrayList<>(List.of(this)));
        if (bricksByUpperZ.containsKey(z[1])) {
            bricksByUpperZ.get(z[1]).add(this);
        } else bricksByUpperZ.put(z[1], new ArrayList<>(List.of(this)));
    }

    public boolean isOverlapping(Brick b) {
        return Math.max(x[0], b.x[0]) <= Math.min(x[1], b.x[1]) && Math.max(y[0], b.y[0]) <= Math.min(y[1], b.y[1]);
    }
    private void countSupporters () {
        if (bricksByUpperZ.containsKey(z[0] - 1)) {
            for (Brick otherBrick : bricksByUpperZ.get(z[0] - 1)) {
                if (this.isOverlapping(otherBrick)) {
                    supportersCount++;
                }
            }
        }
    }

    private void getResters() {
        if (bricksByLowerZ.containsKey(z[1] + 1)) {
            for (Brick otherBrick : bricksByLowerZ.get(z[1] + 1)) {
                if (this.isOverlapping(otherBrick)) {
                    resters.add(otherBrick);
                }
            }
        }
    }

    public static void loadBricks ( int i) throws IOException {
        List<String> input = Files.readAllLines(Path.of("src/input" + i + ".txt"));
        input.forEach(line -> allBricks.add(new Brick(line)));
        allBricks.sort(Comparator.comparingInt(brick -> brick.z[0]));
        allBricks.forEach(Brick::drop);
        allBricks.forEach(brick -> {
            brick.countSupporters();
            brick.getResters();
        });
    }
    public boolean canBeRemoved () {
        if (this.resters != null) {
            for (Brick rester : resters) {
                if (rester.supportersCount == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int countRemovable () {
        int count = 0;
        for (Brick currBrick : allBricks) {
            if (currBrick.canBeRemoved()) count++;
        }
        return count;
    }
}
