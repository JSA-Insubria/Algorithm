package benchmark.model;

public class QueryStats {

    private double cpuTimeSpent;
    private long hdfsRead;
    private long hdfsWrite;

    public QueryStats(double cpuTimeSpent, long hdfsRead, long hdfsWrite) {
        this.cpuTimeSpent = cpuTimeSpent;
        this.hdfsRead = hdfsRead;
        this.hdfsWrite = hdfsWrite;
    }

    public double getCpuTimeSpent() {
        return cpuTimeSpent;
    }

    public void setCpuTimeSpent(double cpuTimeSpent) {
        this.cpuTimeSpent = cpuTimeSpent;
    }

    public long getHdfsRead() {
        return hdfsRead;
    }

    public void setHdfsRead(long hdfsRead) {
        this.hdfsRead = hdfsRead;
    }

    public long getHdfsWrite() {
        return hdfsWrite;
    }

    public void setHdfsWrite(long hdfsWrite) {
        this.hdfsWrite = hdfsWrite;
    }
}
