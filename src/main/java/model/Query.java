package model;

import java.util.List;

public class Query {

    private String query;
    private List<Table> tables;

    public Query() {}

    public Query(String query, List<Table> tables) {
        this.query = query;
        this.tables = tables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public String toString() {
        return "Query query: " + query;
    }
}
