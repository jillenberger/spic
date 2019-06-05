package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

public class ShellCommandJob implements Job {

    private static final Logger logger = Logger.getLogger(ShellCommandJob.class);

    public static final String COMMAND = "command";

    private String command;

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public void configure(HierarchicalConfiguration config) {
        setCommand(config.getString(COMMAND));
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            logger.warn("Shell command execution currently not supported on windows systems.");
            return null;
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            Process process = processBuilder.start();


            logger.info(String.format("Begin stdout and stderr of %s:", command));

            BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String stdOutLine;
            String stdErrLine = null;
            while ((stdOutLine = stdOutReader.readLine()) != null || (stdErrLine = stdErrReader.readLine()) != null) {
                if(stdOutLine != null) System.out.println(stdOutLine);
                if(stdErrLine != null) System.err.println(stdErrLine);
            }
            logger.info("End stdout and stderr.");

            int exitVal = process.waitFor();
            if (exitVal != 0) {
                logger.warn(String.format("Shell command exited with value %s.", exitVal));
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return population;
    }
}
