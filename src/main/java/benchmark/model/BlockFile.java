package benchmark.model;

import model.Replica;

import java.util.List;

public class BlockFile {
    private String id;
    private String file;
    private List<Replica> replicaList;

    private String src;
    private String dest;

    private long bytes;
    private double times;

    public BlockFile() {}

    public BlockFile(String id, String file, List<Replica> replicaList) {
        this.id = id;
        this.file = file;
        this.replicaList = replicaList;
    }

    public BlockFile(String id, String file, List<Replica> replicaList, String src, String dest, long bytes, double times) {
        this.id = id;
        this.file = file;
        this.replicaList = replicaList;
        this.src = src;
        this.dest = dest;
        this.bytes = bytes;
        this.times = times;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<Replica> getReplicaList() {
        return replicaList;
    }

    public void setReplicaList(List<Replica> replicaList) {
        this.replicaList = replicaList;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public double getTimes() {
        return times;
    }

    public void setTimes(double times) {
        this.times = times;
    }

    public String toString() {
        return "ID: " + id +
                " - NAME: " + id +
                " - FILE: " + file +
                " - SRC: " + src +
                " - DEST: " + dest +
                " - BYTES: " + bytes +
                " - TIME: " + times;
    }

}