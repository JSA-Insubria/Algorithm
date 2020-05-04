package model;

import java.util.List;

public class Table {

    private String name;
    private List<DataFile> files;

    public Table() {}

    public Table(String name, List<DataFile> files) {
        this.name = name;
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataFile> getFiles() {
        return files;
    }

    public void setFiles(List<DataFile> files) {
        this.files = files;
    }

    public String toString() {
        return "Table name: " + name;
    }
}
