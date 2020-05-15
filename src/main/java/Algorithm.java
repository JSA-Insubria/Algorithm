import cooccurrence.CoOccurrenceMatrix;
import cooccurrence.PreCoOccurrenceMatrix;
import fill.FillFiles;
import fill.FillNodes;
import fill.FillQueries;
import model.*;
import problemsolver.ConstraintsProblemSolver;
import problemsolver.ConstraintsProblemSolverPareto;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

public class Algorithm {

    private static final String path = "data" + File.separator;

    public static void main(String[] args) {
        FillNodes fillNodes = new FillNodes(path);
        FillFiles fillFiles = new FillFiles(path);

        LinkedHashMap<String, DataFile> files = fillFiles.readFiles();

        FillQueries fillQueries = new FillQueries(path, files);

        List<Node> nodeList = fillNodes.readNodes();
        List<Query> queryList = fillQueries.readQueries();

        PreCoOccurrenceMatrix preCoOccurrenceMatrix = new PreCoOccurrenceMatrix(queryList);
        preCoOccurrenceMatrix.getPreCoOccurrenceMatrix();
        System.out.println(preCoOccurrenceMatrix);

        CoOccurrenceMatrix coOccurrenceMatrix = new CoOccurrenceMatrix(preCoOccurrenceMatrix.getMatrix());
        coOccurrenceMatrix.init();
        System.out.println(coOccurrenceMatrix.toString());


        if (true) {
            ConstraintsProblemSolverPareto constraintsProblemSolverPareto = new ConstraintsProblemSolverPareto(nodeList,
                    files, coOccurrenceMatrix, fillQueries.getTableList());
            constraintsProblemSolverPareto.init();
        } else {
            ConstraintsProblemSolver constraintsProblemSolver = new ConstraintsProblemSolver(nodeList, files,
                    coOccurrenceMatrix, fillQueries.getTableList());
            constraintsProblemSolver.init();
        }

    }

}
