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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.util.IOUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
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

    private static final String NEW_LINE = "\n";

    private static final String SPACES = "    ";

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
                    places = new LinkedHashSet<>();

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

    public void write(Collection<? extends Place> places, String filename) throws IOException, XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        OutputStream stream = IOUtils.createOutputStream(filename);
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);

        writer.writeStartDocument();
        writer.writeCharacters(NEW_LINE);
        writer.writeStartElement(PLACES_ELEMENT);
        writer.writeCharacters(NEW_LINE);

        for (Place place : places) {

            writer.writeCharacters(SPACES);
            writer.writeStartElement(PLACE_ELEMENT);
            writer.writeAttribute(ID_ATTRIBUTE, place.getId());

            Coordinate coordinate = new Coordinate(place.getGeometry().getCoordinate());
            transformer.backward(coordinate);
            writer.writeAttribute(COORDINATE_ATTRIBUTE, CoordinateUtils.toString(coordinate));
            writer.writeCharacters(NEW_LINE);

            for (String activity : place.getActivities()) {
                writer.writeCharacters(SPACES);
                writer.writeCharacters(SPACES);
                writer.writeStartElement(ACTIVITY_ELEMENT);
                writer.writeAttribute(TYPE_ATTRIBUTE, activity);
                writer.writeEndElement();
                writer.writeCharacters(NEW_LINE);
            }

            for (Map.Entry<String, String> entry : place.getAttributes().entrySet()) {
                writer.writeCharacters(SPACES);
                writer.writeCharacters(SPACES);
                writer.writeStartElement(ATTRIBUTE_ELEMENT);
                writer.writeAttribute(NAME_ATTRIBUTE, entry.getKey());
                writer.writeCharacters(entry.getValue());
                writer.writeEndElement();
                writer.writeCharacters(NEW_LINE);
            }

            writer.writeCharacters(SPACES);
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
        }

        writer.writeEndElement();
        writer.writeCharacters(NEW_LINE);
        writer.writeEndDocument();

        writer.flush();
        writer.close();
        stream.close();
    }
}
