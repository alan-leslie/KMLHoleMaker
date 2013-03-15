/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.ArrayList;
import java.util.List;

// TODO - putb in invariants that identify this as a boundary
// polygon 
// at leats four points
// start and end are the same
/**
 *
 * @author alan
 */
public class InnerBoundary {

    private final List<Coordinate> points;
    private final int northIndex;
    private final Coordinate nextEast;
    private final Coordinate nextWest;
    private final boolean isClockwise;
    private final int index;
    private final OuterBoundary outer;
    private Intersection theEastIntersection;
    private Intersection theWestIntersection;
    private List<Intersection> theOtherIntersections; // intersections where this boundary is the
    // end point

    InnerBoundary(int theIndex, OuterBoundary theOuter, List<Coordinate> thePoints) {
        outer = theOuter;
        points = thePoints;

        northIndex = GeoUtils.nothernmostIndex(points);

        nextEast = GeoUtils.nextEasterlyPoint(points, northIndex);
        nextWest = GeoUtils.nextWesterlyPoint(points, northIndex);

        Coordinate directionCheck = points.get(northIndex - 1);

        isClockwise = directionCheck.equals(nextEast) ? false : true;

        index = theIndex;
        theOtherIntersections = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.index;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InnerBoundary other = (InnerBoundary) obj;
        if (this.index != other.index) {
            return false;
        }
        return true;
    }

    public List<Coordinate> getPoints() {
        return points;
    }

    public int getNorthIndex() {
        return northIndex;
    }

    public Coordinate getNextEast() {
        return nextEast;
    }

    public Coordinate getNextWest() {
        return nextWest;
    }

    public boolean isIsClockwise() {
        return isClockwise;
    }

