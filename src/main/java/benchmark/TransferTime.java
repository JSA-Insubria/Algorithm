package benchmark;

import fill.FillNodes;
import model.*;
import benchmark.model.MovedBlock;

import java.io.*;
import java.util.*;

public class TransferTime {

    private final String folder;
    private List<Node> nodeList;

    public TransferTime(String folder) {
        this.folder = folder;
    }

    public Map<String, MovedBlock> getTransferTime() {
        FillNodes fillNodes = new FillNodes("data" + File.separator);
        nodeList = fillNodes.readNodes();
        return readQueryFolder();
    }

    private Map<String, MovedBlock> readQueryFolder() {
        Map<String, MovedBlock> map = new HashMap<>();
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

    private Map<String, MovedBlock> readTestFolder(File file) {
        List<MovedBlock> movedBlockList = new ArrayList<>();
        String queryName = file.getName();
        if (queryName.contains("q")) {
            File[] queryFolders = file.listFiles();
            if (queryFolders != null) {
                for (File qDir : queryFolders) {
                    movedBlockList.add(getMovedBlockSum(qDir));
                }
            }
        }
        Map<String, MovedBlock> map = new HashMap<>();
        map.put(queryName, getMeanOfMovedBlock(movedBlockList));
        return map;
    }

    private MovedBlock getMovedBlockSum(File qDir) {
        List<MovedBlock> movedBlocksPerSubQueries = new ArrayList<>();
        for (Node node : nodeList) {
            movedBlocksPerSubQueries.addAll(readQueriesTimes(node.getHostName(), qDir.getPath()));
        }
        return getSumOfMovedBlock(movedBlocksPerSubQueries);
    }

    private MovedBlock getSumOfMovedBlock(List<MovedBlock> movedBlockList) {
        long bytes = 0;
        double duration = 0;
        for (MovedBlock movedBlock : movedBlockList) {
            bytes += movedBlock.getBytes();
            duration += movedBlock.getDuration();
            System.out.println(movedBlock.toString());
        }
        return new MovedBlock("", "", "", bytes, duration);
    }

    private MovedBlock getMeanOfMovedBlock(List<MovedBlock> movedBlockList) {
        long bytes = 0, size = movedBlockList.size();
        double duration = 0;
        for (MovedBlock movedBlock : movedBlockList) {
            bytes += movedBlock.getBytes();
            duration += movedBlock.getDuration();
            System.out.println(movedBlock.toString());
        }
        return new MovedBlock("", "", "", bytes/size, duration/size);
    }

    private List<MovedBlock> readQueriesTimes(String node, String subPath) {
        List<MovedBlock> movedList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader( new FileReader(subPath +
                    File.separator + "hdfs_read_write" + File.separator + "hdfs_read_" + node + ".log"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.contains("NONMAPREDUCE")) {
                    MovedBlock movedBlock = parseLine(line);
                    if (movedBlock != null) {
                        movedList.add(movedBlock);
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movedList;
    }

    private MovedBlock parseLine(String line) {
        // src: /172.18.0.4:9866,
        // dest: /172.18.0.6:54054,
        // bytes: 2760944,
        // op: HDFS_READ,
        // cliID: DFSClient_attempt_1587929308405_0110_m_000000_0_1963805643_1,
        // offset: 0,
        // srvID: efefec75-1510-4c24-a9f3-46cc14cded7b,
        // blockid: BP-442144132-172.18.0.2-1587929279487:blk_1073743497_2673,
        // duration(ns): 650682243

        String[] split = line.split(",");
        String src = split[0].substring(split[0].indexOf("/"), split[0].lastIndexOf(":"));
        String dest = split[1].substring(split[1].indexOf("/"), split[1].lastIndexOf(":"));
        if (src.equals(dest)) {
            return null;
        }
        else {
            long bytes = Long.parseLong(split[2].trim().substring(split[2].trim().indexOf(" ")).trim());
            double duration = Double.parseDouble(split[8].trim().substring(split[8].trim().indexOf(" ")).trim());
            String block = split[7].substring(split[7].lastIndexOf(":")+1);
            double seconds = duration / 1_000_000_000.0;
            return new MovedBlock(src, dest, block, bytes, seconds);
        }
    }

}

