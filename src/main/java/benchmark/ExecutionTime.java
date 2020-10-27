package benchmark;

import benchmark.model.QueryStats;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ExecutionTime {

    private final String folder;

    public ExecutionTime(String folder) {
        this.folder = folder;
    }

    public Map<String, String> getExecutionTime() {
        return readQueryFolder();
    }

    private Map<String, String> readQueryFolder() {
        Map<String, String> map = new HashMap<>();
        File directory = new File(folder);
        File[] folderFiles = directory.listFiles();
        if (folderFiles != null) {
            for (File file : folderFiles) {
                if (file.getName().contains("q")) {
                    map.putAll(readTestFolder(file));
                }
            }
        }
        return map;
    }

    private Map<String, String> readTestFolder(File file) {
        List<Double> queryTimeList = new ArrayList<>();
        String queryName = file.getName();
        Map<String, String> map = new HashMap<>();
        if (queryName.contains("q") && file.isDirectory()) {
            File[] queryFolders = file.listFiles();
            if (queryFolders != null) {
                for (File qDir : queryFolders) {
                    queryTimeList.add(readQueryExecutionTime(qDir));
                }
            }
            map.put(queryName, getExecutionTimeMean(queryTimeList));
        }
        return map;
    }

    private String getExecutionTimeMean(List<Double> queryTimeList) {
        int num = queryTimeList.size();
        return String.valueOf(roundValue(queryTimeList.stream().mapToDouble(Double::doubleValue).sum()/num));
    }

    private double readQueryExecutionTime(File qDir) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(qDir + File.separator + "QueryExecutionTime.log"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("seconds")) {
                    return Double.parseDouble(line.substring(line.lastIndexOf(":") + 2, line.lastIndexOf(" ")));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double roundValue(Double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

}
