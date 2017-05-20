/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.mid2008.generator;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.mid2008.MiDValues;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class FileReader {

    private final static Logger logger = Logger.getLogger(FileReader.class);

    private final static String DOT = ".";

    private final List<PersonAttributeHandler> personAttHandlers = new ArrayList<>();

    private final List<LegAttributeHandler> legAttHandlers = new ArrayList<>();

    private final List<LegAttributeHandler> journeyAttHandlers = new ArrayList<>();

    private final List<EpisodeAttributeHandler> episodeAttHandlers = new ArrayList<>();

    private final Factory factory;

    private Map<String, Person> persons;

    public FileReader(Factory factory) {
        this.factory = factory;
    }

    public void addPersonAttributeHandler(PersonAttributeHandler handler) {
        personAttHandlers.add(handler);
    }

    public void addLegAttributeHandler(LegAttributeHandler handler) {
        legAttHandlers.add(handler);
    }

    public void addJourneyAttributeHandler(LegAttributeHandler handler) {
        journeyAttHandlers.add(handler);
    }

    public void addEpisodeAttributeHandler(EpisodeAttributeHandler handler) {
        episodeAttHandlers.add(handler);
    }

    public Set<? extends Person> read(String personFile, String legFile, String journeyFile) throws
            IOException {

        persons = new LinkedHashMap<>(65000);
        /*
		 * read and create persons
		 */
        logger.info("Reading persons...");
        new PersonRowHandler().read(personFile);
		/*
		 * read and create legs
		 */
        logger.info("Reading trips...");
        new LegRowHandler().read(legFile);
		/*
		 * read and create journeys
		 */
        logger.info("Reading journeys...");
        new JourneyRowHandler().read(journeyFile);

        return new LinkedHashSet<>(persons.values());
    }

    private String personIdBuilder(Map<String, String> attributes) {
        StringBuilder builder = new StringBuilder(20);
        builder.append(attributes.get(VariableNames.HOUSEHOLD_ID));
        builder.append(DOT);
        builder.append(attributes.get(VariableNames.PERSON_ID));

        return builder.toString();
    }

    private class LegRowHandler extends RowHandler {

        @Override
        protected void handleRow(Map<String, String> attributes) {
            String id = personIdBuilder(attributes);
            Person person = persons.get(id);

            Segment leg = factory.newSegment();
            for (LegAttributeHandler handler : legAttHandlers)
                handler.handle(leg, attributes);

            person.getEpisodes().get(0).addLeg(leg);
        }

    }

    private class PersonRowHandler extends RowHandler {

        @Override
        protected void handleRow(Map<String, String> attributes) {
            Person person = factory.newPerson(personIdBuilder(attributes));

            for (PersonAttributeHandler handler : personAttHandlers) {
                handler.handle(person, attributes);
            }
			/*
		    * add an empty plan to each person
		    */
            Episode episode = factory.newEpisode();
            episode.setAttribute(CommonKeys.DATA_SOURCE, MiDValues.MID_TRIPS);
            person.addEpisode(episode);

            persons.put(person.getId(), person);
        }

    }

    private class JourneyRowHandler extends RowHandler {

        @Override
        protected void handleRow(Map<String, String> attributes) {
            String id = personIdBuilder(attributes);
            Person person = persons.get(id);

            Episode episode = factory.newEpisode();
            episode.setAttribute(CommonKeys.DATA_SOURCE, MiDValues.MID_JOUNREYS);
            for (EpisodeAttributeHandler handler : episodeAttHandlers) {
                handler.handle(episode, attributes);
            }

            person.addEpisode(episode);

            Segment leg = factory.newSegment();
            episode.addLeg(leg);
            leg.setAttribute(MiDKeys.LEG_ORIGIN, ActivityTypes.HOME);
            for (LegAttributeHandler handler : journeyAttHandlers) {
                handler.handle(leg, attributes);
            }
        }

    }
}
