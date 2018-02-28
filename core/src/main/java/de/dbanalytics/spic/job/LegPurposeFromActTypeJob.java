package de.dbanalytics.spic.job;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.SegmentTask;
import de.dbanalytics.spic.processing.TaskRunner;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.util.Collection;

public class LegPurposeFromActTypeJob implements Job {

    @Override
    public void configure(HierarchicalConfiguration config) {

    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {
        TaskRunner.runLegTask(new LegPurposeFromActTypeTask(), population);
        return population;
    }

    private static class LegPurposeFromActTypeTask implements SegmentTask {

        @Override
        public void apply(Segment segment) {
            String type = segment.next().getAttribute(CommonKeys.ACTIVITY_TYPE);
            segment.setAttribute(CommonKeys.LEG_PURPOSE, type);
        }
    }
}
