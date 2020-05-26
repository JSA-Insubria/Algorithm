package fill;

import model.Node;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FillNodes {

    private final String path;

    public FillNodes(String path) {
        this.path = path;
    }

    public List<Node> readNodes() {
        List<Node> nodeList = new ArrayList<>();
        File directory = new File(path + "ClusterInfo");
        File[] folderFiles = directory.listFiles();
        if (folderFiles != null) {
            for (File file : folderFiles) {
                JSONParser jsonParser = new JSONParser();
                try {
                    Object object = jsonParser.parse(new FileReader(file));
                    JSONObject jsonObject = (JSONObject) object;
                    nodeList.add(getNodeData(jsonObject));
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return nodeList;
    }

    private Node getNodeData(JSONObject jsonObject) {
        return new Node(String.valueOf(jsonObject.get("ipAddr")),
                String.valueOf(jsonObject.get("hostName")),
                String.valueOf(jsonObject.get("name")),
                String.valueOf(jsonObject.get("datanodeUuid")),
                String.valueOf(jsonObject.get("networkLocation")),
                Long.parseLong(String.valueOf(jsonObject.get("capacity"))),
                Long.parseLong(String.valueOf(jsonObject.get("dfsUsed"))),
                Long.parseLong(String.valueOf(jsonObject.get("nonDfsUsed"))),
                Long.parseLong(String.valueOf(jsonObject.get("remaining"))));
    }

}
