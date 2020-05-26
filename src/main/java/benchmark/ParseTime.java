package benchmark;

import model.Block;
import model.DataFile;
import model.MovedBlock;
import model.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ParseTime {

    private final List<Node> nodeList;
    private final LinkedHashMap<String, DataFile> files;

    public ParseTime(List<Node> nodeList, LinkedHashMap<String, DataFile> files) {
        this.nodeList = nodeList;
        this.files = files;
    }

    public List<MovedBlock> getMovedBlockList() {
        List<MovedBlock> movedBlockList = new ArrayList<>();
        for (Node node : nodeList) {
            List<MovedBlock> list = readQueriesTimes(node.getHostName());
            movedBlockList.addAll(list);
        }
        return movedBlockList;
    }

    public void test() {
        List<String> blocksList = getBlocksList();
        List<MovedBlock> movedBlockList = getMovedBlockList();
        long times = 0;
        for (MovedBlock movedBlock : movedBlockList) {
            if (blocksList.contains(movedBlock.getBlock())) {
                times += movedBlock.getDuration();
                System.out.println("Blocco: " + movedBlock.getBlock());
            }
        }
        System.out.println("Tempo spostamento blocchi tra nodi: " + TimeUnit.SECONDS.convert(times, TimeUnit.NANOSECONDS)
                + "s");
    }

    private List<String> getBlocksList() {
        List<String> blocksList = new ArrayList<>();
        for (Map.Entry<String, DataFile> map : files.entrySet()) {
            DataFile dataFile = map.getValue();
            for (Block block : dataFile.getBlockList()) {
                blocksList.add(block.getId());
            }
        }
        return blocksList;
    }

    private List<MovedBlock> readQueriesTimes(String node) {
        List<MovedBlock> movedList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader("data/hdfs_read_write/hdfs_read_" + node + ".log"));
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
            long duration = Long.parseLong(split[8].trim().substring(split[8].trim().indexOf(" ")).trim());
            String block = split[7].substring(split[7].lastIndexOf(":")+1);
            return new MovedBlock(src, dest, block, bytes, duration);
        }

    }

}

