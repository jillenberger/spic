package de.dbanalytics.spic.util;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
}
