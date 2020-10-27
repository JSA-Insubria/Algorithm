package problemsolver;

import model.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

public class ConstraintsProblemSolverPareto {

    private final List<Node> nodeList;
    private final Map<String, DataFile> filesMap;
    private final String[][] coOccurrenceMatrix;
    private final Map<String, Table> tableMap;

    private int numNodes;
    private int[] nodesCapacity;

    private int numItems;
    private int[] blocksSize;

    private int replica_factor;

    private LinkedList<String> files; //Elenco file
    private LinkedList<Integer> nBlocks; //Elenco numero blocchi della lista file sopra, stesso ordine!

    private IntVar[][] x;
    private IntVar[] z; //Pareto Variable

    public ConstraintsProblemSolverPareto(List<Node> nodeList, Map<String, DataFile> filesMap,
                                          String[][] coOccurrenceMatrix, Map<String, Table> tableMap) {
        this.nodeList = nodeList;
        this.filesMap = filesMap;
        this.coOccurrenceMatrix = coOccurrenceMatrix;
        this.tableMap = tableMap;
    }

    public void findOptimalSolutions() {
        retrieveData();

        Model model = setModel();
        Solver solver = model.getSolver();
        setSolverTimeLimit(solver);

        //Solve
        List<Solution> solutions = solver.findParetoFront(z, Model.MAXIMIZE);

        //Print Solutions
        PrettyPrint prettyPrint = new PrettyPrint(nodeList, nodesCapacity, files, nBlocks, blocksSize, x, z);
        prettyPrint.print(solutions);

        solver.printStatistics();
    }

    //Get nodes and files information
    private void retrieveData() {
        getNodesInformation();
        getFilesInformation();
        getReplicationFactor();
    }

    private void setSolverTimeLimit(Solver solverTimeLimit) {
        solverTimeLimit.limitTime("30s");
    }

    private Model setModel() {
        Model model = new Model();
        x = model.boolVarMatrix(numItems, numNodes);
        insertConstraints(model);
        return model;
    }

    private void insertConstraints(Model model) {
        setReplicaConstraints(model);

        //get weight of blocks
        ComputeBlocksWeight computeBlocksWeight = new ComputeBlocksWeight(files, nBlocks, coOccurrenceMatrix, tableMap);
        int[] weight = computeBlocksWeight.getBlocksWeight();

        setZBounds(model, computeBlocksWeight.getWeightMin(), computeBlocksWeight.getWeightMax());
        setNodeConstraints(model, weight);
    }

    // sum per row must be equals or greater than replica factor
    private void setReplicaConstraints(Model model) {
        int nFile = 0;
        for (int i = 0; i < numItems;) {
            int num = nBlocks.get(nFile++);
            if (num > 1) {
                // sumXBlock is the same for all the blocks of the same file
                IntVar sumXBlock = model.intVar(replica_factor, numNodes);
                for (int j = 0; j < num; j++) {
                    model.sum(x[i++], "=", sumXBlock).post();
                }
            } else {
                model.sum(x[i++], ">=", replica_factor).post();
            }
        }
    }

    //set lowerbound (smaller block size) and upperbound (sum of all weight of blocks) of z
    private void setZBounds(Model model, int weightMin, int weightMax) {
        z = model.intVarArray(numNodes, weightMin, weightMax, true);
    }

    private void setNodeConstraints(Model model, int[] weight) {
        IntVar[] sumXNode = model.intVarArray(numNodes, 1, IntVar.MAX_INT_BOUND);
        for (int i = 0; i < numNodes; i++) {
            IntVar[] items = new IntVar[numItems];
            for (int j = 0; j < numItems; j++) {
                items[j] = x[j][i];
            }
            //value * block size, must be less than the node capacity
            model.scalar(items, blocksSize, "<=", nodesCapacity[i]).post();
            //value * block size, must be equal to sumXNode
            model.scalar(items, blocksSize, "=", sumXNode[i]).post();
            model.scalar(items, weight, "=", z[i]).post();
        }
        //sumXNode of each node should be equal (attempt to load balance)
        //model.atMostNValues(sumXNode, model.intVar(1, numNodes), false).post();
        model.atMostNValues(sumXNode, model.intVar(numNodes), false).post();
    }

    // get nodes capacity in MB
    private void getNodesInformation() {
        nodesCapacity = new int[nodeList.size()];
        int i = 0;
        for (Node node : nodeList) {
            nodesCapacity[i++] = (int) ((double) node.getCapacity()/(1042^2)) +1;
        }
        numNodes = i;
    }

    //get files information and blocks size in MB
    private void getFilesInformation() {
        files = new LinkedList<>();
        nBlocks = new LinkedList<>();
        List<Integer> filesList = new ArrayList<>();
        for (Map.Entry<String, DataFile> map : filesMap.entrySet()) {
            DataFile file = map.getValue();
            files.add(file.getName());
            nBlocks.add(file.getBlockList().size());
            for (Block block : file.getBlockList()) {
                filesList.add((int) ((double) block.getLength()/(1042^2)) +1);
            }
        }
        numItems = filesList.size();
        blocksSize = new int[filesList.size()];
        blocksSize = filesList.stream().mapToInt(i -> i).toArray();
    }

    private void getReplicationFactor() {
        Map.Entry<String,DataFile> entry = filesMap.entrySet().iterator().next();
        replica_factor = entry.getValue().getBlockList().get(0).getReplicaList().size();
    }
}