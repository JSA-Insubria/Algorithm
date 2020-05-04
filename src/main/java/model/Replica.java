package model;

public class Replica {

    private String hostName;
    private String storageId;
    private String storageType;

    public Replica() {}

    public Replica(String hostName, String storageId, String storageType) {
        this.hostName = hostName;
        this.storageId = storageId;
        this.storageType = storageType;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String toString() {
        return "Replica hostName: " + hostName
                + " StorageId: " + storageId
                + " storageType: " + storageType;
    }
}
