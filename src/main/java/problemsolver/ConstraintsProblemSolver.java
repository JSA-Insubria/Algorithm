package problemsolver;

import com.sun.org.apache.xpath.internal.operations.Mod;
import cooccurrence.CoOccurrenceMatrix;
import model.Block;
import model.DataFile;
import model.Node;
import model.Table;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.variables.IntVar;
import org.omg.CORBA.INTERNAL;

import javax.jws.WebParam;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConstraintsProblemSolver {

    private final List<Node> nodeList;
    private final Map<String, DataFile> filesMap;
    private final CoOccurrenceMatrix coOccurrenceMatrix;
    private final Map<String, Table> tableMap;

    private int numNodes;
    private int[] nodesCapacity;

    private int numItems;
    private int[] replicasSize;

    private int replica_factor;

    private LinkedList<String> files; //Elenco file
    private LinkedList<Integer> nBlocks; //Elenco numero blocchi della lista file sopra, stesso ordine!

    private IntVar[][] x;

    public ConstraintsProblemSolver(List<Node> nodeList, Map<String, DataFile> filesMap,
                                    CoOccurrenceMatrix coOccurrenceMatrix, Map<String, Table> tableMap) {
        this.nodeList = nodeList;
        this.filesMap = filesMap;
        this.coOccurrenceMatrix = coOccurrenceMatrix;
        this.tableMap = tableMap;
    }

    public void init() {
        Model model = new Model();

        getNodesInformation();
        getFilesInformation();

        x = new IntVar[numItems][numNodes];
        for (int i = 0; i < numItems; i++) {
            for (int j = 0; j < numNodes; j++) {
                x[i][j] = model.intVar(0,1);
            }
        }
        checkReplicasContraint(model);
        checkNodesCapacityContraint(model);

        IntVar[] paretoVar = insertCoOccurrenceMatrix(model);
        List<Solution> solutions = model.getSolver().findParetoFront(paretoVar, Model.MAXIMIZE);
        for (Solution solution : solutions) {
            System.out.println("-----------------------------------");
            System.out.println(solution.toString());
        }

        /*
        testSingleSum(model);
        while (model.getSolver().solve()) {
            prettyPrint();
        }
        */
    }

    private void checkReplicasContraint(Model model) {
        //Per ogni i,k, sum(X_i_j_k) = replica_factor ->
        // soddisfo sia il fatto di avere tutte le repliche associate,
        // sia il fatto che tutte le repliche di un blocco sono associate

        for (int i = 0; i < numItems; i++) {
            IntVar[] sum = new IntVar[numNodes];
            if (numNodes >= 0) System.arraycopy(x[i], 0, sum, 0, numNodes);
            model.sum(sum, ">=", replica_factor).post();
            model.sum(sum, "<=", numNodes).post();
        }
    }

    private void checkNodesCapacityContraint(Model model) {
        //La somma di tutti i blocchi presenti in un nodo deve essere < della sua capacità
        for (int i = 0; i < numNodes; i++) {
            IntVar[] sum = new IntVar[numItems];
            int[] capacity = new int[numItems];
            int c = 0;
            for (int j = 0; j < numItems; j++) {
                sum[c] = x[j][i];
                capacity[c] = replicasSize[j];
                c++;
            }
            model.scalar(sum, capacity, "<=", nodesCapacity[i]).post();
        }
    }

    private IntVar[] insertCoOccurrenceMatrix(Model model) {
        String[][] matrix = coOccurrenceMatrix.getMatrix();
        List<FilePosLen> filesIndexToMove = getValue(matrix);

        Map<Integer, Integer> positionMap = positionToMap(filesIndexToMove);

        IntVar[] paretoVar = new IntVar[numNodes];
        for (int i = 0; i < numNodes; i++) {
            int sum = 0;
            IntVar[] items = new IntVar[numItems];
            int[] values = new int[numItems];
            int c = 0;
            for (int j = 0; j < numItems; j++) {
                items[c] = x[j][i];
                values[c] = positionMap.get(j);
                sum += positionMap.get(j);
                c++;
            }
            IntVar sumXNodeVar = model.intVar(0, sum);
            model.scalar(items, values, "<=", sumXNodeVar).post();
            paretoVar[i] = sumXNodeVar;
        }
        return paretoVar;
    }

    private void testSingleSum(Model model) {
        String[][] matrix = coOccurrenceMatrix.getMatrix();
        List<FilePosLen> filesIndexToMove = getValue(matrix);

        Map<Integer, Integer> positionMap = positionToMap(filesIndexToMove);

        int sum = 0;
        IntVar[] items = new IntVar[numItems*numNodes];
        int[] values = new int[numItems*numNodes];
        int c = 0;
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numItems; j++) {
                items[c] = x[j][i];
                values[c] = positionMap.get(j);
                sum += positionMap.get(j);
                c++;
            }
        }
        IntVar sumXNodeVar = model.intVar(0, sum);
        model.scalar(items, values, "<=", sumXNodeVar).post();
        model.setObjective(true, sumXNodeVar);
    }

    private Map<Integer, Integer> positionToMap(List<FilePosLen> filesIndexToMove) {
        Map<Integer, Integer> map = new HashMap<>();
        for (FilePosLen filePosLen : filesIndexToMove) {
            int[] position = filePosLen.getPosition();
            int[] fileLen = filePosLen.getFileLen();
            for (int i = 0; i < position.length; i++) {
                for (int j = 0; j < fileLen[i]; j++) {
                    int pos = position[i] + j;
                    if (map.containsKey(pos)) {
                        int weight = map.get(pos) + filePosLen.weight;
                        map.replace(pos, weight);
                    } else {
                        map.put(pos, filePosLen.weight);
                    }
                }
            }
        }
        return map;
    }

    private List<FilePosLen> getValue(String[][] matrix) {
        if (matrix.length <= 1) {
            System.err.println("No Sufficient Data!");
            System.exit(1);
        } else {
            List<FilePosLen> filePosLenList = new ArrayList<>();
            for (int i = 1; i < matrix.length; i++ ) {
                for (int j = i+1; j < matrix[i].length; j++) {
                    filePosLenList.add(FilePosLen.merge(getFileIndex(matrix[i][0], Integer.parseInt(matrix[i][j])),
                            getFileIndex(matrix[0][j], Integer.parseInt(matrix[i][j]))));
                    System.out.println(matrix[i][0] + " -> " + matrix[0][j] + " -> " +  matrix[i][j]);
                }
            }
            return filePosLenList;
        }
        return null;
    }

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

    private void getNodesInformation() {
        nodesCapacity = new int[nodeList.size()];
        int i = 0;
        for (Node node : nodeList) {
            //nodesCapacacity[i++] = node.getCapacity(); long
            nodesCapacity[i++] = (int) ((double) node.getCapacity()/(1042^2)) +1;
        }
        numNodes = i;
    }

    private void getFilesInformation() {
        files = new LinkedList<>();
        nBlocks = new LinkedList<>();
        List<Integer> filesList = new ArrayList<>();
        for (Map.Entry<String, DataFile> map : filesMap.entrySet()) {
            DataFile file = map.getValue();
            files.add(file.getName());
            nBlocks.add(file.getBlockList().size());
            for (Block block : file.getBlockList()) {
                replica_factor = block.getReplicaList().size();
                //filesList.add(block.getLength()); long
                filesList.add((int) ((double) block.getLength()/(1042^2)) +1);
            }
        }
        numItems = filesList.size();
        replicasSize = new int[filesList.size()];
        replicasSize = filesList.stream().mapToInt(i -> i).toArray();
    }

    private void prettyPrint() {
        System.out.print("\n\t\t");
        for (int n = 0; n < numNodes; n++) {
            System.out.print(nodeList.get(n).getHostName() + "(" + nodesCapacity[n] + ")" + "\t");
        }
        System.out.print("\n");
        int nfile = 0, block = 1;
        for (int i = 0; i < numItems; i++) {
            System.out.print("T_" + (nfile+1) + "_" + block++ + ": ");
            for (int j = 0; j < numNodes; j++) {
                System.out.print(x[i][j] + "\t\t");
            }
            System.out.print(" - " + files.get(nfile) + " blk: " + (block-1) + " - " + replicasSize[i]);
            System.out.print("\n");
            if (block > nBlocks.get(nfile)) {
                nfile++;
                block = 1;
            }
        }

        int[] sumXNode = new int[numNodes];
        for (int j = 0; j < numNodes; j++) {
            for (int i = 0; i < numItems; i++) {
                if (x[i][j].getValue() == 1) {
                    sumXNode[j] += replicasSize[i];
                }
            }
        }

        System.out.print("sum: " + "\t");
        for (int n = 0; n < numNodes; n++) {
            System.out.print(sumXNode[n] + "\t\t\t");
        }
    }


    //Support Class
    private static class FilePosLen {
        int[] position;
        int[] fileLen;
        int weight;

        public FilePosLen(int[] position, int[] fileLen, int weight) {
            this.position = position;
            this.fileLen = fileLen;
            this.weight = weight;
        }

        public int[] getPosition() {
            return position;
        }

        public void setPosition(int[] position) {
            this.position = position;
        }

        public int[] getFileLen() {
            return fileLen;
        }

        public void setFileLen(int[] fileLen) {
            this.fileLen = fileLen;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public static FilePosLen merge(FilePosLen filePosLen1, FilePosLen filePosLen2) {
            int[] pos1 = filePosLen1.getPosition();
            int[] pos2 = filePosLen2.getPosition();
            int[] len1 = filePosLen1.getFileLen();
            int[] len2 = filePosLen2.getFileLen();
            int[] pos = new int[pos1.length + pos2.length];
            int[] len = new int[len1.length + len2.length];
            System.arraycopy(pos1, 0, pos, 0, pos1.length);
            System.arraycopy(pos2, 0, pos, pos1.length, pos2.length);
            System.arraycopy(len1, 0, len, 0, len1.length);
            System.arraycopy(len2, 0, len, len1.length, len2.length);
            return new FilePosLen(pos, len, filePosLen1.getWeight());
        }
    }
}
