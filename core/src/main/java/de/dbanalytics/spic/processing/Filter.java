package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by johannesillenberger on 11.12.17.
 */
public class Filter {

    private static final Logger logger = Logger.getLogger(Filter.class);

    private static boolean verbose = false;

    static public void setVerbose(boolean verbose) {
        Filter.verbose = verbose;
    }

    static public <P extends Person> Collection<P> episodes(Collection<P> persons, Predicate<Episode> predicate) {
        AtomicInteger cnt = new AtomicInteger();
        AtomicInteger rmv = new AtomicInteger();

        persons.parallelStream().forEach(person -> {
            List<Episode> remove = new ArrayList<>(person.getEpisodes().size());
            for (Episode e : person.getEpisodes()) {
                cnt.incrementAndGet();
                if (!predicate.test(e)) remove.add(e);
            }

            for (Episode e : remove) {
                rmv.incrementAndGet();
                person.removeEpisode(e);
            }
        });

        if (verbose && rmv.get() > 0) {
            logger.info(String.format(Locale.US, "Removed %s of %s episodes (%.1f %%).", rmv.get(), cnt.get(), rmv.get() / (double) cnt.get() * 100));
        }

        return persons;
    }

    static public <P extends Person> Collection<P> persons(Collection<P> persons, Predicate<P> predicate) {
        List<P> newPersons = new ArrayList<>(persons.size());

        for (P p : persons) {
            if (predicate.test(p)) newPersons.add(p);
        }

        if (verbose && persons.size() != newPersons.size()) {
            int oldSize = persons.size();
            int delta = oldSize - newPersons.size();
            logger.info(String.format("Removed %s of %s persons (%.2f %%).", delta, oldSize, delta / (double) oldSize * 100));
        }

        return newPersons;
    }
}
