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

    private static final String path = "data" + File.separator + "test" + File.separator;

    public static void main(String[] args) {
        createSolutionsFolder();

        LinkedHashMap<String, DataFile> files = new FillFiles(path).readFiles();
        List<Node> nodeList = new FillNodes(path).readNodes();

        FillQueries fillQueries = new FillQueries(path, files);
        List<Query> queryList = fillQueries.readQueries();

        PreCoOccurrenceMatrix preCoOccurrenceMatrix = new PreCoOccurrenceMatrix(queryList);
        CoOccurrenceMatrix coOccurrenceMatrix = new CoOccurrenceMatrix(preCoOccurrenceMatrix.getMatrix());
        String[][] matrix = coOccurrenceMatrix.getMatrix();

        ConstraintsProblemSolverPareto constraintsProblemSolverPareto = new ConstraintsProblemSolverPareto(nodeList,
                files, matrix, fillQueries.getTableList());
        constraintsProblemSolverPareto.findOptimalSolutions();
    }

    private static void createSolutionsFolder() {
        File solutionsPath = new File("data" + File.separator + "solutions");
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
