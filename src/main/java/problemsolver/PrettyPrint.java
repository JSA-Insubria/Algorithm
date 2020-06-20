package problemsolver;

import model.Node;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PrettyPrint {

    private final int numNodes;
    private final List<Node> nodeList;
    private final int[] nodesCapacity;

    private final int numItems;
    private final LinkedList<String> files;
    private final LinkedList<Integer> nBlocks;

    private final int[] replicasSizeArray;

    private final IntVar[][] x;
    private final IntVar[] z;

    public PrettyPrint(List<Node> nodeList, int[] nodesCapacity, LinkedList<String> files,
                       LinkedList<Integer> nBlocks, int[] replicasSizeArray, IntVar[][] x, IntVar[] z) {
        this.nodeList = nodeList;
        this.nodesCapacity = nodesCapacity;
        this.files = files;
        this.nBlocks = nBlocks;
        this.replicasSizeArray = replicasSizeArray;
        this.x = x;
        this.z = z;
        numItems = files.size();
        numNodes = nodeList.size();
    }

    public void printSolutions(List<Solution> solutionList) {
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
        for (Solution solution : solutionList) {
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
            prettyPrintFilesLocation(solution, n_solution++);
            prettyPrint(solution);
        }
    }

    private void prettyPrint(Solution solution) {
        String print = "\n\t";
        for (int n = 0; n < numNodes; n++) {
            print = print.concat(nodeList.get(n).getHostName() + "(" + nodesCapacity[n] + ")" + "\t");
        }
        print = print.concat("\n");
        int nfile = 0, block = 1;
        for (int i = 0; i < numItems; i++) {
            print = print.concat("T_" + (nfile+1) + "_" + block++ + ":" + "\t");
            for (int j = 0; j < numNodes; j++) {
                int val = solution.getIntVal(x[i][j]);
                print = print.concat(val + "\t\t");
            }
            print = print.concat(" - " + files.get(nfile) + " blk: " + (block-1) + " - " + replicasSizeArray[i] + "\n");
            if (block > nBlocks.get(nfile)) {
                nfile++;
                block = 1;
            }
        }

        int[] sumXNode = new int[numNodes];
        for (int j = 0; j < numNodes; j++) {
            for (int i = 0; i < numItems; i++) {
                if (solution.getIntVal(x[i][j]) == 1) {
                    sumXNode[j] += replicasSizeArray[i];
                }
            }
        }

        print = print.concat("sum:" + "\t");
        for (int n = 0; n < numNodes; n++) {
            print = print.concat(sumXNode[n] + "\t\t");
        }
        printIntoFile("solutions.txt", print);
    }

    private void prettyPrintFilesLocation(Solution solution, int n_solution) {
        StringBuilder hadoop_sol = new StringBuilder();
        int nfile = 0, block = 1;
        for (int i = 0; i < numItems; i++) {
            hadoop_sol.append(files.get(nfile)).append(",");
            for (int j = 0; j < numNodes; j++) {
                int val = solution.getIntVal(x[i][j]);
                if (val == 1) {
                    hadoop_sol.append(nodeList.get(j).getName()).append(",");
                }
                if (j+1 == numNodes) {
                    hadoop_sol.replace(hadoop_sol.length()-1, hadoop_sol.length(), "");
                    hadoop_sol.append("\n");
                }
            }
            if (block > nBlocks.get(nfile)) {
                nfile++;
                block = 1;
            }
        }
        printIntoFile("FilesLocation_" + n_solution + ".txt", hadoop_sol.toString());
    }

    private void printIntoFile(String file, String line) {
        //System.out.print(line);
        File path = new File("data" + File.separator + "/solutions");
        File fileName = new File(path + File.separator + file);
        try {
            FileWriter myWriter = new FileWriter(fileName, true);
            myWriter.write(line + "\n");
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
