package de.dbanalytics.spic.data.io.spic2matsim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.processing.PersonsTask;
import de.dbanalytics.spic.processing.SegmentTask;

import java.util.Collection;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class LegModeValidator implements SegmentTask, EpisodeTask, PersonTask, PersonsTask {

    private static final String DEFAULT_LEG_MODE = "undefined";

    @Override
    public void apply(Segment segment) {
        String mode = segment.getAttribute(CommonKeys.LEG_MODE);
        if(mode == null) segment.setAttribute(CommonKeys.LEG_MODE, DEFAULT_LEG_MODE);
    }

    @Override
    public void apply(Episode episode) {
        for(Segment leg : episode.getLegs()) {
            apply(leg);
        }
    }

    @Override
    public void apply(Person person) {
        for(Episode episode : person.getEpisodes()) {
            apply(episode);
        }
    }

    @Override
    public void apply(Collection<? extends Person> persons) {
        for(Person person : persons) apply(person);
    }
}
