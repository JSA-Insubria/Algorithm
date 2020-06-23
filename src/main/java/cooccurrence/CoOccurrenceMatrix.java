package cooccurrence;

import java.util.Arrays;
import java.util.List;

public class CoOccurrenceMatrix {

    private final String[][] preCoOccurrenceMatrix;
    private String[][] coOccurrenceMatrix;

    public CoOccurrenceMatrix(String[][] preCoOccurrenceMatrix) {
        this.preCoOccurrenceMatrix = preCoOccurrenceMatrix;
    }

    public String[][] getMatrix() {
        List<String> tableList = Arrays.asList(preCoOccurrenceMatrix[0]);
        coOccurrenceMatrix = new String[tableList.size()][tableList.size()];
        addColumnName(tableList);
        fillZero();
        generateCoOccurrenceMatrix();
        return coOccurrenceMatrix;
    }

    private void addColumnName(List<String> tableList) {
        coOccurrenceMatrix[0][0] = "query/tables";
        int f = 1;
        for (String table : tableList) {
            if (table.equals("query/tables")) {
                continue;
            }
            coOccurrenceMatrix[0][f] = table;
            coOccurrenceMatrix[f][0] = table;
            f++;
        }
    }

    private void fillZero() {
        for (int i = 1; i < coOccurrenceMatrix.length; i++) {
            for (int j = 1; j < coOccurrenceMatrix[i].length; j++) {
                coOccurrenceMatrix[i][j] = String.valueOf(0);
            }
        }
    }

    private void generateCoOccurrenceMatrix() {
        List<String> matrix = Arrays.asList(coOccurrenceMatrix[0]);
        for (int i = 1; i < preCoOccurrenceMatrix.length; i++) {
            for (int j = 1; j < preCoOccurrenceMatrix[i].length; j++) {
                if (!preCoOccurrenceMatrix[i][j].equals("0")) {
                    String tableName = preCoOccurrenceMatrix[0][j];
                    for (int q = 1; q < preCoOccurrenceMatrix[i].length; q++) {
                        if (q == j) continue;
                        if (!preCoOccurrenceMatrix[i][q].equals("0")) {
                            String thisTable = preCoOccurrenceMatrix[0][q];
                            String value = coOccurrenceMatrix[matrix.indexOf(thisTable)][matrix.indexOf(tableName)];
                            int newValue = Integer.parseInt(value) + Integer.parseInt(preCoOccurrenceMatrix[i][q]);
                            coOccurrenceMatrix[matrix.indexOf(thisTable)][matrix.indexOf(tableName)] = String.valueOf(newValue);
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        if (coOccurrenceMatrix == null) {
            return "Fill Matrix!";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] strings : coOccurrenceMatrix) {
            for (String string : strings) {
                stringBuilder.append(string).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

}
