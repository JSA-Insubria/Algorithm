package fill;

import model.DataFile;
import model.Query;
import model.Table;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class FillQueries {

    private final String path;
    private final LinkedHashMap<String, DataFile> filesMap;
    private HashMap<String, Table> tableMap;

    public FillQueries(String path, LinkedHashMap<String, DataFile> filesMap) {
        this.path = path;
        this.filesMap = filesMap;
    }

    public HashMap<String, Table> getTableList() {
        return tableMap;
    }

    public List<Query> readQueries() {
        List<Query> queryList = new ArrayList<>();
        File directory = new File(path + "QueryDataBlocks");
        File[] folderFiles = directory.listFiles();
        if (folderFiles != null) {
            for (File file : folderFiles) {
                queryList.add(getQuery(file));
            }
        }
        fillTableList(queryList);
        return queryList;
    }

    private void fillTableList(List<Query> queryList) {
        tableMap = new HashMap<>();
        for (Query query : queryList) {
            for (Table table : query.getTables()) {
                tableMap.put(table.getName(), table);
            }
        }
    }

    private Query getQuery(File file) {
        JSONParser jsonParser = new JSONParser();
        Query query = new Query();
        try {
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;
            query.setQuery((String) jsonObject.get("query"));
            query.setTables(getTables((JSONArray) jsonObject.get("tableList")));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return query;
    }

    private List<Table> getTables(JSONArray jsonArray) {
        List<Table> tableList = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            tableList.add(new Table((String) jsonObject.get("tableName"),
                    getAssociationTableFiles((JSONArray) jsonObject.get("queryDataFileList"))));
        }
        return tableList;
    }

    private List<DataFile> getAssociationTableFiles(JSONArray jsonArray) {
        List<DataFile> dataFileList = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            String fileStatus = (String) jsonObject.get("file");
            String filePath = fileStatus.substring(0, fileStatus.indexOf(";"));
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            try {
                String name = filesMap.get(fileName).getName(); //To avoid null values
                dataFileList.add(filesMap.get(fileName));
            } catch (NullPointerException e) {
                System.out.println("Error: File Not Exists!");
                System.exit(1);
            }
        }
        return dataFileList;
    }

}
