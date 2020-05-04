package fill;

import model.Block;
import model.DataFile;
import model.Replica;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FillFiles {

    private final String path;

    public FillFiles(String path) {
        this.path = path;
    }

    public LinkedHashMap<String, DataFile> readFiles() {
        LinkedHashMap<String, DataFile> filesMap = new LinkedHashMap<>();
        File directory = new File(path + "FilesInfo");
        File[] folderFiles = directory.listFiles();
        if (folderFiles != null) {
            for (File file : folderFiles) {
                org.json.simple.parser.JSONParser jsonParser = new org.json.simple.parser.JSONParser();
                try {
                    Object object = jsonParser.parse(new FileReader(file));
                    JSONObject jsonObject = (JSONObject) object;
                    DataFile dataFile = getFileData(jsonObject);
                    filesMap.put(dataFile.getName() , dataFile);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return filesMap;
    }

    private DataFile getFileData(JSONObject jsonObject) {
        DataFile dataFile = new DataFile();

        String filePath = String.valueOf(jsonObject.get("filePath"));
        dataFile.setName(filePath.substring(filePath.lastIndexOf("/")+1));
        dataFile.setPath(filePath);
        dataFile.setSize(Long.parseLong(String.valueOf(jsonObject.get("fileSize"))));

        JSONArray jsonArray = (JSONArray) jsonObject.get("blockLocations");
        List<Block> blockList = new ArrayList<>();
        for (Object o : jsonArray) {
            blockList.add(getBlock((JSONObject) o));
        }
        dataFile.setBlockList(blockList);

        return dataFile;
    }

    private Block getBlock(JSONObject jsonObject) {
        Block block = new Block();
        block.setId(String.valueOf(jsonObject.get("blockId")));

        JSONObject blockObject = (JSONObject) jsonObject.get("blockLocation");

        block.setReplicaList(getReplicaList(blockObject));
        block.setLength(Long.parseLong(String.valueOf(blockObject.get("length"))));
        return block;
    }

    private List<Replica> getReplicaList(JSONObject jsonObject) {
        List<Replica> replicaList = new ArrayList<>();

        JSONArray hosts = (JSONArray) jsonObject.get("hosts");
        JSONArray storageIds = (JSONArray) jsonObject.get("storageIds");
        JSONArray storageTypes = (JSONArray) jsonObject.get("storageTypes");

        for (int i = 0; i < hosts.size(); i++) {
            replicaList.add(new Replica(String.valueOf(hosts.get(i)),
                    String.valueOf(storageIds.get(i)),
                    String.valueOf(storageTypes.get(i))));
        }

        return replicaList;
    }

}
