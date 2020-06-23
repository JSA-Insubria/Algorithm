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

    private final int[] blocksSize;

    private final IntVar[][] x;
    private final IntVar[] z;

    public PrettyPrint(List<Node> nodeList, int[] nodesCapacity, LinkedList<String> files,
                       LinkedList<Integer> nBlocks, int[] blocksSize, IntVar[][] x, IntVar[] z) {
        this.nodeList = nodeList;
        this.nodesCapacity = nodesCapacity;
        this.files = files;
        this.nBlocks = nBlocks;
        this.blocksSize = blocksSize;
        this.x = x;
        this.z = z;
        numItems = blocksSize.length;
        numNodes = nodeList.size();
    }

    public void print(List<Solution> solutionList) {
        printFirstLineVariableValue();
        int n_solution = 1;
        for (Solution solution : solutionList) {
            printVariableValue(solution);
            printNewFileLocation(solution, n_solution++);
            printStructuredSolution(solution);
        }
    }

    // print node name in var.txt
    private void printFirstLineVariableValue() {
        String firstLine = "";
        for (int n = 0; n < numNodes; n++) {
            if (n == numNodes-1) {
                firstLine = firstLine.concat(nodeList.get(n).getHostName());
            } else {
                firstLine = firstLine.concat(nodeList.get(n).getHostName() + ",");
            }
        }
        printIntoFile("var.txt", firstLine);
    }

    //print values of z in var.txt and in solutions.txt
    private void printVariableValue(Solution solution) {
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
    }

    // print structured solution, for each block print 1 if it is present in node, 0 otherwise
    private void printStructuredSolution(Solution solution) {
        String printSolution = addNodesCapacity() +
                addAllBlocksValues(solution) +
                addBlocksSizeSum(solution);
        printIntoFile("solutions.txt", printSolution);
    }

    private String addNodesCapacity() {
        StringBuilder printSolution = new StringBuilder();
        printSolution.append("\n\t");
        for (int n = 0; n < numNodes; n++) {
            printSolution.append(nodeList.get(n).getHostName()).append("(").append(nodesCapacity[n])
                    .append(")").append("\t");
        }
        return printSolution.append("\n").toString();
    }

    private String addAllBlocksValues(Solution solution) {
        StringBuilder stringBuilder = new StringBuilder();
        int nfile = 0, block = 1;
        for (int i = 0; i < numItems; i++) {
            stringBuilder.append(addBlockValues(solution, nfile, block, i));
            if (++block > nBlocks.get(nfile)) {
                nfile++;
                block = 1;
            }
        }
        return stringBuilder.toString();
    }

    private String addBlockValues(Solution solution,int nfile, int block, int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("T_").append(nfile + 1).append("_").append(block).append(":").append("\t");
        for (int j = 0; j < numNodes; j++) {
            int val = solution.getIntVal(x[i][j]);
            stringBuilder.append(val).append("\t\t");
        }
        stringBuilder.append(" - ").append(files.get(nfile)).append(" blk: ").append(block).append(" - ")
                .append(blocksSize[i]).append("\n");
        return stringBuilder.toString();
    }

    private String addBlocksSizeSum(Solution solution) {
        StringBuilder stringBuilder = new StringBuilder();
        int[] sumXNode = getBlocksSumSizePerNode(solution);
        stringBuilder.append("sum:" + "\t");
        for (int n = 0; n < numNodes; n++) {
            stringBuilder.append(sumXNode[n]).append("\t\t");
        }
        return stringBuilder.toString();
    }

    // print the sum of blocks size only if the block is present in the node
    private int[] getBlocksSumSizePerNode(Solution solution) {
        int[] sumXNode = new int[numNodes];
        for (int j = 0; j < numNodes; j++) {
            for (int i = 0; i < numItems; i++) {
                if (solution.getIntVal(x[i][j]) == 1) {
                    sumXNode[j] += blocksSize[i];
                }
            }
        }
        return sumXNode;
    }

    // print solution ready for the tool to move hadoop blocks
    private void printNewFileLocation(Solution solution, int n_solution) {
        printIntoFile("FilesLocation_" + n_solution + ".txt", addBlocks(solution));
    }

    private String addBlocks(Solution solution) {
        StringBuilder stringBuilder = new StringBuilder();
        int nfile = 0, block = 1;
        for (int i = 0; i < numItems; i++) {
            stringBuilder.append(files.get(nfile)).append(",").append(addBlocksLocations(solution, i));
            if (++block > nBlocks.get(nfile)) {
                nfile++;
                block = 1;
            }
        }
        return stringBuilder.toString();
    }

    private String addBlocksLocations(Solution solution, int i) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < numNodes; j++) {
            int val = solution.getIntVal(x[i][j]);
            if (val == 1) {
                stringBuilder.append(nodeList.get(j).getName()).append(",");
            }
            if (j+1 == numNodes) {
                stringBuilder.replace(stringBuilder.length()-1, stringBuilder.length(), "");
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private void printIntoFile(String file, String line) {
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
