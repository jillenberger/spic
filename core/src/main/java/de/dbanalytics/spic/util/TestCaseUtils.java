package de.dbanalytics.spic.util;

import junitx.framework.FileAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class TestCaseUtils {

    public static void compareFiles(String expected, String actual) throws IOException {
        boolean isGZexpected = expected.endsWith(".gz");
        if (isGZexpected) expected = unzip(expected);

        boolean isGZactual = actual.endsWith(".gz");
        if (isGZactual) actual = unzip(actual);

        FileAssert.assertEquals(new File(expected), new File(actual));

        if (isGZexpected) Files.delete(Paths.get(expected));
        if (isGZactual) Files.delete(Paths.get(actual));
    }

    public static String unzip(String filename) {
        try {
            Runtime.getRuntime().exec("gunzip -k " + filename).waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename.substring(0, filename.length() - 3);
    }

    public static void deleteDir(String filename) throws IOException {
        Files.list(Paths.get(filename)).forEach(path -> {
            try {
                if (Files.isDirectory(path)) {
                    deleteDir(path.toString());
                }

                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
