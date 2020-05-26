package model;

public class Node {

    private String ipAddr;
    private String hostName;
    private String name;
    private String uuid;
    private String networkLocation;
    private long capacity;
    private long dfsUsed;
    private long nonDfsUsed;
    private long remaining;

    public Node() {}

    public Node(String ipAddr, String hostName, String name, String uuid, String networkLocation, long capacity, long dfsUsed, long nonDfsUsed, long remaining) {
        this.ipAddr = ipAddr;
        this.hostName = hostName;
        this.name = name;
        this.uuid = uuid;
        this.networkLocation = networkLocation;
        this.capacity = capacity;
        this.dfsUsed = dfsUsed;
        this.nonDfsUsed = nonDfsUsed;
        this.remaining = remaining;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNetworkLocation() {
        return networkLocation;
    }

    public void setNetworkLocation(String networkLocation) {
        this.networkLocation = networkLocation;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getDfsUsed() {
        return dfsUsed;
    }

    public void setDfsUsed(long dfsUsed) {
        this.dfsUsed = dfsUsed;
    }

    public long getNonDfsUsed() {
        return nonDfsUsed;
    }

    public void setNonDfsUsed(long nonDfsUsed) {
        this.nonDfsUsed = nonDfsUsed;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public String toString() {
        return "ipAddr: " + ipAddr + "(" + hostName + ")" + "\n" +
                "Capacity: " + capacity + " Remaining: " + remaining + "\n" +
                "dfsUsed: " + dfsUsed + " nonDfsUsed: " + nonDfsUsed;
    }
}
