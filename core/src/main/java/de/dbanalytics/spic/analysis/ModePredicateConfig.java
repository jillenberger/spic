package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.analysis.LegAttributePredicate;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.PredicateOrComposite;
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.job.Configurator;
import org.apache.commons.configuration2.HierarchicalConfiguration;


public class ModePredicateConfig implements Configurator<Predicate<Segment>> {

    private static final String MODES = "modes";

    @Override
    public Predicate<Segment> configure(HierarchicalConfiguration config) {
        PredicateOrComposite<Segment> predicate = new PredicateOrComposite<>();

        String modes[] = config.getString(MODES).split("\\s");
        for (String mode : modes) {
            predicate.addComponent(new LegAttributePredicate(Attributes.KEY.MODE, mode));
        }

        return predicate;
    }

    @Override
    public Predicate<Segment> configure(HierarchicalConfiguration config, Predicate<Segment> object) {
        return object;
    }
}
