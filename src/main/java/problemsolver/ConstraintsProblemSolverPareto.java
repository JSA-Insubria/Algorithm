package problemsolver;

import cooccurrence.CoOccurrenceMatrix;
import model.Block;
import model.DataFile;
import model.Node;
import model.Table;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class ConstraintsProblemSolverPareto {

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
    private IntVar[] z;

    public ConstraintsProblemSolverPareto(List<Node> nodeList, Map<String, DataFile> filesMap,
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

        x = model.boolVarMatrix(numItems, numNodes);
        insertConstraints(model);

        ParetoOptimizer paretoOptimizer = new ParetoOptimizer(Model.MAXIMIZE, z);
        Solver solver = model.getSolver();
        solver.plugMonitor(paretoOptimizer);

        solver.limitTime("3h");

        while (solver.solve());
        List<Solution> solutions = paretoOptimizer.getParetoFront();

        String firstLine = "";
        for (int n = 0; n < numNodes; n++) {
            if (n == numNodes-1) {
                firstLine = firstLine.concat(nodeList.get(n).getHostName());
            } else {
                firstLine = firstLine.concat(nodeList.get(n).getHostName() + ",");
            }
        }
        printIntoFile("var.txt", firstLine);

        int n_solution = 1;
        for (Solution solution : solutions) {
            String print = "";
            for (int i = 0; i < numNodes; i++) {
                if (i == z.length-1) {
                    print = print.concat("" + solution.getIntVal(z[i]));
                } else {
                    print = print.concat(solution.getIntVal(z[i]) + ",");
                }
            }
            printIntoFile("var.txt", print);
            printIntoFile("solutions.txt", "\n" + print);
            prettyPrint(solution, n_solution++);
        }
        solver.printStatistics();
    }

    private void insertConstraints(Model model) {
        String[][] matrix = coOccurrenceMatrix.getMatrix();
        List<FilePosLen> filesIndexToMove = getValue(matrix);

        Map<Integer, Integer> positionMap = positionToMap(filesIndexToMove);
        int[] weight = new int[positionMap.size()];
        int weightMax = 0, weightMin = Integer.MAX_VALUE;
        for (int m = 0; m < positionMap.size(); m++) {
            weight[m] = positionMap.get(m);
            System.out.println(weight[m]);
            weightMax += weight[m];
            if (weight[m] < weightMin) {
                weightMin = weight[m];
            }
        }

        int nFile = 0;
        for (int i = 0; i < numItems;) {
            int num = nBlocks.get(nFile++);
            if (num > 1) {
                IntVar sumXBlock = model.intVar(replica_factor, numNodes);
                for (int j = 0; j < num; j++) {
                    model.sum(x[i++], "=", sumXBlock).post();
                }
            } else {
                model.sum(x[i++], ">=", replica_factor).post();
            }
        }

        IntVar[] sumXNode = model.intVarArray(numNodes, 1, IntVar.MAX_INT_BOUND);
        z = model.intVarArray(numNodes, weightMin, weightMax, true);
        for (int i = 0; i < numNodes; i++) {
            IntVar[] items = new IntVar[numItems];
            for (int j = 0; j < numItems; j++) {
                items[j] = x[j][i];
            }
            model.scalar(items, replicasSize, "<=", nodesCapacity[i]).post();
            model.scalar(items, replicasSize, "=", sumXNode[i]).post();
            model.scalar(items, weight, "=", z[i]).post();
        }
        //model.allEqual(sumXNode).post();
        //IntVar capNNode = model.intVar(1, numNodes);
        model.atMostNValues(sumXNode, model.intVar(1, numNodes), false).post();
        //model.setObjective(true, capNNode);
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
            for (int i = 1; i < matrix.length; i++) {
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

    private void printIntoFile(String file, String line) {
        //System.out.print(line);
        String path = "/home/simone/Documenti/Università/Tesi/Algoritmo/files";
        File fileName = new File(path + File.separator + file);
        try {
            FileWriter myWriter = new FileWriter(fileName, true);
            myWriter.write(line + "\n");
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prettyPrint(Solution solution, int n_solution) {
        StringBuilder hadoop_sol = new StringBuilder();
        String print = "\n\t";
        for (int n = 0; n < numNodes; n++) {
            print = print.concat(nodeList.get(n).getHostName() + "(" + nodesCapacity[n] + ")" + "\t");
        }
        print = print.concat("\n");
        int nfile = 0, block = 1;
        for (int i = 0; i < numItems; i++) {
            print = print.concat("T_" + (nfile+1) + "_" + block++ + ":" + "\t");
            hadoop_sol.append(files.get(nfile)).append(",");
            for (int j = 0; j < numNodes; j++) {
                int val = solution.getIntVal(x[i][j]);
                print = print.concat(val + "\t\t");
                if (val == 1) {
                    hadoop_sol.append(nodeList.get(j).getName()).append(",");
                }
                if (j+1 == numNodes) {
                    hadoop_sol.replace(hadoop_sol.length()-1, hadoop_sol.length(), "");
                    hadoop_sol.append("\n");
                }
            }
            print = print.concat(" - " + files.get(nfile) + " blk: " + (block-1) + " - " + replicasSize[i] + "\n");
            if (block > nBlocks.get(nfile)) {
                nfile++;
                block = 1;
            }
        }

        int[] sumXNode = new int[numNodes];
        for (int j = 0; j < numNodes; j++) {
            for (int i = 0; i < numItems; i++) {
                if (solution.getIntVal(x[i][j]) == 1) {
                    sumXNode[j] += replicasSize[i];
                }
            }
        }

        print = print.concat("sum:" + "\t");
        for (int n = 0; n < numNodes; n++) {
            print = print.concat(sumXNode[n] + "\t\t");
        }
        printIntoFile("solutions.txt", print);
        printIntoFile("FilesLocation_" + n_solution + ".txt", hadoop_sol.toString());
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

        public int[] getFileLen() {
            return fileLen;
        }

        public int getWeight() {
            return weight;
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
