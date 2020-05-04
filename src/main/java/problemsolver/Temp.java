package problemsolver;

import cooccurrence.CoOccurrenceMatrix;
import model.Block;
import model.DataFile;
import model.Node;
import model.Table;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import java.util.*;

public class Temp {
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

    public Temp(List<Node> nodeList, Map<String, DataFile> filesMap,
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

        //x = model.intVarMatrix(numItems, numNodes, 0, 1);
        x = new IntVar[numItems][numNodes];
        for (int i = 0; i < numItems; i++) {
            for (int j = 0; j < numNodes; j++) {
                x[i][j] = model.intVar(0,1);
            }
        }
        checkReplicasContraint(model);
        checkNodesCapacityContraint(model);

        Solver solver = model.getSolver();
        function(model);
        solver.findSolution();
        prettyPrint();

    }


    private void function(Model model) {
        int[] sum = new int[numNodes];
        for (int j = 0; j < numNodes; j++) {
            for (int i = 0; i < numItems; i++) {
                if (x[i][j].getValue() == 1) {
                    sum[j] += getCoOccurrenceValue(i);
                }
            }
        }
        IntVar[] sumVar = model.intVarArray(sum.length, sum);
        for (IntVar intVar : sumVar) {
            model.setObjective(true, intVar);
        }
    }

    private int getCoOccurrenceValue(int i) {
        String[][] matrix = coOccurrenceMatrix.getMatrix();

        return 0;
    }

    private void getFileNameFromIndex(int i) {




        for (Map.Entry<String, DataFile> map : filesMap.entrySet()) {
            DataFile dataFile = map.getValue();
        }
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

}
