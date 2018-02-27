package de.dbanalytics.spic.gis;

import de.dbanalytics.spic.gis.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

/**
 * @author johannes
 */
public class PlaceIndexLoader implements DataLoader {

    private final int epsg;

    public PlaceIndexLoader(int epsg) {
        this.epsg = epsg;
    }

    @Override
    public Object load(String filename) {
        try {
            PlacesIO placesIO = new PlacesIO();
            placesIO.setGeoTransformer(GeoTransformer.WGS84toX(epsg));
            Set<Place> places = placesIO.read(filename);
            return new PlaceIndex(places);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return null;
    }
}
