package model;

import java.util.List;

public class DataFile {

    private String name;
    private String path;
    private long size;

    private List<Block> blockList;

    public DataFile() {}

    public DataFile(String name, String path, long size, List<Block> blockList) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.blockList = blockList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }

    public String toString() {
        return "File name: " + name
                + " path: " + path
                + " size: " + size;
    }
}
