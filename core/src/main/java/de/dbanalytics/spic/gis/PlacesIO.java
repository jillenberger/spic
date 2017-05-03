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

package de.dbanalytics.spic.gis;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.util.IOUtils;
import javanet.staxutils.IndentingXMLEventWriter;
import org.geotools.geometry.jts.JTSFactoryFinder;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jillenberger
 */
public class PlacesIO {

    private static final String PLACES_ELEMENT = "places";

    private static final String PLACE_ELEMENT = "place";

    private static final String ATTRIBUTE_ELEMENT = "attribute";

    private static final String ACTIVITY_ELEMENT = "activity";

    private static final String ID_ATTRIBUTE = "id";

    private static final String COORDINATE_ATTRIBUTE = "coordinate";

    private static final String NAME_ATTRIBUTE = "name";

    private static final String TYPE_ATTRIBUTE = "type";

    private static final String EMPTY = "";

    private GeoTransformer transformer = GeoTransformer.identityTransformer();

    public void setGeoTransformer(GeoTransformer transformer) {
        this.transformer = transformer;
    }

    public Set<Place> read(String filename) throws IOException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(IOUtils.createInputStream(filename));

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Set<Place> places = null;
        Place place = null;

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if (localName.equals(PLACES_ELEMENT)) {
                    /*
                    Init places set.
                     */
                    places = new HashSet<>();

                } else if (localName.equals(PLACE_ELEMENT)) {
                    /*
                    Parse place element.
                     */
                    if (places != null) {
                        String id = startElement.getAttributeByName(new QName(ID_ATTRIBUTE)).getValue();
                        String coord = startElement.getAttributeByName(new QName(COORDINATE_ATTRIBUTE)).getValue();

                        Point point = geometryFactory.createPoint(CoordinateUtils.parse(coord));
                        transformer.forward(point);

                        place = new Place(id, point);
                        places.add(place);
                    }

                } else if (startElement.getName().getLocalPart().equals(ATTRIBUTE_ELEMENT)) {
                    /*
                    Parse attribute element.
                     */
                    if (place != null) {
                        String name = startElement.getAttributeByName(new QName(NAME_ATTRIBUTE)).getValue();
                        String value = reader.nextEvent().asCharacters().getData();
                        place.setAttribute(name, value);
                    }
                } else if (startElement.getName().getLocalPart().equals(ACTIVITY_ELEMENT)) {
                    /*
                    Parse activity element.
                     */
                    if (place != null) {
                        String type = startElement.getAttributeByName(new QName(TYPE_ATTRIBUTE)).getValue();
                        place.addActivity(type);
                    }
                }
            } else if (event.isEndElement()) {
                /*
                For safety, release place pointer.
                 */
                EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals(PLACE_ELEMENT)) place = null;
            }
        }

        return places;
    }

    public void write(Collection<Place> places, String filename) throws IOException, XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        OutputStream stream = IOUtils.createOutputStream(filename);
        XMLEventWriter writer = new IndentingXMLEventWriter(outputFactory.createXMLEventWriter(stream));
        XMLEventFactory factory = XMLEventFactory.newInstance();

        writer.add(factory.createStartDocument());
        writer.add(factory.createStartElement(EMPTY, EMPTY, PLACES_ELEMENT));

        for (Place place : places) {
            /*
            Write place element.
             */
            writer.add(factory.createStartElement(EMPTY, EMPTY, PLACE_ELEMENT));
            writer.add(factory.createAttribute(ID_ATTRIBUTE, place.getId()));

            transformer.backward(place.getGeometry());
            writer.add(factory.createAttribute(COORDINATE_ATTRIBUTE,
                    CoordinateUtils.toString(place.getGeometry().getCoordinate())));
            /*
            Write activities.
             */
            for (String activity : place.getActivities()) {
                writer.add(factory.createStartElement(EMPTY, EMPTY, ACTIVITY_ELEMENT));
                writer.add(factory.createAttribute(TYPE_ATTRIBUTE, activity));
                writer.add(factory.createEndElement(EMPTY, EMPTY, ACTIVITY_ELEMENT));
            }
            /*
            Writer attributes.
             */
            for (Map.Entry<String, String> entry : place.getAttributes().entrySet()) {
                writer.add(factory.createStartElement(EMPTY, EMPTY, ATTRIBUTE_ELEMENT));
                writer.add(factory.createAttribute(NAME_ATTRIBUTE, entry.getKey()));
                writer.add(factory.createCharacters(entry.getValue()));
                writer.add(factory.createEndElement(EMPTY, EMPTY, ATTRIBUTE_ELEMENT));
            }

            writer.add(factory.createEndElement(EMPTY, EMPTY, PLACE_ELEMENT));
        }

        writer.add(factory.createEndElement(EMPTY, EMPTY, PLACES_ELEMENT));
        writer.add(factory.createEndDocument());

        writer.flush();
        writer.close();
        stream.close();
    }
}
