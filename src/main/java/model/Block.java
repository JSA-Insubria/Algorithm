package model;

import java.util.List;

public class Block {

    private String id;
    private long length;

    private List<Replica> replicaList;

    public Block() {}

    public Block(String id, long length, List<Replica> replicaList) {
        this.id = id;
        this.length = length;
        this.replicaList = replicaList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public List<Replica> getReplicaList() {
        return replicaList;
    }

    public void setReplicaList(List<Replica> replicaList) {
        this.replicaList = replicaList;
    }

    public String toString() {
        return "Block id: " + id
                + " length: " + length;
    }
}
