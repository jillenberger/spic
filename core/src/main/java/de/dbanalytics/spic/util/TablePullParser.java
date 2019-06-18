package de.dbanalytics.spic.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablePullParser {

    public static final String DEFAULT_SEPARATOR = ";";

    private final BufferedReader reader;

    private final String separator;

    private String[] colNames;

    private String nextLine;

    public TablePullParser(String filename) throws IOException {
        this(filename, DEFAULT_SEPARATOR);
    }

    public TablePullParser(String filename, String separator) throws IOException {
        this.separator = separator;
        reader = new BufferedReader(new FileReader(filename));

        nextLine = reader.readLine();
        if (nextLine == null) {
            throw new RuntimeException("File is empty.");
        } else {
            colNames = nextLine.split(separator);
        }
    }

    public void lowerCaseColNames() {
        for (int i = 0; i < colNames.length; i++) {
            colNames[i] = colNames[i].toLowerCase();
        }
    }

    public String getSeparator() {
        return separator;
    }

    public String[] getColNames() {
        return colNames;
    }

    public boolean hasNext() {
        try {
            nextLine = reader.readLine();
            boolean result = nextLine != null;

            if (!result) {
                reader.close();
            }

            return result;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, String> nextLine() {
        String[] tokens = nextLine.split(separator, -1);

        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 0; i < colNames.length; i++) {
            fields.put(colNames[i], tokens[i]);
        }

        return fields;
    }
}
