package cooccurrence;

import model.Query;
import model.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PreCoOccurrenceMatrix {

    private String[][] matrix;
    private final List<Query> queries;

    public PreCoOccurrenceMatrix(List<Query> queries) {
        this.queries = queries;
    }

    public String[][] getMatrix() {
        fillMatrix();
        return matrix;
    }

    private void fillMatrix() {
        List<Table> tableList = getTables();
        HashMap<Integer, Query> queryMap = getQueriesMap();
        matrix = new String[queryMap.size()+1][tableList.size()+1];
        setColumnsName(tableList);
        setMatrixValues(queryMap, readQueriesTimes());
        fillZero();
    }

    private void setColumnsName(List<Table> tableList) {
        int i = 1;
        matrix[0][0] = "query/tables";
        for (Table table : tableList) {
            matrix[0][i++] = table.getName();
        }
    }

    private void setMatrixValues(HashMap<Integer, Query> queryMap, int[] qNums) {
        int j = 1;
        List<String> matrix0 = Arrays.asList(matrix[0]);
        for (Map.Entry<Integer, Query> map : queryMap.entrySet()) {
            matrix[j][0] = String.valueOf(map.getKey());
            List<Table> queryTables = map.getValue().getTables();
            for (Table table : queryTables) {
                matrix[j][matrix0.indexOf(table.getName())] = String.valueOf(qNums[j-1]);
            }
            j++;
        }
    }

    private void fillZero() {
        for (int i = 1; i < matrix.length; i++) {
            for (int j = 1; j < matrix[i].length; j++) {
                if (matrix[i][j] == null) {
                    matrix[i][j] = String.valueOf(0);
                }
            }
        }
    }

    private HashMap<Integer, Query> getQueriesMap() {
        HashMap<Integer, Query> queryMap = new HashMap<>();
        int i = 1;
        for (Query query : queries) {
            if (!queryMap.containsValue(query)) {
                queryMap.put(i++, query);
            }
        }
        return queryMap;
    }

    // get the tables used in the executed queries
    private List<Table> getTables() {
        List<Table> tableList = new ArrayList<>();
        List<String> tableCheck = new ArrayList<>();
        for (Query query : queries) {
            List<Table> queryTable = query.getTables();
            for (Table table : queryTable) {
                if (!tableCheck.contains(table.getName())) {
                    tableCheck.add(table.getName());
                    tableList.add(table);
                }
            }
        }
        return tableList;
    }

    public String toString() {
        if (matrix == null) {
          return "Fill Matrix!";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] strings : matrix) {
            for (String string : strings) {
                stringBuilder.append(string).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    // reads execute.sh to extract the number of time that a query is executed
    private int[] readQueriesTimes() {
        int[] numbers = new int[matrix.length];
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("data/execute.sh"));
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("query/query-")) {
                    numbers[i++] = Integer.parseInt(line.substring(line.indexOf("..")+2, line.indexOf("}")));
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numbers;
    }

}
