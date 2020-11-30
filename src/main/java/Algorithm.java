import cooccurrence.CoOccurrenceMatrix;
import cooccurrence.PreCoOccurrenceMatrix;
import fill.FillFiles;
import fill.FillNodes;
import fill.FillQueries;
import model.*;
import problemsolver.ConstraintsProblemSolverPareto;

import java.awt.datatransfer.FlavorEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Algorithm {

    private static String path = "algorithm_data" + File.separator;

    public static void main(String[] args) {
        createSolutionsFolder();
        String root_path = path;
        String folder = args[0];
        path = path + folder + File.separator;

        LinkedHashMap<String, DataFile> files = new FillFiles(path).readFiles();
        List<Node> nodeList = new FillNodes(path).readNodes();

        FillQueries fillQueries = new FillQueries(path, files);
        List<Query> queryList = fillQueries.readQueries();

        PreCoOccurrenceMatrix preCoOccurrenceMatrix = new PreCoOccurrenceMatrix(queryList, root_path);
        CoOccurrenceMatrix coOccurrenceMatrix = new CoOccurrenceMatrix(preCoOccurrenceMatrix.getMatrix());
        String[][] matrix = coOccurrenceMatrix.getMatrix();

        ConstraintsProblemSolverPareto constraintsProblemSolverPareto = new ConstraintsProblemSolverPareto(nodeList,
                files, matrix, fillQueries.getTableList(), args[1], root_path);
        constraintsProblemSolverPareto.findOptimalSolutions();
    }

    private static void createSolutionsFolder() {
        File solutionsPath = new File(path + "solutions");
        try {
            if (solutionsPath.exists()) {
                Files.walk(solutionsPath.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
            solutionsPath.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
