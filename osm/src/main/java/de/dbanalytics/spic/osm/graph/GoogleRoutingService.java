package de.dbanalytics.spic.osm.graph;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleRoutingService implements RoutingService {

    private final GeoApiContext context;

    public GoogleRoutingService(String apikey) {
        context = new GeoApiContext.Builder().apiKey(apikey).build();
    }

    @Override
    public Route query(double fromLon, double fromLat, double toLon, double toLat) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(context).
                    origin(new LatLng(fromLat, fromLon)).
                    destination(new LatLng(toLat, toLon)).
                    mode(TravelMode.DRIVING).
                    alternatives(false).
                    units(Unit.METRIC).
                    await();

            if (result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                return new GoogleRoute(route.legs);
            } else {
                return null;
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    public static class GoogleRoute implements Route {

        private final double traveltime;

        private final double distance;

        private final List<RouteLeg> routeLegs;

        private GoogleRoute(DirectionsLeg legs[]) {
            double tmptt = 0;
            double tmpdist = 0;

            routeLegs = new ArrayList<>(legs.length);
            for (DirectionsLeg leg : legs) {
                routeLegs.add(new GoogleRouteLeg(leg.duration.inSeconds, leg.distance.inMeters));
                tmpdist += leg.distance.inMeters;
                tmptt += leg.duration.inSeconds;
            }

            traveltime = tmptt;
            distance = tmpdist;
        }

        @Override
        public double traveltime() {
            return traveltime;
        }

        @Override
        public double distance() {
            return distance;
        }

        @Override
        public List<RouteLeg> routeLegs() {
            return routeLegs;
        }
    }

    public static class GoogleRouteLeg implements RouteLeg {

        private final double traveltime;

        private final double distance;

        private GoogleRouteLeg(double traveltime, double distance) {
            this.traveltime = traveltime;
            this.distance = distance;
        }

        @Override
        public double traveltime() {
            return traveltime;
        }

        @Override
        public double distance() {
            return distance;
        }

        @Override
        public long[] nodes() {
            return new long[0];
        }
    }
}
