package problemsolver;

import model.DataFile;
import problemsolver.model.FilePosLen;
import model.Table;

import java.util.*;
import java.util.stream.IntStream;

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

    //return an array of blocks' weight
    public int[] getBlocksWeight() {
        // get map <index, weight> for each block
        Map<Integer, Integer> positionMap = getMapFromCoOccurrenceMatrix();
        // converting the map into an ordered array of weights
        List<Integer> sortedKeys = new ArrayList<>(positionMap.keySet());
        int[] weight = new int[positionMap.size()];
        for (Integer integer : sortedKeys) {
            weight[integer] = positionMap.get(integer);
            System.out.println(weight[integer]);
        }
        weightMin = Arrays.stream(weight).min().orElse(Integer.MAX_VALUE);
        weightMax = Arrays.stream(weight).sum();
        return weight;
    }

    // get map<position of block, weight of block> from Co-Occurrence Matrix
    // assigns a weight to each block, given by the sum of the weights of the tables where the block is used
    private Map<Integer, Integer> getMapFromCoOccurrenceMatrix() {
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

    // get the index of the first block of a file, numbers of blocks and the weight
    private List<FilePosLen> getCoOccurrenceValues() {
        List<FilePosLen> filePosLenList = new ArrayList<>();
        for (int i = 1; i < coOccurrenceMatrix.length; i++) {
            for (int j = i+1; j < coOccurrenceMatrix[i].length; j++) {
                // merges the indexes of the table files on the column with the indexes of the table files on the row
                filePosLenList.add(FilePosLen.merge(getIndexFromTable(coOccurrenceMatrix[i][0], Integer.parseInt(coOccurrenceMatrix[i][j])),
                        getIndexFromTable(coOccurrenceMatrix[0][j], Integer.parseInt(coOccurrenceMatrix[i][j]))));
                System.out.println(coOccurrenceMatrix[i][0] + " -> " + coOccurrenceMatrix[0][j] + " -> " +  coOccurrenceMatrix[i][j]);
            }
        }
        return filePosLenList;
    }

    // computes the indexes of the table's files
    // returns a FilePosLenght object
    // with the weights to be assigned to the block at the "pos" index and for the next "len" blocks
    private FilePosLen getIndexFromTable(String tableName, int weight) {
        int q = 0;
        Table table = tableMap.get(tableName);
        int[] pos = new int[table.getFiles().size()];
        int[] len = new int[table.getFiles().size()];
        // in the case of which the table consists of multiple files
        for (DataFile dataFile : table.getFiles()) {
            pos[q] = getIndex(dataFile);
            len[q++] = dataFile.getBlockList().size();
        }
        return new FilePosLen(pos, len, weight);
    }

    private int getIndex(DataFile dataFile) {
        String fileName = dataFile.getName();
        int fileIndex = files.indexOf(fileName);
        int index = 0;
        for (int i = 0; i < fileIndex; i++) {
            index += nBlocks.get(i);
        }
        return index;
    }

}
