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

    void getTopInitialWest(List<Coordinate> pointList) {
        pointList.add(theEastIntersection.startPt);
        pointList.add(nextEast);
        pointList.add(points.get(northIndex));
        pointList.add(nextWest);
        pointList.add(theWestIntersection.endPt);
    }

    Intersection getTopWestOuter(List<Coordinate> pointList) {
        Intersection nextIntersection = outer.getNextIntersection(theWestIntersection);

        if (nextIntersection != null) {
            int i = theWestIntersection.endIndex;
            outer.followFromTo(theWestIntersection.endIndex,
                    nextIntersection.endIndex,
                    pointList,
                    nextIntersection.endPt,
                    nextIntersection.endPt);
        }

        return nextIntersection;
    }

    Intersection getTopEast(Intersection nextIntersection, List<Coordinate> pointList) {
        Intersection topEastOuterIntersection = null;
        pointList.add(nextIntersection.startPt);

        InnerBoundary innerForNextIntersection = nextIntersection.mainInner;
        Intersection nextEastIntersection = innerForNextIntersection.theEastIntersection;

        if (theEastIntersection.otherInner == nextEastIntersection.mainInner) {
            List<Coordinate> pointsTo = innerForNextIntersection.getPointsBetween(innerForNextIntersection.nextWest, theEastIntersection.endPt, false);
            for (Coordinate thePoint : pointsTo) {
                pointList.add(thePoint);
            }
            pointList.add(theEastIntersection.endPt);
            pointList.add(theEastIntersection.startPt);
            return null;
        } else {
            List<Coordinate> pointsTo = innerForNextIntersection.getSouthPoints(false);
            for (Coordinate thePoint : pointsTo) {
                pointList.add(thePoint);
            }

            pointList.add(nextEastIntersection.startPt);
            pointList.add(nextEastIntersection.endPt);
        }

        if (nextEastIntersection.outer != null) {
            topEastOuterIntersection = nextEastIntersection;
        } else {
            pointList.add(nextEastIntersection.startPt);

            InnerBoundary innerForNextEastIntersection = nextEastIntersection.otherInner;
            Intersection nextNextEastIntersection = innerForNextEastIntersection.theEastIntersection;

            if (theEastIntersection.otherInner == nextNextEastIntersection.mainInner) {
                List<Coordinate> pointsTo = innerForNextIntersection.getPointsBetween(innerForNextEastIntersection.nextWest, theEastIntersection.endPt, false);
                for (Coordinate thePoint : pointsTo) {
                    pointList.add(thePoint);
                }
                pointList.add(theEastIntersection.endPt);
                pointList.add(theEastIntersection.startPt);
                return null;
            } else {
                List<Coordinate> pointsToNext = innerForNextEastIntersection.getPointsBetween(nextEastIntersection.endPt, innerForNextEastIntersection.nextEast, false);

                for (Coordinate thePoint : pointsToNext) {
                    pointList.add(thePoint);
                }

                pointList.add(nextNextEastIntersection.startPt);
                pointList.add(nextNextEastIntersection.endPt);
            }

            if (nextNextEastIntersection.outer != null) {
                topEastOuterIntersection = nextNextEastIntersection;
            } else {
                pointList.add(nextNextEastIntersection.startPt);

                InnerBoundary innerForNextNextEastIntersection = nextNextEastIntersection.otherInner;
                Intersection nextNextNextEastIntersection = innerForNextNextEastIntersection.theEastIntersection;

                if (theEastIntersection.otherInner == nextNextNextEastIntersection.mainInner) {
                    List<Coordinate> pointsTo = innerForNextNextEastIntersection.getPointsBetween(nextNextNextEastIntersection.endPt, theEastIntersection.endPt, false);
                    for (Coordinate thePoint : pointsTo) {
                        pointList.add(thePoint);
                    }
                    pointList.add(theEastIntersection.endPt);
                    pointList.add(theEastIntersection.startPt);
                    return null;
                } else {
                    List<Coordinate> pointsToNextNext = innerForNextNextEastIntersection.getPointsBetween(nextNextEastIntersection.endPt, innerForNextNextEastIntersection.nextEast, false);
//                    List<Coordinate> pointsToNextNext = innerForNextNextEastIntersection.getSouthPoints(false);
                    for (Coordinate thePoint : pointsToNextNext) {
                        pointList.add(thePoint);
                    }

                    pointList.add(nextNextNextEastIntersection.startPt);
                    pointList.add(nextNextNextEastIntersection.endPt);
                }

                if (nextNextNextEastIntersection.outer != null) {
                    topEastOuterIntersection = nextNextNextEastIntersection;
                } else {
                    return null; // should never get here
                }
            }
        }

        return topEastOuterIntersection;
    }

    Intersection getTopEastOuter(Intersection outerEast, List<Coordinate> pointList) {
        Intersection nextIntersection = outer.getNextIntersection(outerEast);

        if (nextIntersection != null) {
            outer.followFromTo(outerEast.endIndex,
                    nextIntersection.endIndex,
                    pointList,
                    nextIntersection.endPt,
                    nextIntersection.endPt);
        }

        return nextIntersection;
    }

    void getTopWestBackHome(Intersection nextOuterIntersection, List<Coordinate> pointList) {
        pointList.add(nextOuterIntersection.startPt);

        InnerBoundary nextWestInner = nextOuterIntersection.mainInner;
        Intersection nextWestIntersection = nextWestInner.theWestIntersection;

        List<Intersection> otherIntersections = nextWestInner.theOtherIntersections;

        int otherIndex = -1;
        Intersection theOtherIntersection = null;

        for (Intersection otherIntersection : otherIntersections) {
//            if (!otherIntersection.isEast) {
                otherIndex = otherIntersection.endIndex;
                theOtherIntersection = otherIntersection;
//            }
        }

        List<Coordinate> pointsToNext = nextWestInner.getSouthPoints(true);


        if (theOtherIntersection != null) {
            pointsToNext = nextWestInner.getPointsBetween(nextWestInner.getNextWest(), theOtherIntersection.endPt, false);
            for (Coordinate thePoint : pointsToNext) {
                pointList.add(thePoint);
            }

            // TODO - this should finish when the inner equals theEastIntersection.otherInner
            if(theEastIntersection.otherInner != nextWestInner){
                pointList.add(theOtherIntersection.endPt);
                pointList.add(theOtherIntersection.startPt);
                pointList.add(theOtherIntersection.mainInner.nextWest);
                List<Coordinate> pointsToNextAgain = theOtherIntersection.mainInner.getPointsBetween(theOtherIntersection.mainInner.nextWest, theEastIntersection.endPt, false);
                for (Coordinate thePoint : pointsToNextAgain) {
                    pointList.add(thePoint);
                }
            }
            
            pointList.add(theEastIntersection.endPt);
            pointList.add(theEastIntersection.startPt);
            pointList.add(nextEast);
        } else {
            for (Coordinate thePoint : pointsToNext) {
                pointList.add(thePoint);
            }

            pointList.add(nextWestIntersection.endPt);
        }
    }

    List<Coordinate> getTopPoints() {
        List<Coordinate> retVal = new ArrayList<>();

        getTopInitialWest(retVal);
        // from the should generate north the west isntersection has to go to outer
        if (theWestIntersection.outer != null) {
            Intersection nextIntersection = getTopWestOuter(retVal);
            
            if(nextIntersection.mainInner != this){
                Intersection topEastIntersection = getTopEast(nextIntersection, retVal);

                if (topEastIntersection != null) {
                    Intersection nextOuterIntersection = getTopEastOuter(topEastIntersection, retVal);

                    // going west again - well tending that way anyway
                    getTopWestBackHome(nextOuterIntersection, retVal);
                    
                    retVal.add(theEastIntersection.endPt);
                    retVal.add(theEastIntersection.startPt);
                } else {
                    retVal.add(nextEast);
                }
            }
        }

//        retVal.add(theEastIntersection.endPt);
//        retVal.add(theEastIntersection.startPt);

        List<Integer> theNextWestIndex = new ArrayList<>();

        // debug code
        for (int i = 0; i < retVal.size(); ++i) {
            if (retVal.get(i).equals(nextWest)) {
                theNextWestIndex.add(i);
                Coordinate theNext = retVal.get(i + 1);
                Coordinate thePrev = retVal.get(i - 1);
                int x = 0;
            }
        }

        return retVal;
    }

    void addOtherIntersection(Intersection theIntersection) {
        theOtherIntersections.add(theIntersection);
    }

    // assuming that inner is anticlockwise
    // TODO - fix so that it can be clockwise
    // precon start and end mut be on this boundary
    private List<Coordinate> getPointsBetween(Coordinate startPt, Coordinate endPt, boolean clockwise) {
        int startPtIndex = GeoUtils.findIntersectSegmentIndex(startPt, points);
        int endPtIndex = GeoUtils.findIntersectSegmentIndex(endPt, points);

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

    // get the points from the next intersection on the outer back 
    // to this.
    void followEastGoingIntersections(InnerBoundary innerForNextIntersection, List<Coordinate> pointList) {
        Intersection nextEastIntersection = innerForNextIntersection.theEastIntersection;

        if (!isSoutheasternmost) {
            List<Coordinate> pointsTo = innerForNextIntersection.getSouthPoints(false);
            for (Coordinate thePoint : pointsTo) {
                pointList.add(thePoint);
            }

            pointList.add(nextEastIntersection.startPt);
            pointList.add(nextEastIntersection.endPt);
        }

        InnerBoundary nextEastInner = nextEastIntersection.otherInner;

        if (nextEastInner != null && !(nextEastInner.equals(this))) {
            List<Coordinate> pointsBetween = nextEastInner.getPointsBetween(nextEastIntersection.endPt, nextEastInner.nextEast, false);
            for (Coordinate thePoint : pointsBetween) {
                pointList.add(thePoint);
            }

            Intersection nextEastEast = nextEastInner.theEastIntersection;

            pointList.add(nextEastEast.startPt);
            pointList.add(nextEastEast.endPt);

            InnerBoundary nextNextEastInner = nextEastEast.otherInner;

            if (nextNextEastInner != null && !(nextNextEastInner.equals(this))) {
                List<Coordinate> pointsBetween2 = nextNextEastInner.getPointsBetween(nextEastEast.endPt, nextNextEastInner.nextEast, false);
                for (Coordinate thePoint : pointsBetween2) {
                    pointList.add(thePoint);
                }

                Intersection nextEastEastEast = nextNextEastInner.theEastIntersection;

                pointList.add(nextEastEastEast.startPt);
                pointList.add(nextEastEastEast.endPt);
            }
        }

        Coordinate lastAddedPoint = pointList.get(pointList.size() - 1);
        List<Coordinate> southPoints = this.getPointsBetween(lastAddedPoint, nextEast, false);

        for (Coordinate thePoint : southPoints) {
            pointList.add(thePoint);
        }
    }

    boolean followWestGoingIntersections(InnerBoundary innerForNextIntersection,
            List<Coordinate> pointList,
            Intersection prevIntersection) {
        boolean hasGeneratedSouthPoints = false;

        if (innerForNextIntersection == this) {
            List<Coordinate> pointsBetween = this.getPointsBetween(prevIntersection.endPt, this.nextEast, false);
            for (Coordinate thePoint : pointsBetween) {
                pointList.add(thePoint);
            }

            return true;
        }

        pointList.add(innerForNextIntersection.nextEast);
        pointList.add(innerForNextIntersection.getPoints().get(innerForNextIntersection.getNorthIndex()));
        pointList.add(innerForNextIntersection.nextWest);

        pointList.add(innerForNextIntersection.theWestIntersection.startPt);
        pointList.add(innerForNextIntersection.theWestIntersection.endPt);

        if (innerForNextIntersection.theWestIntersection.outer == null) {
            InnerBoundary westerlyInner = innerForNextIntersection.theWestIntersection.otherInner;
            Intersection theNextIntersection = outer.getNextIntersection(westerlyInner.theWestIntersection);

            if (westerlyInner == this) {
                List<Coordinate> pointsBetween = this.getPointsBetween(innerForNextIntersection.theWestIntersection.endPt, this.nextEast, false);
                for (Coordinate thePoint : pointsBetween) {
                    pointList.add(thePoint);
                }

                return true;
            } else {
                if (getTheWestIntersection().otherInner == westerlyInner) {
                    List<Coordinate> pointsBetween = westerlyInner.getPointsBetween(innerForNextIntersection.theWestIntersection.endPt, getTheWestIntersection().endPt, false);
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }

                    pointList.add(getTheWestIntersection().endPt);

                    return false;
                } else {
                    List<Coordinate> pointsBetween = westerlyInner.getPointsBetween(innerForNextIntersection.theWestIntersection.endPt, westerlyInner.nextEast, false);
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }

                    pointList.add(westerlyInner.nextEast);
                    pointList.add(westerlyInner.getPoints().get(westerlyInner.getNorthIndex()));
                    pointList.add(westerlyInner.nextWest);

                    pointList.add(westerlyInner.getTheWestIntersection().startPt);
                    pointList.add(westerlyInner.getTheWestIntersection().endPt);

                    Intersection nextIntersection = outer.getNextIntersection(westerlyInner.getTheWestIntersection());

                    outer.followFromTo(westerlyInner.getTheWestIntersection().endIndex,
                            nextIntersection.endIndex,
                            pointList,
                            nextIntersection.endPt,
                            nextIntersection.endPt);

                    hasGeneratedSouthPoints = followEastBackHome(nextIntersection, pointList);
                }
            }

            // need round trip to itself west
        } else {
            Intersection nextIntersection = outer.getNextIntersection(innerForNextIntersection.theWestIntersection);
            List<Coordinate> outerPoints = outer.getPoints();
            int i = innerForNextIntersection.theWestIntersection.endIndex;

            outer.followFromTo(innerForNextIntersection.theWestIntersection.endIndex,
                    nextIntersection.endIndex,
                    pointList,
                    prevIntersection.endPt,
                    nextIntersection.endPt);

            hasGeneratedSouthPoints = followEastBackHome(nextIntersection, pointList);
        }

        return hasGeneratedSouthPoints;
    }

    List<Coordinate> getBottomPoints() {
        List<Coordinate> retVal = new ArrayList<>();
        if (theEastIntersection.outer == null) {
            return retVal;
        }

        boolean hasGeneratedSouthPoints = false;

        retVal.add(theEastIntersection.endPt);

        Intersection nextIntersection = outer.getNextIntersection(theEastIntersection);
//        Intersection prevIntersection = outer.getNextIntersection(theEastIntersection);
//        if (theEastIntersection.endIndex == nextIntersection.endIndex) {
//            
//            if(theEastIntersection.endPt.equals(nextIntersection.endPt)){
//                int x = 0;
//            }
//                
//            nextIntersection = outer.getNextIntersection(nextIntersection);
//        }

        if (nextIntersection != null) {
            outer.followFromTo(theEastIntersection.endIndex,
                    nextIntersection.endIndex,
                    retVal,
                    nextIntersection.endPt,
                    nextIntersection.endPt);

            InnerBoundary nextInner = nextIntersection.mainInner;

            if (!nextIntersection.isEast) {
                retVal.add(nextIntersection.startPt);
                retVal.add(nextInner.nextWest);

                List<Coordinate> southPoints = nextInner.getSouthPoints(false);

                for (Coordinate thePoint : southPoints) {
                    retVal.add(thePoint);
                }

                retVal.add(nextInner.nextEast);
                retVal.add(nextInner.getTheEastIntersection().endPt);

                Intersection nextNextIntersection = outer.getNextIntersection(nextInner.getTheEastIntersection());

                if (nextNextIntersection != null) {
                    outer.followFromTo(nextInner.getTheEastIntersection().endIndex,
                            nextNextIntersection.endIndex,
                            retVal,
                            nextNextIntersection.endPt,
                            nextNextIntersection.endPt);

                    nextInner = nextNextIntersection.mainInner;
                    nextIntersection = nextNextIntersection;
                }
            }

            if (isSoutheasternmost) {
                if (!(nextInner.equals(this))) {
                    followEastGoingIntersections(nextInner, retVal);

                    hasGeneratedSouthPoints = true;
                }
            } else {
                hasGeneratedSouthPoints = followWestGoingIntersections(nextInner, retVal, nextIntersection);
            }
        }


        if (!hasGeneratedSouthPoints) {
            double prevPointLongitude = retVal.get(retVal.size() - 1).getLongitude();
            Coordinate northPoint = points.get(getNorthIndex());

            if (prevPointLongitude > northPoint.getLongitude()) {
                retVal.add(northPoint);
            }

            retVal.add(nextWest);

            List<Coordinate> southPoints = getSouthPoints(false);
            for (Coordinate thePoint : southPoints) {
                retVal.add(thePoint);
            }
        }

        List<Integer> theNextWestIndex = new ArrayList<>();

//        if (nextEastIntersection != null) {
        for (int i = 0; i < retVal.size(); ++i) {
            if (retVal.get(i).equals(nextWest)) {
                theNextWestIndex.add(i);
//                    Coordinate theNext = retVal.get(i + 1);
//                    Coordinate thePrev = retVal.get(i - 1);
                int x = 0;
            }
        }
//        }

        retVal.add(nextEast);

//        retVal.add(theEastIntersection.startPt);

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

    boolean followEastBackHome(Intersection nextIntersection, List<Coordinate> pointList) {
        boolean hasGeneratedSouthPoints = false;

        InnerBoundary theNextInner = nextIntersection.mainInner;

        if (!theNextInner.equals(this)) {
            int endIndex = -1;
            Intersection fromThis = null;

            if (theWestIntersection.otherInner == theNextInner) {
                endIndex = GeoUtils.findIntersectSegmentIndex(theWestIntersection.endPt, theNextInner.getPoints());
                fromThis = theWestIntersection;
            }

            Intersection nextEastIntersection = theNextInner.theEastIntersection;
            InnerBoundary theNextNextInner = nextEastIntersection.otherInner;

            if (endIndex == -1) {
                List<Coordinate> pointsTo = theNextInner.getSouthPoints(false);
                for (Coordinate thePoint : pointsTo) {
                    pointList.add(thePoint);
                }

                pointList.add(nextEastIntersection.startPt);
                pointList.add(nextEastIntersection.endPt);
            } else {
                nextEastIntersection = fromThis;
                theNextNextInner = nextEastIntersection.mainInner;

                List<Coordinate> pointsTo = theNextInner.getPointsBetween(theNextInner.getNextWest(), fromThis.endPt, false);

                for (Coordinate thePoint : pointsTo) {
                    pointList.add(thePoint);
                }

                pointList.add(fromThis.endPt);
                pointList.add(fromThis.startPt);
            }

            if (theNextNextInner != null) {
                if (theNextNextInner.equals(this)) {
                    hasGeneratedSouthPoints = true;
                    List<Coordinate> pointsBetween = this.getPointsBetween(nextEastIntersection.endPt, nextEast, false);
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }
                } else {
                    Intersection nextNextEastIntersection = theNextNextInner.theEastIntersection;

                    List<Coordinate> pointsForNextInner = theNextNextInner.getPointsBetween(nextEastIntersection.endPt, theNextNextInner.nextEast, false);
                    for (Coordinate thePoint : pointsForNextInner) {
                        pointList.add(thePoint);
                    }

                    pointList.add(nextNextEastIntersection.startPt);
                    pointList.add(nextNextEastIntersection.endPt);

                    InnerBoundary theNextNextNextInner = nextNextEastIntersection.otherInner;

                    if (theNextNextNextInner != null) {
                        if (theNextNextNextInner.equals(this)) {
                            hasGeneratedSouthPoints = true;
                            List<Coordinate> pointsBetween = this.getPointsBetween(nextNextEastIntersection.endPt, nextEast, false);

                            for (Coordinate thePoint : pointsBetween) {
                                pointList.add(thePoint);
                            }
                        } else {
                            List<Coordinate> pointsForOtherInner = theNextNextNextInner.getSouthPoints(false);
                            for (Coordinate thePoint : pointsForOtherInner) {
                                pointList.add(thePoint);
                            }

                            Intersection nextNextNextEastIntersection = theNextNextNextInner.theEastIntersection;
                            pointList.add(nextNextNextEastIntersection.startPt);
                            pointList.add(nextNextNextEastIntersection.endPt);

                            InnerBoundary theNextNextNextNextInner = nextNextNextEastIntersection.otherInner;

                            if (theNextNextNextNextInner != null) {
                                if (theNextNextNextNextInner.equals(this)) {
                                    hasGeneratedSouthPoints = true;
                                    List<Coordinate> pointsBetween = this.getPointsBetween(nextNextNextEastIntersection.endPt, nextEast, false);
                                    for (Coordinate thePoint : pointsBetween) {
                                        pointList.add(thePoint);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Intersection nextOuterIntersection = outer.getNextIntersection(nextEastIntersection);
                outer.followFromTo(nextEastIntersection.endIndex,
                        nextOuterIntersection.endIndex,
                        pointList,
                        nextOuterIntersection.endPt,
                        nextOuterIntersection.endPt);
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
}
