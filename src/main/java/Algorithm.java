import cooccurrence.CoOccurrenceMatrix;
import cooccurrence.PreCoOccurrenceMatrix;
import fill.FillFiles;
import fill.FillNodes;
import fill.FillQueries;
import model.*;
import problemsolver.ConstraintsProblemSolverPareto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class Algorithm {

    private static final String path = "data" + File.separator;

    public static void main(String[] args) {

        createSolutionsFolder();

        FillNodes fillNodes = new FillNodes(path);
        FillFiles fillFiles = new FillFiles(path);

        LinkedHashMap<String, DataFile> files = fillFiles.readFiles();

        FillQueries fillQueries = new FillQueries(path, files);

        List<Node> nodeList = fillNodes.readNodes();
        List<Query> queryList = fillQueries.readQueries();

        PreCoOccurrenceMatrix preCoOccurrenceMatrix = new PreCoOccurrenceMatrix(queryList);
        CoOccurrenceMatrix coOccurrenceMatrix = new CoOccurrenceMatrix(preCoOccurrenceMatrix.getMatrix());
        String[][] matrix = coOccurrenceMatrix.getMatrix();

        System.out.println(preCoOccurrenceMatrix.toString());
        System.out.println(coOccurrenceMatrix.toString());

        ConstraintsProblemSolverPareto constraintsProblemSolverPareto = new ConstraintsProblemSolverPareto(nodeList,
                files, matrix, fillQueries.getTableList());
        constraintsProblemSolverPareto.findOptimalSolutions();

    }

    private static void createSolutionsFolder() {
        File solutionsPath = new File("data" + File.separator + "/solutions");
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
