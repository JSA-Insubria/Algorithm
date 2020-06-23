package benchmark;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ExecutionTime {

    private final String folder;

    public ExecutionTime(String folder) {
        this.folder = folder;
    }

    public HashMap<String, String> getExecutionTime() {
        return readTimes();
    }

    private HashMap<String, String> readTimes() {
        HashMap<String, String> queriesMap = new HashMap<>();
        File directory = new File(folder);
        File[] folderFiles = directory.listFiles();
        if (folderFiles != null) {
            for (File file : folderFiles) {
                String queryName = file.getName();
                if (queryName.contains("q")) {
                    double mean = 0;
                    File[] queryFolders = file.listFiles();
                    if (queryFolders != null) {
                        double sum = 0;
                        for (File qDir : queryFolders) {
                            String execNum = qDir.getName().substring(qDir.getName().lastIndexOf("_") + 1);
                            BufferedReader bufferedReader = null;
                            try {
                                bufferedReader = new BufferedReader(new FileReader(qDir + "/QueryExecutionTime.log"));
                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    String time = line.substring(line.lastIndexOf(":") + 2, line.lastIndexOf(" "));
                                    sum += Double.parseDouble(time);
                                    System.out.println("Query: " + queryName + "_" + execNum + " -> Time: " + time);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mean = BigDecimal.valueOf(sum / queryFolders.length).setScale(3, RoundingMode.HALF_UP).doubleValue();
                        queriesMap.put(queryName, String.valueOf(mean));
                    }
                }
            }
        }
        return queriesMap;
    }

}
