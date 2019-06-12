package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InterpreterScriptJob extends ShellCommandJob {

    private static final Logger logger = Logger.getLogger(InterpreterScriptJob.class);

    private static final String INTERPRETER = "interpreter";

    private static final String SCRIPTFIlE = "scriptFile";

    public static final String ARGUMENTS = "arguments.argument";

    private String interpreter;

    private String scriptFile;

    private List<String> arguments;

    @Override
    public void configure(HierarchicalConfiguration config) {
        setInterpreter(config.getString(INTERPRETER));
        setScriptFile(config.getString(SCRIPTFIlE));

        List<String> tmpArguments = config.getList(String.class, ARGUMENTS);
        if(tmpArguments == null) tmpArguments = new ArrayList<>(0);
        setArguments(tmpArguments);
    }

    protected void setInterpreter(String interpreter) {
        this.interpreter = interpreter;
    }

    protected void setArguments(List<String> args) {
        this.arguments = args;
    }

    protected void setScriptFile(String filename) {
        this.scriptFile = filename;
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {
        InputStream stream = ClassLoader.getSystemResourceAsStream(scriptFile);
        if(stream == null) {
            logger.warn(String.format("Script %s not found in JAR file.", scriptFile));
            return null;
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        if(tempDir == null) {
            logger.warn("System property \"java.io.tmpdir\" not set.");
            return null;
        }

        String newScripFile = null;
        try {
            logger.info(String.format("Copying %s to %s.", scriptFile, tempDir));
            newScripFile = String.format("%s/%s", tempDir, scriptFile);
            Files.copy(stream, Paths.get(String.format("%s/%s", tempDir, scriptFile)), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(interpreter);
        builder.append(" ");
        builder.append(newScripFile);
        if(arguments != null) {
            for(String arg : arguments) {
                builder.append(" ");
                builder.append(arg);
            }
        }
        setCommand(builder.toString());

        return super.execute(population);
    }
}
