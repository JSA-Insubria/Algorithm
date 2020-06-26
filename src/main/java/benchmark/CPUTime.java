package benchmark;

import benchmark.model.QueryStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPUTime {

    private final String folder;

    public CPUTime(String folder) {
        this.folder = folder;
    }

    public Map<String, QueryStats> getCPUTime() {
        return readQueryFolder();
    }

    private Map<String, QueryStats> readQueryFolder() {
        Map<String, QueryStats> map = new HashMap<>();
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

    private Map<String, QueryStats> readTestFolder(File file) {
        List<QueryStats> queryStatsList = new ArrayList<>();
        String queryName = file.getName();
        if (queryName.contains("q")) {
            File[] queryFolders = file.listFiles();
            if (queryFolders != null) {
                for (File qDir : queryFolders) {
                    queryStatsList.add(readQueryCPUTime(qDir));
                }
            }
        }
        Map<String, QueryStats> map = new HashMap<>();
        map.put(queryName, getQueryStatsMean(queryStatsList));
        return map;
    }

    private QueryStats getQueryStatsMean(List<QueryStats> queryStatsList) {
        int num = queryStatsList.size();
        double cpuTime = 0;
        long hdfsRead = 0;
        long hdfsWrite = 0;
        for (QueryStats queryStats: queryStatsList) {
            cpuTime += queryStats.getCpuTimeSpent();
            hdfsRead += queryStats.getHdfsRead();
            hdfsWrite += queryStats.getHdfsWrite();
        }
        return new QueryStats(roundValue(cpuTime/num), hdfsRead/num, hdfsWrite/num);
    }

    private QueryStats readQueryCPUTime(File qDir) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(qDir + "/QueryCPUTime.log"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("CPU")) {
                    return getQueryStats(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new QueryStats(0, 0,0);
    }

    private QueryStats getQueryStats(String line) {
        String[] stats = line.split(",");
        return new QueryStats(roundValue(Double.parseDouble(stats[0].split(":")[1])),
                Long.parseLong(stats[1].split(":")[1]),
                Long.parseLong(stats[2].split(":")[1]));
    }

    private double roundValue(Double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

}
