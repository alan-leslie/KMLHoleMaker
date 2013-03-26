package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.ArrayList;
import java.util.List;

// TODO - put in invariants that identify this as a boundary
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
    private boolean isSoutheasternmost;
    private final OuterBoundary outer;
    private Intersection theEastIntersection;
    private Intersection theWestIntersection;
    private List<Intersection> theOtherIntersections; // intersections where this boundary is the
    // end point
    private boolean southLoopback;

    InnerBoundary(int theIndex, OuterBoundary theOuter, List<Coordinate> thePoints) {
        outer = theOuter;
        points = thePoints;

        northIndex = GeoUtils.nothernmostIndex(points);

        nextEast = GeoUtils.nextEasterlyPoint(points, northIndex);
        nextWest = GeoUtils.nextWesterlyPoint(points, northIndex);

        Coordinate directionCheck = null;
        if (northIndex > 0) {
            directionCheck = points.get(northIndex - 1);
        } else {
            directionCheck = points.get(points.size() - 2);
        }

        isClockwise = directionCheck.equals(nextEast) ? false : true;

        index = theIndex;
        theOtherIntersections = new ArrayList<>();
        isSoutheasternmost = false;
        southLoopback = false;
    }

    // precon - first and last are in the points list
    public List<Coordinate> getSouthPoints(boolean clockwise) {
        List<Coordinate> retVal = new ArrayList<>();
        int noOfSegments = getPoints().size() - 1;
        int startIndex = noOfSegments - 1;

        if (clockwise) {
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
            if (northIndex == 0) {
                for (int i = northIndex + 2; i < getPoints().size() - 1; ++i) {
                    Coordinate segmentEnd = getPoints().get(i);
                    retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
                }
            } else {
                if (northIndex == 1) {
                    for (int i = northIndex + 2; i < getPoints().size() - 2; ++i) {
                        Coordinate segmentEnd = getPoints().get(i);
                        retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
                    }
                } else {
                    for (int i = northIndex + 2; i < getPoints().size(); ++i) {
                        Coordinate segmentEnd = getPoints().get(i);
                        retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
                    }

                    // start point is same as end pont so skip itr
                    for (int i = 1; i < northIndex - 1; ++i) {
                        Coordinate segmentEnd = getPoints().get(i);
                        retVal.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
                    }
                }
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

    void addOtherIntersection(Intersection theIntersection) {
        theOtherIntersections.add(theIntersection);
    }

    // simplify the boundary by removing any points less than 
    // distance from prev 
    List<Coordinate> getSimplifiedBoundary(double diatance){
        List<Coordinate> retVal = new ArrayList<>();
        
        retVal.add(getNextWest());
        retVal.add(getPoints().get(getNorthIndex()));
        retVal.add(getNextEast());
        
        List<Coordinate> southPoints = getSouthPoints(true);
        
        for(Coordinate thePoint: southPoints){
            retVal.add(thePoint);
        }
        
        retVal.add(getNextWest());
      
        return retVal;
    }
    
    // to help identify this when debugging 
    // show the whole of this boundary
    List<Coordinate> getClosedBoundary(){
        List<Coordinate> retVal = new ArrayList<>();
        
        retVal.add(getNextWest());
        retVal.add(getPoints().get(getNorthIndex()));
        retVal.add(getNextEast());
        
        List<Coordinate> southPoints = getSouthPoints(true);
        
        for(Coordinate thePoint: southPoints){
            retVal.add(thePoint);
        }
       
        return retVal;
    }

    // assuming that inner is anticlockwise
    // TODO - fix so that it can be clockwise
    // precon start and end mut be on this boundary
    List<Coordinate> getPointsBetween(Coordinate startPt, Coordinate endPt, boolean clockwise) {
        int startPtIndex = GeoUtils.findIntersectSegmentIndex(startPt, points);
        int endPtIndex = GeoUtils.findIntersectSegmentIndex(endPt, points);
        
        if(startPtIndex == -1 || endPtIndex == -1){
            int x = 0;
        }

        List<Coordinate> retVal = new ArrayList<>();

        if (clockwise) {
            if (startPtIndex > endPtIndex) {
                for (int i = startPtIndex; i >= endPtIndex; --i) {
                    retVal.add(points.get(i));
                }
            } else {
                for (int i = startPtIndex; i >= 0; --i) {
                    retVal.add(points.get(i));
                }

                for (int i = points.size() - 1; i > endPtIndex; --i) {
                    retVal.add(points.get(i));
                }
            }
        } else {
            if (startPtIndex > endPtIndex) {
                for (int i = startPtIndex; i < points.size() - 1; ++i) {
                    retVal.add(points.get(i));
                }

                for (int i = 0; i < endPtIndex; ++i) {
                    retVal.add(points.get(i));
                }
            } else {
                for (int i = startPtIndex + 1; i <= endPtIndex; ++i) {
                    retVal.add(points.get(i));
                }
            }
        }

        return retVal;
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

    public Intersection getTheEastIntersection() {
        return theEastIntersection;
    }

    public Intersection getTheWestIntersection() {
        return theWestIntersection;
    }

    public void setIsSoutheasternmost(boolean isSoutheasternmost) {
        this.isSoutheasternmost = isSoutheasternmost;
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

    boolean fromPrevToHere(Intersection prevIntersection,
            List<Coordinate> pointsList) {
        boolean hasGeneratedSouthPoints = false;
        InnerBoundary eastInner = prevIntersection.mainInner; // should come from prev intersection??

        if (!prevIntersection.isEast) {
            Intersection nextEastIntersection = prevIntersection;
            Coordinate westPoint = prevIntersection.endPt;
            Coordinate eastPoint = prevIntersection.startPt;

            while (eastInner != this && eastInner != null) {
                pointsList.add(westPoint);
                pointsList.add(eastPoint);

                List<Coordinate> pointsBetween = eastInner.getPointsBetween(eastPoint, eastInner.nextEast, true);
                for (int j = pointsBetween.size() - 1; j >= 0; --j) {
                    Coordinate thePoint = pointsBetween.get(j);
                    pointsList.add(thePoint);
                }

                nextEastIntersection = eastInner.getTheEastIntersection();
                westPoint = nextEastIntersection.startPt;
                eastPoint = nextEastIntersection.endPt;
                eastInner = nextEastIntersection.otherInner;
            }

            if (eastInner.equals(this)) {
                pointsList.add(westPoint);
                pointsList.add(eastPoint);

                hasGeneratedSouthPoints = true;
                List<Coordinate> pointsBetween = this.getPointsBetween(eastPoint, nextEast, true);
                for (int j = pointsBetween.size() - 1; j >= 0; --j) {
                    Coordinate thePoint = pointsBetween.get(j);
                    pointsList.add(thePoint);
                }
            }
        }
        return hasGeneratedSouthPoints;
    }

    public int getIndex() {
        return index;
    }

    public boolean isIsSoutheasternmost() {
        return isSoutheasternmost;
    }

    boolean shouldGenerateNorth() {
        if (getTheEastIntersection().outer != null
                && getTheWestIntersection().outer != null) {
            Intersection nextIntersection = outer.getNextIntersection(getTheWestIntersection());

            if (nextIntersection.equals(getTheEastIntersection())) {
                return true;
            }
        }

        if (getTheEastIntersection().outer == null && getTheWestIntersection().outer != null) {
            if (theOtherIntersections.size() < 2) {
                return true;
            }
        }

        return false;
    }

    List<Intersection> getTheOtherIntersections() {
        return theOtherIntersections;
    }

    boolean GetSouthLoopback() {
        return southLoopback;
    }

    void SetSouthLoopback(boolean b) {
        southLoopback = b;
    }
}
