package problemsolver;

import model.DataFile;
import model.FilePosLen;
import model.Table;

import java.util.*;

public class ComputeBlocksWeight {

    private final LinkedList<String> files;
    private final LinkedList<Integer> nBlocks;

    private final String[][] coOccurrenceMatrix;
    private final Map<String, Table> tableMap;

    private int weightMin = Integer.MAX_VALUE;
    private int weightMax = 0;

    public ComputeBlocksWeight(LinkedList<String> files, LinkedList<Integer> nBlocks, String[][] coOccurrenceMatrix,
                               Map<String, Table> tableMap) {
        this.files = files;
        this.nBlocks = nBlocks;
        this.coOccurrenceMatrix = coOccurrenceMatrix;
        this.tableMap = tableMap;
    }

    public int getWeightMin() {
        return weightMin;
    }

    public int getWeightMax() {
        return weightMax;
    }

    //return an array of blocks weight
    public int[] getBlocksWeight() {
        Map<Integer, Integer> positionMap = positionToMap();
        int[] weight = new int[positionMap.size()];
        for (int m = 0; m < positionMap.size(); m++) {
            weight[m] = positionMap.get(m);
            System.out.println(weight[m]);
            weightMax += weight[m];
            if (weight[m] < weightMin) {
                weightMin = weight[m];
            }
        }
        return weight;
    }

    // get map<position, weight> from Co-Occurrence Matrix
    private Map<Integer, Integer> positionToMap() {
        List<FilePosLen> filesIndexToMove = getCoOccurrenceValues();
        Map<Integer, Integer> map = new HashMap<>();
        for (FilePosLen filePosLen : filesIndexToMove) {
            int[] position = filePosLen.getPosition();
            int[] fileLen = filePosLen.getFileLen();
            for (int i = 0; i < position.length; i++) {
                for (int j = 0; j < fileLen[i]; j++) {
                    int pos = position[i] + j;
                    if (map.containsKey(pos)) {
                        int weight = map.get(pos) + filePosLen.getWeight();
                        map.replace(pos, weight);
                    } else {
                        map.put(pos, filePosLen.getWeight());
                    }
                }
            }
        }
        return map;
    }

    // get starting block index of file, numbers of blocks of a file and the weight of file's blocks
    private List<FilePosLen> getCoOccurrenceValues() {
        List<FilePosLen> filePosLenList = new ArrayList<>();
        for (int i = 1; i < coOccurrenceMatrix.length; i++) {
            for (int j = i+1; j < coOccurrenceMatrix[i].length; j++) {
                filePosLenList.add(FilePosLen.merge(getFileIndex(coOccurrenceMatrix[i][0], Integer.parseInt(coOccurrenceMatrix[i][j])),
                        getFileIndex(coOccurrenceMatrix[0][j], Integer.parseInt(coOccurrenceMatrix[i][j]))));
                System.out.println(coOccurrenceMatrix[i][0] + " -> " + coOccurrenceMatrix[0][j] + " -> " +  coOccurrenceMatrix[i][j]);
            }
        }
        return filePosLenList;
    }

    // compute the index of the first block of a file in a node and get FilePosLen object
    private FilePosLen getFileIndex(String tableName, int weight) {
        int q = 0;
        Table table = tableMap.get(tableName);
        int[] pos = new int[table.getFiles().size()];
        int[] len = new int[table.getFiles().size()];
        for (DataFile dataFile : table.getFiles()) {
            String fileName = dataFile.getName();
            int fileIndex = files.indexOf(fileName);
            int sum = 0;
            for (int i = 0; i < fileIndex; i++) {
                sum += nBlocks.get(i);
            }
            pos[q] = sum;
            len[q++] = dataFile.getBlockList().size();
        }
        return new FilePosLen(pos, len, weight);
    }

}
