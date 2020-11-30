package benchmark;

import benchmark.model.MovedBlock;
import benchmark.model.QueryStats;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Benchmark {

    private static String path = "algorithm_data" + File.separator;
    private static final String execution_sh_path = "algorithm_data" + File.separator;

    private static int nQueries = 0;
    private static Map<String, MovedBlock> movedBlockMap;
    private static Map<String, String> executionTimeMap;
    private static Map<String, QueryStats> cpuTimeMap;

    public static void main(String[] args) {
        path = path + args[0] + File.separator;
        deleteTimeMean();

        TransferTime transferTime = new TransferTime(path);
        movedBlockMap = transferTime.getTransferTime();
        ExecutionTime executionTime = new ExecutionTime(path);
        executionTimeMap = executionTime.getExecutionTime();
        CPUTime cpuTime = new CPUTime(path);
        cpuTimeMap = cpuTime.getCPUTime();

        List<String> sortedKeys = new ArrayList<String>(executionTimeMap.keySet());
        sortedKeys.sort(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Integer val1 = Integer.parseInt(s1.substring(1));
                Integer val2 = Integer.parseInt(s2.substring(1));
                return val1.compareTo(val2);
            }
        });

        nQueries = executionTimeMap.size();
        int[] tQueries = readQueriesTimes();

        //addZipfDistribution(sortedKeys, tQueries); //uncomment also in setMatrixValues of PreCoOccurrenceMatrix class
        prettyPrint(sortedKeys, tQueries);
    }

    private static void addZipfDistribution(List<String> sortedKeys, int[] tQueries) {
        addTimesToMovedBlocks(sortedKeys, tQueries);
        addTimesToQueries(sortedKeys, tQueries);
        addTimesToQueryStats(sortedKeys, tQueries);
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

    private static void addTimesToQueries(List<String> sortedKeys, int[] tQueries) {
        int i = 0;
        for (String key : sortedKeys) {
            double newTime = Double.parseDouble(executionTimeMap.get(key)) * tQueries[i++];
            newTime = BigDecimal.valueOf(newTime).setScale(3, RoundingMode.HALF_UP).doubleValue();
            executionTimeMap.replace(key, String.valueOf(newTime));
        }
    }

    private static void addTimesToQueryStats(List<String> sortedKeys, int[] tQueries) {
        int i = 0;
        for (String key : sortedKeys) {
            QueryStats old = cpuTimeMap.get(key);
            double cpuTime = old.getCpuTimeSpent() * tQueries[i];
            long hdfsRead = old.getHdfsRead() * tQueries[i];
            long hdfsWrite = old.getHdfsWrite() * tQueries[i++];
            cpuTimeMap.replace(key, new QueryStats(roundValue(cpuTime), hdfsRead, hdfsWrite));
        }
    }

    private static int[] readQueriesTimes() {
        int[] numbers = new int[nQueries];
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(execution_sh_path + "execute.sh"));
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
                .append("executiontime").append(",")
                .append("cputime").append(",")
                .append("hdfsread").append(",")
                .append("transferred_bytes").append(",")
                .append("transferred_time").append("\n");
        int i = 0;
        for (String key : sortedKeys) {
            stringBuilder.append(key).append(",")
                    .append(tQueries[i++]).append(",")
                    .append(executionTimeMap.get(key)).append(",")
                    .append(cpuTimeMap.get(key).getCpuTimeSpent()).append(",")
                    .append(cpuTimeMap.get(key).getHdfsRead()).append(",")
                    .append(movedBlockMap.get(key).getBytes()).append(",")
                    .append(movedBlockMap.get(key).getDuration()).append("\n");
        }
        printTimeMean(stringBuilder.toString());
    }

    private static void deleteTimeMean() {
        File fileName = new File(path + "benchmark.csv");
        if (fileName.exists()) {
            fileName.delete();
        }
    }

    private static void printTimeMean(String line) {
        File fileName = new File(path + "benchmark.csv");
        try {
            FileWriter myWriter = new FileWriter(fileName, true);
            myWriter.write(line + "\n");
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double roundValue(Double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

}