    // precon - first and last are in the points list
    public List<Coordinate> getSouthPoints() {
        List<Coordinate> retVal = new ArrayList<>();
        int noOfSegments = getPoints().size() - 1;
        int startIndex = noOfSegments - 1;

        if (isIsClockwise()) {
            // TODO - deal with boundary case
            if (northIndex > 1) {
                for (int i = northIndex - 3; i > 0; --i) {
                    Coordinate segmentEnd = getPoints().get(i + 1);
                    retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
                }
            }

            if (northIndex == 1) {
                startIndex = noOfSegments - 2;
            }

            if (northIndex == 0) {
                startIndex = noOfSegments - 3;
            }

            for (int i = startIndex; i > northIndex; --i) {
                Coordinate segmentEnd = getPoints().get(i + 1);
                retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            if (northIndex > 1) {
                for (int i = northIndex + 2; i < getPoints().size(); ++i) {
                    Coordinate segmentEnd = getPoints().get(i);
                    retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
                }
            }

            // start point is same as end pont so skip itr
            for (int i = 1; i < northIndex - 1; ++i) {
                Coordinate segmentEnd = getPoints().get(i);
                retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        }

        return retVal;
    }

    void addEast(Intersection east) {
        theEastIntersection = east;
    }

    void addWest(Intersection west) {
        theWestIntersection = west;
    }

    List<Coordinate> getTopPoints() {
        // could double check that next west etc
        // is the same as intersection start 
        List<Coordinate> retVal = new ArrayList<>();

        // for the intersection 
        // go north until you meet another intersection
        // maybe could calculate a list of end points for all boundaries??
        // in the first instance just work with those that have an outer intersect

        retVal.add(theEastIntersection.startPt);
        retVal.add(nextEast);
        retVal.add(points.get(northIndex));
        retVal.add(nextWest);
        retVal.add(theWestIntersection.endPt);

        if (theWestIntersection.outer != null) {
            Intersection nextIntersection = outer.getNextIntersection(theWestIntersection);

            if (nextIntersection != null) {
                int i = theWestIntersection.endIndex;
                List<Coordinate> outerPoints = outer.getPoints();

                if (theWestIntersection.endIndex < nextIntersection.endIndex) {
                    while (i < nextIntersection.endIndex) {
                        retVal.add(outerPoints.get(i + 1));
                        ++i;
                    }

                    retVal.add(nextIntersection.endPt);
                } else {
                    while (i < outerPoints.size() - 1) {
                        retVal.add(outerPoints.get(i + 1));
                        ++i;
                    }

                    i = 0;
                    while (i < nextIntersection.endIndex) {
                        retVal.add(outerPoints.get(i + 1));
                        ++i;
                    }

                    retVal.add(nextIntersection.endPt);
                }

                retVal.add(nextIntersection.startPt);

                // I think that this must be true??
                InnerBoundary nextInner = nextIntersection.mainInner;
                if (nextInner.equals(theEastIntersection.otherInner)) {
                    List<Coordinate> pointsTo = nextInner.getPointsBetween(nextIntersection.startPt, theEastIntersection.endPt, theEastIntersection.endIndex);

                    // want to go from nextIntersection start pt
                    // to eastIntersection end pt
                    // both should be on the same inner??
                    for (Coordinate thePoint : pointsTo) {
                        retVal.add(thePoint);
                    }
                }
                // if the next intersection inner is this then we're finished
            }
        }

        retVal.add(theEastIntersection.endPt);
        retVal.add(theEastIntersection.startPt);

        return retVal;
    }

    void addOtherIntersection(Intersection theIntersection) {
        theOtherIntersections.add(theIntersection);
    }

    // assuming that inner is anticlockwise
    // TODO - fix so that it can be clockwise
    // precon start and end mut be on this boundary
    private List<Coordinate> getPointsBetween(Coordinate startPt, Coordinate endPt, int testEndPtIndex) {
        int startPtIndex = GeoUtils.findIntersectSegmentIndex(startPt, points);
        int endPtIndex = GeoUtils.findIntersectSegmentIndex(endPt, points);

        List<Coordinate> retVal = new ArrayList<>();
        int startIndex = northIndex + 1;
        // double check that startPt is at this index
        // and that end pt is at the same index

        // TODO - need to handle the opposiet way round

        for (int i = startIndex; i <= testEndPtIndex; ++i) {
            retVal.add(points.get(i));
        }

        return retVal;
    }

    List<Coordinate> getBottomPoints() {
        // could double check that next west etc
        // is the same as intersection start 
        List<Coordinate> retVal = new ArrayList<>();

        // for the intersection 
        // go north until you meet another intersection
        // maybe could calculate a list of end points for all boundaries??
        // in the first instance just work with those that have an outer intersect

        retVal.add(theEastIntersection.startPt);
        retVal.add(theEastIntersection.endPt);
        
        if (theEastIntersection.outer != null) {
            Intersection prevIntersection = outer.getPrevIntersection(theEastIntersection);

            if (prevIntersection != null) {
                int i = theEastIntersection.endIndex;
                List<Coordinate> outerPoints = outer.getPoints();

                if (theEastIntersection.endIndex < prevIntersection.endIndex) {
                    while (i < prevIntersection.endIndex) {
                        retVal.add(outerPoints.get(i + 1));
                        ++i;
                    }

                    retVal.add(prevIntersection.endPt);
                } else {
                    while (i < outerPoints.size() - 1) {
                        retVal.add(outerPoints.get(i + 1));
                        ++i;
                    }

                    i = 0;
                    while (i < prevIntersection.endIndex) {
                        retVal.add(outerPoints.get(i + 1));
                        ++i;
                    }

                    retVal.add(prevIntersection.endPt);
                }

                retVal.add(prevIntersection.startPt);

                // I think that this must be true??
                InnerBoundary nextInner = prevIntersection.mainInner;
                if (nextInner.equals(theWestIntersection.otherInner)) {
                    List<Coordinate> pointsTo = nextInner.getPointsBetween(prevIntersection.startPt, theEastIntersection.endPt, theEastIntersection.endIndex);

                    // want to go from nextIntersection start pt
                    // to eastIntersection end pt
                    // both should be on the same inner??
                    for (Coordinate thePoint : pointsTo) {
                        retVal.add(thePoint);
                    }
                }
                // if the next intersection inner is this then we're finished
            }
        }
        
        retVal.add(theWestIntersection.endPt);

        retVal.add(nextWest);
        
        List<Coordinate> southPoints = getSouthPoints();
        for(Coordinate thePoint: southPoints){
            retVal.add(thePoint);
        }
        
        retVal.add(nextEast);
        
        retVal.add(theEastIntersection.startPt);

        return retVal;
    }
}
