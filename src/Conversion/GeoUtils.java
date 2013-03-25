/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alan
 */
public class GeoUtils {
    // Hversine formula from http://www.movable-type.co.uk/scripts/latlong.html
    // other places I have seen more precise earth radius measure (R)

    static double distance(Coordinate pt1, Coordinate pt2) {
        double R = 6371.0D; // km
        double lat1 = Math.toRadians(pt1.getLatitude());
        double lat2 = Math.toRadians(pt2.getLatitude());
        double lon1 = Math.toRadians(pt1.getLongitude());
        double lon2 = Math.toRadians(pt2.getLongitude());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = ((Math.sin(dLat / 2) * Math.sin(dLat / 2))
                + (Math.sin(dLon / 2) * Math.sin(dLon / 2))) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = R * c;

        return dist;
    }

    static Coordinate findIntersect(Coordinate referencePoint, double bearing, List<Coordinate> coordinates) {
        // these need to come from outer
        // iterate over all segments - until you find an intersect point
        // that is also inside the segment
        Coordinate retVal = null;
        List<Coordinate> theIntersects = new ArrayList<>();

        for (int i = 1; i < coordinates.size(); ++i) {
            int boundarySegmentStartPos = i - 1;
            Coordinate boundarySegmentStart = coordinates.get(boundarySegmentStartPos);
            Coordinate boundarySegmentEnd = coordinates.get(i);
            double boundarySegmentBearing = getInitialBearing(boundarySegmentStart, boundarySegmentEnd);

            Coordinate theIntersect = calculateLatLonIntersection(referencePoint, bearing, boundarySegmentStart, boundarySegmentBearing);

            if (theIntersect != null) {
                if (isInSegment(boundarySegmentStart, boundarySegmentEnd, theIntersect)) {
                    theIntersects.add(theIntersect);
                }
            }
        }

        // want to get the closest to the test point
        double minLongitude = 180;
        int i = 0;
        for (Coordinate theIntersect : theIntersects) {
            double intersectLongitude = theIntersect.getLongitude();
            double absLon = Math.abs(intersectLongitude - referencePoint.getLongitude());

            if (absLon < minLongitude) {
                minLongitude = absLon;
                retVal = theIntersect;
            }

            ++i;
        }

        return retVal;
    }

    static int findIntersectSegmentIndex(Coordinate testPoint, List<Coordinate> coordinates) {
        int retVal = -1;
        for (int i = 1; i < coordinates.size(); ++i) {
            int boundarySegmentStartPos = i - 1;
            Coordinate boundarySegmentStart = coordinates.get(boundarySegmentStartPos);
            Coordinate boundarySegmentEnd = coordinates.get(i);
            if (isInSegment(boundarySegmentStart, boundarySegmentEnd, testPoint)) {
                retVal = boundarySegmentStartPos;
            }
        }

        return retVal;
    }

    static boolean isInSegment(Coordinate segmentStart, Coordinate segmentEnd, Coordinate testPoint) {
        if (segmentStart.equals(testPoint) || segmentEnd.equals(testPoint)) {
            return true;
        }

        double epsilon = 0.00001D;
        double brng1 = getInitialBearing(segmentStart, testPoint);
        double brng2 = getInitialBearing(segmentStart, segmentEnd);
        double brng3 = getInitialBearing(testPoint, segmentStart);
        double brng4 = getInitialBearing(segmentEnd, segmentStart);

        double brngDiff = Math.abs(brng1 - brng2);

        if (brngDiff < epsilon) {
            double distance1 = distance(segmentStart, segmentEnd);
            double distance2 = distance(segmentStart, testPoint);

            if (distance1 > distance2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the point of intersection of two paths defined by point and
     * bearing
     *
     * see http://williams.best.vwh.net/avform.htm#Intersection
     *
     * @param {LatLon} p1: First point
     * @param {Number} brng1: Initial bearing from first point
     * @param {LatLon} p2: Second point
     * @param {Number} brng2: Initial bearing from second point
     * @returns {LatLon} Destination point (null if no unique intersection
     * defined)
     */
    public static Coordinate calculateLatLonIntersection(Coordinate p1, double brng1, Coordinate p2, double brng2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());
        double brng13 = Math.toRadians(brng1);
        double brng23 = Math.toRadians(brng2);
        double dLat = lat2 - lat1, dLon = lon2 - lon1;
        double dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)));

