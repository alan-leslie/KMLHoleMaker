package Conversion;

import de.micromata.opengis.kml.v_2_2_0.AbstractObject;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author al
 * TDOD - remove dupicate points
 * add intermediate points
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
        Document theDocument = (Document)theInputKML.getFeature();
        List<Feature> theDocumentFeatures = theDocument.getFeature();
        Document theConvertedDocument = (Document)theConvertedKML.getFeature();
        List<Feature> theConvertedFeatures = new ArrayList<>();
        
        for(Feature theFeature: theDocumentFeatures){
            Folder theFolder = (Folder)theFeature; 
            List<Feature> theObjects = theFolder.getFeature();
            Folder theConvertedFolder = theFolder.clone();
            List<Feature> theConvertedObjects = new ArrayList<>();
            
            for (Feature theObject : theObjects) {
                Placemark thePlacemark = (Placemark)theObject;
                Placemark convertedPlacemark = thePlacemark.clone();
                // this not going to work cos there can only be one
                // outer per placemear 
                // so need to replace single placemark with multiples
                
                try {
                    Polygon thePolygon = (Polygon)convertedPlacemark.getGeometry();
                    Boundary outer = thePolygon.getOuterBoundaryIs();
                    List<Boundary> innerBoundaryIs = thePolygon.getInnerBoundaryIs();
                    List<Boundary> newInner = new ArrayList<>();

                    if(!(innerBoundaryIs.isEmpty())){
                        for(Boundary innerBoundary: innerBoundaryIs){
                            List<Coordinate> theCoords = innerBoundary.getLinearRing().getCoordinates();
                            int northIndex = nothernmostIndex(theCoords); 
 
                            Coordinate nextEast = nextEasterlyPoint(theCoords, northIndex);
                            Coordinate nextWest = nextWesterlyPoint(theCoords, northIndex);
                            
                            // need the index also ???
                            Coordinate eastOuter = findOuterIntersect(nextEast, 90.0, outer);
                            Coordinate westOuter = findOuterIntersect(nextWest, -90.0, outer);
                            
                            // new segments to be added to outer are:
                            // nextEast -> eastOuter
                            // nextWest -> westOuter
                            // plus all of thos north of the outer coords
                        }
                        
                        // this is the important bit and it should create new placemarks
                        // but start with cutting the pentagon
                        List<Coordinate> newCoords = new ArrayList<>();
                        LinearRing newRing = thePolygon.getOuterBoundaryIs().getLinearRing().clone();
                        newRing.setCoordinates(newCoords);
                        Boundary newBoundary = thePolygon.getOuterBoundaryIs().clone();
                        newBoundary.setLinearRing(newRing);
                        thePolygon.setOuterBoundaryIs(newBoundary);
                        thePolygon.setInnerBoundaryIs(newInner);
                    }
                } catch(ClassCastException exc) {
                    // ...
                }
                            
                theConvertedObjects.add(convertedPlacemark);
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
    public static Coordinate nextEasterlyPoint(List<Coordinate> polygon, int index) {
        // to be a proper polygon needs at least 4 coords 
        // first and last are the same 
        Coordinate next = (index == (polygon.size() -1)) ? polygon.get(1) : polygon.get(index + 1);
        Coordinate prev = (index == 0) ? polygon.get(index -1) : polygon.get(index -1);
        
        if(next.getLongitude() > prev.getLongitude()){
            return next;
        } else {
            if(next.getLongitude() == prev.getLongitude()){
                // TODO - 
                // most northerly if both west 
                // southerly if both east   
                return next;
            } else {
                return prev; 
            }
        }
    }

    public static Coordinate nextWesterlyPoint(List<Coordinate> polygon, int index) {
        Coordinate next = (index == (polygon.size() -1)) ? polygon.get(1) : polygon.get(index + 1);
        Coordinate prev = (index == 0) ? polygon.get(index -1) : polygon.get(index -1);
        
        if(next.getLongitude() > prev.getLongitude()){
            return prev;
        } else {
            if(next.getLongitude() == prev.getLongitude()){
                // TODO - 
                // most northerly if both west 
                // southerly if both east   
                return prev;
            } else {
                return next; 
            }
        }
    }
    
    public static int southernmostIndex(List<Coordinate> polygon) {
        int retVal = -1;
        double theLatitude = 90.0;
        
        int i = 0;
        for(Coordinate theCoord: polygon){
            if(theCoord.getLatitude() < theLatitude){
                retVal = i;
                theLatitude = theCoord.getLatitude();
            }
            
            ++i;
        }
        
        return retVal;    
    }
    
    public static int nothernmostIndex(List<Coordinate> polygon) {
        int retVal = -1;
        double theLatitude = -90.0;
        
        int i = 0;
        for(Coordinate theCoord: polygon){
            if(theCoord.getLatitude() > theLatitude){
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

        Coordinate retVal = new Coordinate(Math.toDegrees(lat3), Math.toDegrees(lon3));

        return retVal;
    }

    private static double getInitialBearing(Coordinate pt1, Coordinate pt2) {
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

    private static Coordinate getPointOnLine(Coordinate pt1, Coordinate pt2, double d) {
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
 * Returns the point of intersection of two paths defined by point and bearing
 *
 * see http://williams.best.vwh.net/avform.htm#Intersection
 *
 * @param {LatLon} p1: First point
 * @param {Number} brng1: Initial bearing from first point
 * @param {LatLon} p2: Second point
 * @param {Number} brng2: Initial bearing from second point
 * @returns {LatLon} Destination point (null if no unique intersection defined)
 */
public static Coordinate LatLonIntersection(Coordinate p1, double brng1, Coordinate p2, double brng2) {
    double lat1 = Math.toRadians(p1.getLatitude());
    double lon1 = Math.toRadians(p1.getLongitude());
    double lat2 = Math.toRadians(p2.getLatitude());
    double lon2 = Math.toRadians(p2.getLongitude());
    double brng13 = Math.toRadians(brng1); 
    double brng23 = Math.toRadians(brng2);
    double dLat = lat2 - lat1, dLon = lon2 - lon1;
    double dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)));

    if (dist12 == 0D){
        return null;
    }
    // initial/final bearings between points
    double brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(dist12)) / (Math.sin(dist12) * Math.cos(lat1)));
    if (brngA != Double.NaN) {
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
    if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0){
        return null;
    } // infinite intersections
    if (Math.sin(alpha1) * Math.sin(alpha2) < 0){
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

    return new Coordinate(Math.toDegrees(lat3), Math.toDegrees(lon3));
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

    private Coordinate findOuterIntersect(Coordinate nextEast, double bearing, Boundary outer) {
        // these need to come from outer
        // iterate over all segments - until you find an intersect point
        // that is also inside the segment
        Coordinate retVal = null; 
        List<Coordinate> coordinates = outer.getLinearRing().getCoordinates();
        
        for(int i = 1; i < coordinates.size() && retVal == null; ++i){
            Coordinate boundarySegmentStart = coordinates.get(i - 1);
            Coordinate boundarySegmentEnd = coordinates.get(i);
            double boundarySegmentBearing = getInitialBearing(boundarySegmentStart, boundarySegmentEnd);
            
            Coordinate theIntersect = LatLonIntersection(nextEast, bearing, boundarySegmentStart, boundarySegmentBearing);
            
            if(theIntersect != null && isInSegment(boundarySegmentStart, boundarySegmentEnd, theIntersect)){
                retVal = theIntersect;
            }
        }
        
        return retVal;
    }
    
    private static boolean isInSegment(Coordinate segmentStart, Coordinate segmentEnd, Coordinate testPoint){
        double northerlyLat = segmentStart.getLatitude();
        double southerlyLat = segmentEnd.getLatitude();
        double easterlyLon = segmentStart.getLongitude();
        double westerlyLon = segmentEnd.getLongitude();
        
        if(northerlyLat < southerlyLat){
            southerlyLat = segmentEnd.getLatitude();
            northerlyLat = segmentStart.getLatitude();        
        }

        if(easterlyLon < westerlyLon){
            easterlyLon = segmentEnd.getLongitude();
            westerlyLon = segmentStart.getLongitude();        
        }
        
        if((testPoint.getLatitude() >= southerlyLat && testPoint.getLatitude() <= northerlyLat) &&
                (testPoint.getLongitude() >= westerlyLon && testPoint.getLongitude() <= easterlyLon)){
            return true;
        } else {
             return false; 
        }
    }
}
