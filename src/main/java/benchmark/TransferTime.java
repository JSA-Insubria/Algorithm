package benchmark;

import benchmark.model.BlockFile;
import benchmark.model.MovedBlock;
import fill.FillFiles;
import fill.FillNodes;
import model.Block;
import model.DataFile;
import model.Node;
import model.Replica;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferTime {

    private final String folder;
    private List<Node> nodeList;
    private Map<String, Node> nodeMap;
    private Map<String, DataFile> fileMap;

    private List<String> blockIDList;
    private Map<String, BlockFile> blockFileList;

    private Map<String, List<BlockFile>> blockMovedPerFile;

    private Map<String, MovedBlock> movedBlockMap;

    public TransferTime(String folder) {
        this.folder = folder;
    }

    public Map<String, MovedBlock> getTransferTime() {
        checkFile();

        movedBlockMap = new HashMap<>();
        nodeList = new FillNodes(folder).readNodes();
        fileMap = new FillFiles(folder).readFiles();
        getNodeMap();
        getBlockIDList();

        Map<String, List<MovedBlock>> blockMoved = readQueryFolder();
        blockMovedPerFile = new HashMap<>();

        // CSV header
        printTimeMean("transferTimes.csv", "query" + "," + "bytes" + "," + "time" + ","
                + "total" + "," + "moved" + "," + "percentage" + "\n");

        // for every queries
        for (Map.Entry<String, List<MovedBlock>> map : blockMoved.entrySet()) {
            getMovedBlockPerQuery(map.getKey(), map.getValue());
            blockMovedPerFile.clear();
        }
        return movedBlockMap;
    }

    private void getMovedBlockPerQuery(String queryName, List<MovedBlock> movedBlockList) {
        for (MovedBlock movedBlock : movedBlockList) { // for every block moved
            if (blockIDList.contains(movedBlock.getBlock())) { // check if block is a files' block
                BlockFile tmp = blockFileList.get(movedBlock.getBlock()); // generic file information
                BlockFile blockFile = new BlockFile(tmp.getId(), tmp.getFile(), tmp.getReplicaList(),
                        nodeMap.get(movedBlock.getSrc()).getHostName(), nodeMap.get(movedBlock.getDest()).getHostName(),
                        movedBlock.getBytes(), movedBlock.getDuration());
                getOnlyMovedBlock(blockFile, queryName);
                putMovedBlockIntoMap(blockFile);
            }
        }
        countMovedBlockPerFile(queryName);
    }

    private void getOnlyMovedBlock(BlockFile blockFile, String queryName) {
        if (!blockFile.getSrc().equals(blockFile.getDest())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(queryName).append(" -> ").append(blockFile.getId())
                    .append(" -> Src: ").append(blockFile.getSrc())
                    .append(" -> Dest: ").append(blockFile.getDest())
                    .append(" -> File: ").append(blockFile.getFile())
                    .append(" -> Location: ");
            for (Replica replica : blockFile.getReplicaList()) {
                stringBuilder.append(replica.getHostName()).append(",");
            }
            stringBuilder.append(" -> TransferTime: ").append(blockFile.getTimes())
                    .append(" -> TransferBytes: ").append(blockFile.getBytes());
            stringBuilder.append("\n");
            System.out.print(stringBuilder.toString());
            printTimeMean("workload_nodes.log", stringBuilder.toString());
        }
    }

    private void putMovedBlockIntoMap(BlockFile blockFile) {
        blockMovedPerFile.computeIfAbsent(blockFile.getFile(), k -> new ArrayList<>()).add(blockFile);
    }

    private void countMovedBlockPerFile(String queryName) {
        long bytes = 0;
        double time = 0;
        int totNum = 0;
        int totMoved = 0;
        for (Map.Entry<String, List<BlockFile>> blockMap : blockMovedPerFile.entrySet()) {
            int queryCount = blockMap.getValue().size(), queryCountMoved = 0;
            for (BlockFile blockFile : blockMap.getValue()) {
                if (!blockFile.getSrc().equals(blockFile.getDest())) {
                    queryCountMoved++;
                    bytes += blockFile.getBytes();
                    time += blockFile.getTimes();
                }
            }
            printTimeMean("queryCorrectNodes.log", "Query: " + queryName +
                    " -> File: " + blockMap.getKey() +
                    " -> Total: " + queryCount +
                    " -> Moved: " + queryCountMoved +
                    " -> Percentage: " + getPercentage(queryCountMoved, queryCount) + "%\n");
            totNum += queryCount;
            totMoved += queryCountMoved;
        }
        movedBlockMap.put(queryName, new MovedBlock("", "", "", bytes, time));
        printTimeMean("transferTimes.csv", queryName + "," + bytes + "," + time + ","
                + totNum + "," + totMoved + "," + getPercentage(totMoved, totNum) + "\n");
    }

    private void getNodeMap() {
        nodeMap = new HashMap<>();
        for (Node node : nodeList) {
            nodeMap.put("/" + node.getName().split(":")[0], node);
        }
    }

    private void getBlockIDList() {
        blockIDList = new ArrayList<>();
        blockFileList = new HashMap<>();
        for (Map.Entry<String, DataFile> map : fileMap.entrySet()) {
            DataFile dataFile = map.getValue();
            for (Block block : dataFile.getBlockList()) {
                String blockID =  block.getId().substring(0, block.getId().lastIndexOf("_"));
                blockIDList.add(blockID);
                blockFileList.put(blockID, new BlockFile(blockID, dataFile.getName(), block.getReplicaList()));
            }
        }
    }

    private Map<String, List<MovedBlock>> readQueryFolder() {
        Map<String, List<MovedBlock>> map = new HashMap<>();
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

    private Map<String, List<MovedBlock>> readTestFolder(File file) {
        List<MovedBlock> movedBlockList = new ArrayList<>();
        String queryName = file.getName();
        if (queryName.contains("q")) {
            File[] queryFolders = file.listFiles();
            if (queryFolders != null) {
                for (File qDir : queryFolders) {
                    for (Node node : nodeList) {
                        movedBlockList.addAll(readQueriesTimes(node.getHostName(), qDir.getPath()));
                    }
                }
            }
        }
        Map<String, List<MovedBlock>> map = new HashMap<>();
        map.put(queryName, movedBlockList);
        return map;
    }

    private List<MovedBlock> readQueriesTimes(String node, String subPath) {
        List<MovedBlock> movedList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader( new FileReader(subPath +
                    File.separator + "hdfs_read_write" + File.separator + "hdfs_read_" + node + ".log"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.contains("NONMAPREDUCE")) {
                    movedList.add(parseLine(line));
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

        long bytes = Long.parseLong(split[2].trim().substring(split[2].trim().indexOf(" ")).trim());
        double duration = Double.parseDouble(split[8].trim().substring(split[8].trim().indexOf(" ")).trim());
        String block = split[7].substring(split[7].lastIndexOf(":") + 1);
        double seconds = duration / 1_000_000_000.0;
        return new MovedBlock(src, dest, block.substring(0, block.lastIndexOf("_")), bytes, seconds);
    }

    public String getPercentage(int n, int total) {
        float proportion = ((float) n) / ((float) total);
        return String.valueOf(proportion * 100);
    }

    private void checkFile() {
        File fileName = new File(folder + File.separator + "workload_nodes.log");
        File fileName2 = new File(folder + File.separator + "queryCorrectNodes.log");
        File fileName3 = new File(folder + File.separator + "transferTimes.csv");
        if (fileName.exists() | fileName2.exists() | fileName3.exists()) {
            fileName.delete();
            fileName2.delete();
            fileName3.delete();
        }
    }

    private void printTimeMean(String file, String line) {
        File fileName = new File(folder + File.separator +  file);
        try {
            FileWriter myWriter = new FileWriter(fileName, true);
            myWriter.write(line);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
