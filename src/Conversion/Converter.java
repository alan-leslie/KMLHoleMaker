package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author al TDOD - remove dupicate points add intermediate points
 */
public class Converter {

    private Kml theInputKML = null;
    private Kml theConvertedKML = null;
    private Logger theLogger = null;

    public Converter(Kml theKML, Logger theLogger) {
        this.theInputKML = theKML;
        this.theLogger = theLogger;
    }

    public Kml getInputKML() {
        return theInputKML;
    }

    public Kml getConvertedKML() {
        return theConvertedKML;
    }

    public void convert() {
        theConvertedKML = theInputKML.clone();
        Document theDocument = (Document) theInputKML.getFeature();
        List<Feature> theDocumentFeatures = theDocument.getFeature();
        Document theConvertedDocument = (Document) theConvertedKML.getFeature();
        List<Feature> theConvertedFeatures = new ArrayList<>();

        for (Feature theFeature : theDocumentFeatures) {
            Folder theFolder = (Folder) theFeature;
            List<Feature> theObjects = theFolder.getFeature();
            Folder theConvertedFolder = theFolder.clone();
            List<Feature> theConvertedObjects = new ArrayList<>();

            for (Feature theObject : theObjects) {
                Placemark thePlacemark = (Placemark) theObject;
                // this not going to work cos there can only be one
                // outer per placemear 
                // so need to replace single placemark with multiples

                try {
                    Polygon thePolygon = (Polygon) thePlacemark.getGeometry();
                    Boundary outer = thePolygon.getOuterBoundaryIs();
                    List<Boundary> innerBoundaryIs = thePolygon.getInnerBoundaryIs();
                    List<Boundary> newInner = new ArrayList<>();

                    if (!(innerBoundaryIs.isEmpty())) {
                        // precon - outer must be closed polygon
                        // more than two different points

                        // this is the important bit and it should create new placemarks
                        // but start with cutting the pentagon
                        LinearRing newRing = thePolygon.getOuterBoundaryIs().getLinearRing().clone();

                        for (Boundary innerBoundary : innerBoundaryIs){
                            List<Boundary> emptyInner = new ArrayList<>();
                            
                            Placemark northPlacemark = thePlacemark.clone();
                            Polygon northPolygon = (Polygon)northPlacemark.getGeometry();
                            List<Coordinate> northCoords = getNorthSlice(innerBoundary.getLinearRing().getCoordinates(),
                                    outer.getLinearRing().getCoordinates());
                            northPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(northCoords);
                            northPolygon.setInnerBoundaryIs(emptyInner);
                            theConvertedObjects.add(northPlacemark);
                            
                            Placemark southPlacemark = thePlacemark.clone();
                            Polygon southPolygon = (Polygon)southPlacemark.getGeometry();
                            List<Coordinate> southCoords = getSouthSlice(innerBoundary.getLinearRing().getCoordinates(),
                                    outer.getLinearRing().getCoordinates());
                            southPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
                            southPolygon.setInnerBoundaryIs(emptyInner);
                            theConvertedObjects.add(southPlacemark);
                        }
                    }
                } catch (ClassCastException exc) {
                    // ...
                }
            }

            theConvertedFolder.setFeature(theConvertedObjects);
            theConvertedFeatures.add(theConvertedFolder);
        }

        theConvertedDocument.setFeature(theConvertedFeatures);
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

    public static void main(String[] args) {
//        Coordinate pt1 = new Coordinate();
//        Coordinate pt2 = new Coordinate();
////        pt1.setLatitude(34.122222);
////        pt1.setLongitude(118.4111111);
////        pt2.setLatitude(40.66972222);
////        pt2.setLongitude(73.94388889);
//        pt1.setLatitude(35.0);
//        pt1.setLongitude(45.0);
//        pt2.setLatitude(35.0);
//        pt2.setLongitude(135.0);
//        Coordinate midPoint = midPoint(pt1, pt2);
//        Coordinate bCheck1 = new Coordinate();
//        Coordinate bCheck2 = new Coordinate();
//        bCheck1.setLatitude(53.32056);
//        bCheck1.setLongitude(-1.72972);
//        bCheck2.setLatitude(53.18806);
//        bCheck2.setLongitude(0.13639);
//
//        double bearing = getInitialBearing(bCheck1, bCheck2);
//        Waypoint dPt1 = new Waypoint();
//        Waypoint dPt2 = new Waypoint();
//        dPt1.setCoordinate(bCheck1);
//        dPt2.setCoordinate(bCheck2);
//        double distance = dPt1.calculateDistanceTo(dPt2);
//
//        Coordinate theEndPoint = getPointOnLine(bCheck1, bCheck2, distance);
//
//        //print out in degrees
////        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
//        System.out.println(midPoint.getLatitude() + " " + midPoint.getLongitude());
//        System.out.println("Bearing: " + Double.toString(bearing));
//        System.out.println("Distance: " + Double.toString(distance));
//        System.out.println(theEndPoint.getLatitude() + " " + theEndPoint.getLongitude());
    }

    static Coordinate findIntersect(Coordinate nextEast, double bearing, List<Coordinate> coordinates) {
        // these need to come from outer
        // iterate over all segments - until you find an intersect point
        // that is also inside the segment
        Coordinate retVal = null;
        List<Integer> theIntersects = new ArrayList<>();

        for (int i = 1; i < coordinates.size(); ++i) {
            int boundarySegmentStartPos = i - 1;
            Coordinate boundarySegmentStart = coordinates.get(boundarySegmentStartPos);
            Coordinate boundarySegmentEnd = coordinates.get(i);
            double boundarySegmentBearing = getInitialBearing(boundarySegmentStart, boundarySegmentEnd);

            Coordinate theIntersect = calculateLatLonIntersection(nextEast, bearing, boundarySegmentStart, boundarySegmentBearing);

            if (theIntersect != null) {
                if (isInSegment(boundarySegmentStart, boundarySegmentEnd, theIntersect)) {
                    theIntersects.add(boundarySegmentStartPos);
                }
            }
        }
        
        // want to get the closest to the test point
        double minLongitude = 180;
        for(Integer theIntersectIndex: theIntersects){
            double intersectLongitude = coordinates.get(theIntersectIndex).getLatitude();
            double absLon = Math.abs(intersectLongitude);
            
            if(absLon < minLongitude){
                minLongitude = absLon;
                retVal = coordinates.get(theIntersectIndex);
            }
        }

        return retVal;
    }
    
    static int findIntersectSegmentIndex(Coordinate nextEast, double bearing, List<Coordinate> coordinates) {
        int retVal = -1;
        List<Integer> theIntersects = new ArrayList<>();

        for (int i = 1; i < coordinates.size(); ++i) {
            int boundarySegmentStartPos = i - 1;
            Coordinate boundarySegmentStart = coordinates.get(boundarySegmentStartPos);
            Coordinate boundarySegmentEnd = coordinates.get(i);
            double boundarySegmentBearing = getInitialBearing(boundarySegmentStart, boundarySegmentEnd);

            Coordinate theIntersect = calculateLatLonIntersection(nextEast, bearing, boundarySegmentStart, boundarySegmentBearing);

            if (theIntersect != null) {
                if (isInSegment(boundarySegmentStart, boundarySegmentEnd, theIntersect)) {
                    theIntersects.add(boundarySegmentStartPos);
                }
            }
        }

        // want to get the closest to the test point
        double minLongitude = 180;
        for(Integer theIntersectIndex: theIntersects){
            double intersectLongitude = coordinates.get(theIntersectIndex).getLatitude();
            double absLon = Math.abs(intersectLongitude);
            
            if(absLon < minLongitude){
                minLongitude = absLon;
                retVal = theIntersectIndex;
            }
        }
        
        return retVal;
    }

    static boolean isInSegment(Coordinate segmentStart, Coordinate segmentEnd, Coordinate testPoint) {
        if (segmentStart.equals(testPoint) || segmentEnd.equals(testPoint)) {
            return true;
        }

        double epsilon = 0.000001D;
        double brng1 = getInitialBearing(segmentStart, testPoint);
        double brng2 = getInitialBearing(segmentStart, segmentEnd);
        if (Math.abs(brng1 - brng2) < epsilon) {
            double distance1 = distance(segmentStart, segmentEnd);
            double distance2 = distance(segmentStart, testPoint);

            if (distance1 > distance2) {
                return true;
            }
        }

        return false;
    }

    // Hversine formula from http://www.movable-type.co.uk/scripts/latlong.html
    // other places I have seen more precise earth radius measure (R)
    static double distance(Coordinate pt1, Coordinate pt2) {
        double R = 6371.0D; // km
        double lat1 = Math.toRadians(pt1.getLatitude());
        double lat2 = Math.toRadians(pt2.getLatitude());
        double lon1 = Math.toRadians(pt1.getLongitude());
        double lon2 = Math.toRadians(pt2.getLongitude());
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = ((Math.sin(dLat / 2) * Math.sin(dLat / 2))
                + (Math.sin(dLon / 2) * Math.sin(dLon / 2))) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = R * c;

        return dist;
    }

    // TODO - sort out for anticlockwise polygons
    static List<Coordinate> getSouthSlice(List<Coordinate> inner, List<Coordinate> outer) {
        List<Coordinate> newCoords = new ArrayList<>();
        int northIndex = nothernmostIndex(inner);

        Coordinate nextEast = nextEasterlyPoint(inner, northIndex);
        Coordinate nextWest = nextWesterlyPoint(inner, northIndex);
        
        // the inner could be anticlockwise
        // in which case the nextEast pos = northIndex - 1
        // and nextWest pos = northIndex + 1

        Coordinate eastOuter = findIntersect(nextEast, 90.0, outer);
        Coordinate westOuter = findIntersect(nextWest, -90.0, outer);
        int eastIndex = findIntersectSegmentIndex(nextEast, 90.0, outer);
        int westIndex = findIntersectSegmentIndex(nextWest, -90.0, outer);

        newCoords.add(eastOuter);

        // different pattern depending on whether clockwise or not
        // if east less than west may not mean the it it is clockwise???
        if (eastIndex < westIndex) {           
            for (int i = eastIndex; i < westIndex; ++i) {
                Coordinate segmentEnd = outer.get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            for (int i = westIndex; i < westIndex; ++i) {
                Coordinate segmentStart = i == 0 ? outer.get(outer.size() - 2) : outer.get(i - 1);
                Coordinate segmentEnd = outer.get(i);

                newCoords.add(segmentEnd);
            }
        }

        newCoords.add(westOuter);
        newCoords.add(nextWest);
        
        int noOfSegments = inner.size() -1;
        int startIndex = noOfSegments - 1;

        if(northIndex > 1){
            for (int i = northIndex + 2; i < inner.size(); ++i) {
                Coordinate segmentEnd = inner.get(i);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        }
        
        // TODO - deal with boundary case
        if(northIndex == 1){
            startIndex = noOfSegments - 2;
        } 

        if(northIndex == 0){
             startIndex = noOfSegments - 3;               
        }

        // start point is same as end pont so skip itr
        for (int i = 1; i < northIndex - 1; ++i) {
            Coordinate segmentEnd = inner.get(i);
            newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
        }
        
        newCoords.add(nextEast);
        newCoords.add(eastOuter);

        return newCoords;
    }
    
    static List<Coordinate> getNorthSlice(List<Coordinate> inner, List<Coordinate> outer) {
        List<Coordinate> newCoords = new ArrayList<>();
        int northIndex = nothernmostIndex(inner);

        Coordinate nextEast = nextEasterlyPoint(inner, northIndex);
        Coordinate nextWest = nextWesterlyPoint(inner, northIndex);

        Coordinate eastOuter = findIntersect(nextEast, 90.0, outer);
        Coordinate westOuter = findIntersect(nextWest, -90.0, outer);
        int eastIndex = findIntersectSegmentIndex(nextEast, 90.0, outer);
        int westIndex = findIntersectSegmentIndex(nextWest, -90.0, outer);

        newCoords.add(eastOuter);
        newCoords.add(nextEast);
        newCoords.add(inner.get(northIndex));
        newCoords.add(nextWest);
        newCoords.add(westOuter);

        // different pattern depending on whether clockwise or not
        if (eastIndex < westIndex) {
            int noOfSegments = outer.size() - 1;
            for (int i = westIndex; i < noOfSegments; ++i) {
                Coordinate segmentEnd = outer.get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
            
            for (int i = 0; i < eastIndex; ++i) {
                Coordinate segmentEnd = outer.get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            for (int i = westIndex; i < westIndex; ++i) {
                Coordinate segmentStart = i == 0 ? outer.get(outer.size() - 2) : outer.get(i - 1);
                Coordinate segmentEnd = outer.get(i);

                newCoords.add(segmentEnd);
            }
        }

        newCoords.add(eastOuter);

        return newCoords;
    }
}
