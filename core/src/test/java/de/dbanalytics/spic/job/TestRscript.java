package de.dbanalytics.spic.job;

import de.dbanalytics.spic.util.TestCaseUtils;
import junit.framework.TestCase;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;

public class TestRscript extends TestCase {

    public void testCars() throws IOException {
        String referenceDir = "src/test/resources/";
        String tmpDir = "src/test/tmp/";

        new File(tmpDir).mkdirs();
        TestCaseUtils.deleteDir(tmpDir);

        try {
            JobExecutor.main(new String[]{"src/test/resources/config.rscript.xml"});
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        TestCaseUtils.compareFiles(referenceDir + "cars.csv", tmpDir + "cars.csv");

    }
}