        if (dist12 == 0D) {
            return null;
        }
        // initial/final bearings between points
        double brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(dist12)) / (Math.sin(dist12) * Math.cos(lat1)));
        if (brngA == Double.NaN) {
            brngA = 0;
        } // protect against rounding

        double brngB = Math.acos((Math.sin(lat1) - Math.sin(lat2) * Math.cos(dist12)) / (Math.sin(dist12) * Math.cos(lat2)));

        double brng12 = 0.0D;
        double brng21 = 0.0D;

        if (Math.sin(lon2 - lon1) > 0) {
            brng12 = brngA;
            brng21 = 2 * Math.PI - brngB;
        } else {
            brng12 = 2 * Math.PI - brngA;
            brng21 = brngB;
        }
        double alpha1 = (brng13 - brng12 + Math.PI) % (2 * Math.PI) - Math.PI; // angle 2-1-3
        double alpha2 = (brng21 - brng23 + Math.PI) % (2 * Math.PI) - Math.PI; // angle 1-2-3
        if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0) {
            return null;
        } // infinite intersections
        if (Math.sin(alpha1) * Math.sin(alpha2) < 0) {
            return null;
        } // ambiguous intersection
        //alpha1 = Math.abs(alpha1);
        //alpha2 = Math.abs(alpha2);
        // ... Ed Williams takes abs of alpha1/alpha2, but seems to break calculation?
        double alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) + Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(dist12));
        double dist13 = Math.atan2(Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2), Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3));
        double lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13) + Math.cos(lat1) * Math.sin(dist13) * Math.cos(brng13));
        double dLon13 = Math.atan2(Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1), Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3));
        double lon3 = lon1 + dLon13;
        lon3 = (lon3 + 3 * Math.PI) % (2 * Math.PI) - Math.PI; // normalise to -180..+180º

        return new Coordinate(Math.toDegrees(lon3), Math.toDegrees(lat3));
    }

    public static double getInitialBearing(Coordinate pt1, Coordinate pt2) {
        double lat1 = pt1.getLatitude();
        double lat2 = pt2.getLatitude();
        double dLon = Math.toRadians(pt2.getLongitude() - pt1.getLongitude());

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);
        brng = Math.toDegrees(brng);

        return brng;
    }

    public static Coordinate getPointOnLine(Coordinate pt1, Coordinate pt2, double d) {
        double lat1 = pt1.getLatitude();
        double lon1 = pt1.getLongitude();
        double brng = getInitialBearing(pt1, pt2);
        double R = 6371000.0;

        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R)
                + Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
                Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        Coordinate retVal = new Coordinate(lat2, lon2);

        return retVal;
    }

    static int southernmostIndex(List<Coordinate> polygon) {
        int retVal = -1;
        double theLatitude = 90.0;

        int i = 0;
        for (Coordinate theCoord : polygon) {
            if (theCoord.getLatitude() < theLatitude) {
                retVal = i;
                theLatitude = theCoord.getLatitude();
            }

            ++i;
        }

        return retVal;
    }

    static int nothernmostIndex(List<Coordinate> polygon) {
        int retVal = -1;
        double theLatitude = -90.0;

        int i = 0;
        for (Coordinate theCoord : polygon) {
            if (theCoord.getLatitude() > theLatitude) {
                retVal = i;
                theLatitude = theCoord.getLatitude();
            }

            ++i;
        }

        return retVal;
    }

    public static Coordinate midPoint(Coordinate pt1, Coordinate pt2) {
        double lon1 = pt1.getLongitude();
        double lat1 = pt1.getLatitude();
        double lon2 = pt2.getLongitude();
        double lat2 = pt2.getLatitude();

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        Coordinate retVal = new Coordinate(Math.toDegrees(lon3), Math.toDegrees(lat3));

        return retVal;
    }

    static Coordinate nextWesterlyPoint(List<Coordinate> polygon, int index) {
        Coordinate next = (index == (polygon.size() - 1)) ? polygon.get(1) : polygon.get(index + 1);
        Coordinate prev = (index == 0) ? polygon.get(polygon.size() - 2) : polygon.get(index - 1);

        if (next.getLongitude() > prev.getLongitude()) {
            return prev;
        } else {
            if (next.getLongitude() == prev.getLongitude()) {
// TODO - throw exception
                return null;
            } else {
                return next;
            }
        }
    }

    // assuming coords are clockwise
    // these are only correct if index is in the middle of the coords
    // if it is at 0 then need length - 2 
    // if it is at other end then need 1
    // maybe just get the coord at index 
    // then next or prev till west or east is found
    // also need to look out for the case where there are 
    // multiples at the northernmost (jagged polygons)
    // got to wath this 
    // could have a scalene triangle so all points are east (or west)
    // to be a proper polygon needs at least 4 coords 
    // first and last are the same 
    // precon - polygon must be a polygon - so at least 4 coords
    // and that northernmost has a real next and prev - thinsk that that follows
    // precon - that next and prev do not have the same longitude (on the same line)
    // 
    static Coordinate nextEasterlyPoint(List<Coordinate> polygon, int index) {
        Coordinate next = (index == (polygon.size() - 1)) ? polygon.get(1) : polygon.get(index + 1);
        Coordinate prev = (index == 0) ? polygon.get(polygon.size() - 2) : polygon.get(index - 1);

        if (next.getLongitude() > prev.getLongitude()) {
            return next;
        } else {
            if (next.getLongitude() == prev.getLongitude()) {
// TODO - throw exception
                return null;
            } else {
                return prev;
            }
        }
    }
}
