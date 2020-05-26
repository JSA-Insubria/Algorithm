package model;

public class MovedBlock {

    private String src;
    private String dest;
    private String block;
    private long bytes;
    private long duration;

    public MovedBlock() {};

    public MovedBlock(String src, String dest, String block, long bytes, long duration) {
        this.src = src;
        this.dest = dest;
        this.block = block;
        this.bytes = bytes;
        this.duration = duration;
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

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
