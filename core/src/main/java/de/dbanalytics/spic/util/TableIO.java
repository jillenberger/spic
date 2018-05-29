package de.dbanalytics.spic.util;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TableIO {

    public static Table<Integer, String, String> read(String filename, String separator) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();

            /** read header */
            String[] header = line.split(separator);
            List<String> colNames = Arrays.stream(header).collect(Collectors.toList());

            /** count rows */
            List<Integer> rowIndices = new LinkedList();
            int rowIndex = 0;
            while (reader.readLine() != null) {
                rowIndices.add(rowIndex);
                rowIndex++;
            }

            /** create table */
            ArrayTable<Integer, String, String> table = ArrayTable.create(rowIndices, colNames);

            reader = new BufferedReader(new FileReader(filename));
            reader.readLine();

            rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(separator);

                for (int colIndex = 0; colIndex < fields.length; colIndex++) {
                    table.put(rowIndex, colNames.get(colIndex), fields[colIndex]);
                }
                rowIndex++;
            }

            return table;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void write(Table<Integer, String, String> table, String filename, String separator) throws IOException {
        Set<Integer> rows = new TreeSet<>(table.rowKeySet());
        List<String> columns = new ArrayList<>(table.columnKeySet());

        if (columns.size() > 0) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            /** write header */
            writer.write(columns.get(0));
            for (int i = 1; i < columns.size(); i++) {
                writer.write(separator);
                writer.write(columns.get(i));
            }
            writer.newLine();
            /** write body */
            for (Integer row : rows) {
                writer.write(table.get(row, columns.get(0)));
                for (int i = 1; i < columns.size(); i++) {
                    writer.write(separator);
                    String val = table.get(row, columns.get(i));
                    if (val == null) val = "";
                    writer.write(val);
                }
                writer.newLine();
            }
            writer.close();
        }
    }
}
