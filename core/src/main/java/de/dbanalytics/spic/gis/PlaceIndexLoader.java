package de.dbanalytics.spic.gis;

import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

/**
 * @author johannes
 */
public class PlaceIndexLoader implements DataLoader {

    private static final Logger logger = Logger.getLogger(PlaceIndexLoader.class);

    private final int epsg;

    public PlaceIndexLoader(int epsg) {
        this.epsg = epsg;
    }

    public PlaceIndexLoader() {
        epsg = 0;
    }

    @Override
    public Object load(String filename) {
        try {
            PlacesIO placesIO = new PlacesIO();
            if(epsg > 0) {
                placesIO.setGeoTransformer(GeoTransformer.WGS84toX(epsg));
            }
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
