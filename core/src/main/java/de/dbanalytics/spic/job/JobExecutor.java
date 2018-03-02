package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.util.Executor;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class JobExecutor {

    private static final Logger logger = Logger.getLogger(JobExecutor.class);

    private static final String JOB_EXECUTOR_TAG = "jobExecutor";

    private static final String JOBS_TAG = "jobs.job";

    private static final String JOBS_DUMP_ATTR = "jobs.job[@dump]";

    private static final String OUTPUT_DIRECTORY_TAG = "outputDirectory";

    private List<JobWrapper> jobWrappers = new ArrayList<>();

    private String outputDirectory;

    public static void main(String args[]) throws ConfigurationException {
        /** Hide debug logging */
        Level level = Logger.getRootLogger().getLevel();
        Logger.getRootLogger().setLevel(Level.INFO);

        Configurations configs = new Configurations();
        XMLConfiguration config = configs.xml(args[0]);

        Logger.getRootLogger().setLevel(level);

        JobExecutor executor = new JobExecutor();
        executor.configure(config.configurationAt(JOB_EXECUTOR_TAG));
        executor.run();
    }

    public void configure(HierarchicalConfiguration config) {
        /** get job list */
        List<String> jobs = config.getList(String.class, JOBS_TAG);
        String[] dumps = config.getStringArray(JOBS_DUMP_ATTR);
        if (jobs.size() != dumps.length) {
            throw new RuntimeException("Missing \"dump\" attribute for jobs.");
        }

        /** other params */
        setOutputDirectory(config.getString(OUTPUT_DIRECTORY_TAG));

        /** instantiate and configure jobs */
        for (int i = 0; i < jobs.size(); i++) {
            try {
                /** create instance */
                Class<? extends Job> clazz = Class.forName(jobs.get(i)).asSubclass(Job.class);

                Constructor<? extends Job> ctor = clazz.getConstructor();
                Job job = ctor.newInstance();

                /** configure job */
                String jobName = job.getClass().getSimpleName();
                HierarchicalConfiguration jobConfig = null;
                try {
                    jobConfig = config.configurationAt(jobName);
                } catch (ConfigurationRuntimeException e) {

                }
                job.configure(jobConfig);

                /** wrap job */
                JobWrapper wrapper = new JobWrapper(job, Boolean.parseBoolean(dumps[i]));
                jobWrappers.add(wrapper);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOutputDirectory(String directory) {
        this.outputDirectory = directory;
    }

    public void run() {
        Collection<? extends Person> persons = null;
        for (JobWrapper wrapper : jobWrappers) {
            logger.info(String.format("*** Executing job %s ***", wrapper.getJob().getClass().getSimpleName()));
            persons = wrapper.getJob().execute(persons);
            logger.info("*** Done ***");

            if (wrapper.isDump()) {
                if (outputDirectory == null) {
                    logger.warn("No output directory set. Cannot dump persons.");
                } else {
                    String dir = String.format("%s/%s", outputDirectory, wrapper.getJob().getClass().getSimpleName());
                    try {
                        if (!Files.exists(Paths.get(dir))) Files.createDirectory(Paths.get(dir));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String file = String.format("%s/persons.xml.gz", dir);
                    PopulationIO.writeToXML(file, persons);
                }
            }
        }
        /** shutdown executor in case it was used */
        Executor.shutdown();
    }

    private static class JobWrapper {

        private final Job job;

        private final boolean dump;

        public JobWrapper(Job job, boolean dump) {
            this.job = job;
            this.dump = dump;
        }

        public Job getJob() {
            return job;
        }

        public boolean isDump() {
            return dump;
        }
    }
}
