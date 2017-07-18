package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.MarkovEngineListener;
import de.dbanalytics.spic.sim.data.CachedPerson;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;

/**
 * @author johannes
 */
public class ODDistributionDebugger implements MarkovEngineListener {

    private final String outputDirectory;

    private final String name;

    private final ODCalibrator delegate;

    private final long interval;
    private final DecimalFormat df;
    private long iteration;

    public ODDistributionDebugger(ODCalibrator delegate, String name, String outDir, long interval) {
        this.delegate = delegate;
        this.name = name;
        this.outputDirectory = outDir;
        this.interval = interval;
        df = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(340);

    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        if (iteration % interval == 0) {
            String dir = outputDirectory + "/" + df.format(iteration) + "/";
            new File(dir).mkdirs();
            String filename = dir + name + ".txt";
            try {
                delegate.debugDump(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        iteration++;
    }
}
