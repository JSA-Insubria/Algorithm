package benchmark;

import problemsolver.model.MovedBlock;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Benchmark {

    //private static final String folder = "data/" + "Original/";
    private static final String folder = "data/" + "First/";
    //private static final String folder = "data/" + "Mid/";
    //private static final String folder = "data/" + "Last/";

    private static int nQueries = 0;
    private static HashMap<String, String> queriesMap;
    private static Map<String, MovedBlock> movedBlockMap;

    public static void main(String[] args) {
        deleteTimeMean();

        TransferTime transferTime = new TransferTime(folder);
        movedBlockMap = transferTime.getTransferTime();
        ExecutionTime executionTime = new ExecutionTime(folder);
        queriesMap = executionTime.getExecutionTime();

        List<String> sortedKeys = new ArrayList<String>(queriesMap.keySet());
        sortedKeys.sort(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Integer val1 = Integer.parseInt(s1.substring(1));
                Integer val2 = Integer.parseInt(s2.substring(1));
                return val1.compareTo(val2);
            }
        });

        nQueries = queriesMap.size();
        int[] tQueries = readQueriesTimes();

        addTimesToQueries(sortedKeys, tQueries);
        addTimesToMovedBlocks(sortedKeys, tQueries);
        prettyPrint(sortedKeys, tQueries);
    }

    private static void addTimesToQueries(List<String> sortedKeys, int[] tQueries) {
        int i = 0;
        for (String key : sortedKeys) {
            double newTime = Double.parseDouble(queriesMap.get(key)) * tQueries[i++];
            newTime = BigDecimal.valueOf(newTime).setScale(3, RoundingMode.HALF_UP).doubleValue();
            queriesMap.replace(key, String.valueOf(newTime));
        }
    }

    private static void addTimesToMovedBlocks(List<String> sortedKeys, int[] tQueries) {
        int i = 0;
        for (String key : sortedKeys) {
            MovedBlock old = movedBlockMap.get(key);
            long bytes = old.getBytes() * tQueries[i];
            double duration = old.getDuration() * tQueries[i++];
            duration = BigDecimal.valueOf(duration).setScale(3, RoundingMode.HALF_UP).doubleValue();
            movedBlockMap.replace(key, new MovedBlock("", "", "", bytes, duration));
        }
    }

    private static int[] readQueriesTimes() {
        int[] numbers = new int[nQueries];
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("data/execute.sh"));
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("query/query-")) {
                    numbers[i++] = Integer.parseInt(line.substring(line.indexOf("..")+2, line.indexOf("}")));
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numbers;
    }

    private static void prettyPrint(List<String> sortedKeys, int[] tQueries) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("query").append(",")
                .append("times").append(",")
                .append("mean").append(",")
                .append("transferred_bytes").append(",")
                .append("transferred_time").append("\n");
        int i = 0;
        for (String key : sortedKeys) {
            stringBuilder.append(key).append(",")
                    .append(tQueries[i++]).append(",")
                    .append(queriesMap.get(key)).append(",")
                    .append(movedBlockMap.get(key).getBytes()).append(",")
                    .append(movedBlockMap.get(key).getDuration()).append("\n");
        }
        printTimeMean(stringBuilder.toString());
    }

    private static void deleteTimeMean() {
        File fileName = new File(folder + "benchmark.csv");
        if (fileName.exists()) {
            fileName.delete();
        }
    }

    private static void printTimeMean(String line) {
        File fileName = new File(folder + "benchmark.csv");
        try {
            FileWriter myWriter = new FileWriter(fileName, true);
            myWriter.write(line + "\n");
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
