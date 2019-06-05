package de.dbanalytics.spic.job;

import com.fasterxml.aalto.util.BufferRecycler;
import de.dbanalytics.spic.data.Person;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RscriptJob extends ShellCommandJob {

    private static final Logger logger = Logger.getLogger(RscriptJob.class);

    public static final String RSCRIPTCOMMAND = "rscriptCommand";

    public static final String RSCRIPTFIlE = "rscriptFile";

    public static final String ARGUMENTS = "arguments.argument";

    private String rscriptCommand;

    private String rscriptFile;

    private List<String> arguments;

    @Override
    public void configure(HierarchicalConfiguration config) {
        rscriptCommand = config.getString(RSCRIPTCOMMAND, "Rscript");
        rscriptFile = config.getString(RSCRIPTFIlE);

        List<String> tmpArguments = config.getList(String.class, ARGUMENTS);
        if(tmpArguments == null) tmpArguments = new ArrayList<>(0);
        setArguments(tmpArguments);
    }

    protected void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    protected void setRscriptFile(String filename) {
        this.rscriptFile = filename;
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {
        InputStream stream = ClassLoader.getSystemResourceAsStream(rscriptFile);
        if(stream == null) {
            logger.warn(String.format("Rscript %s not found in JAR file.", rscriptFile));
            return null;
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        if(tempDir == null) {
            logger.warn("System property \"java.io.tmpdir\" not set.");
            return null;
        }

        String newRscripFile = null;
        try {
            logger.info(String.format("Copying %s to %s.", rscriptFile, tempDir));
            newRscripFile = String.format("%s/%s", tempDir, rscriptFile);
            Files.copy(stream, Paths.get(String.format("%s/%s", tempDir, rscriptFile)), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(rscriptCommand);
        builder.append(" ");
        builder.append(newRscripFile);
        for(String arg : arguments) {
            builder.append(" ");
            builder.append(arg);
        }
        setCommand(builder.toString());

        return super.execute(population);
    }
}
