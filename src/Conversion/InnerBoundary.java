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

                outer.followFromTo(theWestIntersection.endIndex,
                        nextIntersection.endIndex,
                        retVal,
                        nextIntersection.endPt,
                        nextIntersection.endPt);

//                if (theWestIntersection.endIndex < nextIntersection.endIndex) {
//                    while (i < nextIntersection.endIndex) {
//                        retVal.add(outerPoints.get(i + 1));
//                        ++i;
//                    }
//
//                    retVal.add(nextIntersection.endPt);
//                } else {
//                    while (i < outerPoints.size() - 1) {
//                        retVal.add(outerPoints.get(i + 1));
//                        ++i;
//                    }
//
//                    i = 0;
//                    while (i < nextIntersection.endIndex) {
//                        retVal.add(outerPoints.get(i + 1));
//                        ++i;
//                    }
//
//                    retVal.add(nextIntersection.endPt);
//                }

                retVal.add(nextIntersection.startPt);

                // I think that this must be true??
                InnerBoundary nextInner = nextIntersection.mainInner;
                if (nextInner.equals(theEastIntersection.otherInner)) {
                    if (getNorthIndex() == 19 || getNorthIndex() == 0) {
                        List<Coordinate> pointsTo = nextInner.getPointsBetween(nextIntersection.startPt, theEastIntersection.endPt, theEastIntersection.endIndex, false);

                        // want to go from nextIntersection start pt
                        // to eastIntersection end pt
                        // both should be on the same inner??
                        for (Coordinate thePoint : pointsTo) {
                            retVal.add(thePoint);
                        }
                    } else {
                        List<Coordinate> pointsTo = nextInner.getPointsBetween(nextIntersection.startPt, theEastIntersection.endPt, theEastIntersection.endIndex, true);

                        // want to go from nextIntersection start pt
                        // to eastIntersection end pt
                        // both should be on the same inner??
                        for (int j = pointsTo.size() - 1; j >= 0; --j) {
                            Coordinate thePoint = pointsTo.get(j);
                            retVal.add(thePoint);
                        }
                    }
                }
                // if the next intersection inner is this then we're finished
            }
        }

        retVal.add(theEastIntersection.endPt);
        retVal.add(theEastIntersection.startPt);

        List<Integer> theNextWestIndex = new ArrayList<>();

//        if (nextEastIntersection != null) {
        for (int i = 0; i < retVal.size(); ++i) {
            if (retVal.get(i).equals(theEastIntersection.endPt)) {
                theNextWestIndex.add(i);
                Coordinate theNext = retVal.get(i + 1);
                Coordinate thePrev = retVal.get(i - 1);
                int x = 0;
            }
        }
//        }

        return retVal;
    }

    void addOtherIntersection(Intersection theIntersection) {
        theOtherIntersections.add(theIntersection);
    }

    // assuming that inner is anticlockwise
    // TODO - fix so that it can be clockwise
    // precon start and end mut be on this boundary
    private List<Coordinate> getPointsBetween(Coordinate startPt, Coordinate endPt, int testEndPtIndex, boolean clockwise) {
        int startPtIndex = GeoUtils.findIntersectSegmentIndex(startPt, points);
        int endPtIndex = GeoUtils.findIntersectSegmentIndex(endPt, points);

        List<Coordinate> retVal = new ArrayList<>();
//        int startIndex = northIndex + 1;
        // double check that startPt is at this index
        // and that end pt is at the same index

        // TODO - need to handle the opposiet way round
        // and the fact that the boundary is a closed polygon
        // but implemented as a list
        if (clockwise) {  // and this is anti
            int firstPartStart = Math.min(startPtIndex, endPtIndex);
            int secondPartEnd = Math.max(startPtIndex, endPtIndex);

//            retVal.add(startPt);

            for (int i = firstPartStart; i >= 0; --i) {
                retVal.add(points.get(i));
            }

            for (int i = points.size() - 1; i > secondPartEnd; --i) {
                retVal.add(points.get(i));
            }
        } else {
//            retVal.add(startPt);
            int firstPartStart = Math.min(startPtIndex, endPtIndex);
            int secondPartEnd = Math.max(startPtIndex, endPtIndex);

            for (int i = firstPartStart + 1; i <= secondPartEnd; ++i) {
                retVal.add(points.get(i));
            }
        }

        return retVal;
    }

    // get the points from the next intersection on the outer back 
    // to this.
    void followEastGoingIntersections(InnerBoundary innerForNextIntersection, List<Coordinate> pointList) {
        List<Coordinate> pointsTo = innerForNextIntersection.getSouthPoints(false);
        for (Coordinate thePoint : pointsTo) {
            pointList.add(thePoint);
        }

        Intersection nextEastIntersection = innerForNextIntersection.theEastIntersection;
        // I'm expecting that we have added east . start pt
        pointList.add(nextEastIntersection.startPt);
        pointList.add(nextEastIntersection.endPt);

        InnerBoundary nextEastInner = nextEastIntersection.otherInner;

        if (nextEastInner != null && !(nextEastInner.equals(this))) {
            List<Coordinate> pointsBetween = nextEastInner.getPointsBetween(nextEastIntersection.endPt, nextEastInner.nextEast, 56, false);
            //            List<Coordinate> pointsTo = eastInner.getSouthPoints(false);
//                        for (int j = pointsBetween.size() - 1; j >= 0; --j) {
//                            Coordinate thePoint = pointsBetween.get(j);
            for (Coordinate thePoint : pointsBetween) {
                pointList.add(thePoint);
            }

            Intersection nextEastEast = nextEastInner.theEastIntersection;

            pointList.add(nextEastEast.startPt);
            pointList.add(nextEastEast.endPt);
            
           InnerBoundary nextNextEastInner = nextEastEast.otherInner;

            if (nextNextEastInner != null && !(nextNextEastInner.equals(this))) {
                List<Coordinate> pointsBetween2 = nextNextEastInner.getPointsBetween(nextEastEast.endPt, nextNextEastInner.nextEast, 56, false);
                //            List<Coordinate> pointsTo = eastInner.getSouthPoints(false);
    //                        for (int j = pointsBetween.size() - 1; j >= 0; --j) {
    //                            Coordinate thePoint = pointsBetween.get(j);
                for (Coordinate thePoint : pointsBetween2) {
                    pointList.add(thePoint);
                }

                Intersection nextEastEastEast = nextNextEastInner.theEastIntersection;

                pointList.add(nextEastEastEast.startPt);
                pointList.add(nextEastEastEast.endPt);
            }
        }

//                    List<Coordinate> southPoints = getSouthPoints(false);
        Coordinate lastAddedPoint = pointList.get(pointList.size() - 1);
        List<Coordinate> southPoints = this.getPointsBetween(lastAddedPoint, nextEast, 56, true);

        for (int j = southPoints.size() - 1; j >= 0; --j) {
            Coordinate thePoint = southPoints.get(j);
            pointList.add(thePoint);
        }
    }

    boolean followWestGoingIntersections(InnerBoundary innerForNextIntersection,
            List<Coordinate> pointList,
            Intersection prevIntersection) {
        boolean hasGeneratedSouthPoints = false;

        if (innerForNextIntersection == this) {
            // end point should actually be the next norherly intersection that hits this
            // or nextEast

            List<Coordinate> pointsBetween = this.getPointsBetween(prevIntersection.endPt, this.nextEast, 56, true);
            int i = 0;
            for (Coordinate thePoint : pointsBetween) {
//                if(i != 0){
                pointList.add(thePoint);
//                }
                ++i;
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
                // end point should actually be the next norherly intersection that hits this
                // or nextEast

                List<Coordinate> pointsBetween = this.getPointsBetween(innerForNextIntersection.theWestIntersection.endPt, this.nextEast, 56, false);
                for (Coordinate thePoint : pointsBetween) {
                    pointList.add(thePoint);
                }

                return true;
            }            // need round trip to itself west
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
        // could double check that next west etc
        // is the same as intersection start 
        List<Coordinate> retVal = new ArrayList<>();
        if (theEastIntersection.outer == null) {
            return retVal;
        }

        Intersection nextEastIntersection = null;
        boolean hasGeneratedSouthPoints = false;

        // for the intersection 
        // go north until you meet another intersection
        // maybe could calculate a list of end points for all boundaries??
        // in the first instance just work with those that have an outer intersect

//        retVal.add(theEastIntersection.startPt);
        retVal.add(theEastIntersection.endPt);

        Intersection prevIntersection = outer.getNextIntersection(theEastIntersection);

        if (prevIntersection != null) {
            Intersection nextIntersection = outer.getNextIntersection(theEastIntersection);

            outer.followFromTo(theEastIntersection.endIndex,
                nextIntersection.endIndex,
                retVal,
                nextIntersection.endPt,
                nextIntersection.endPt);
            
//                hasGeneratedSouthPoints = fromPrevToHere(prevIntersection, retVal);

//                retVal.add(prevIntersection.startPt);

            // if prev intersection is connected to this then were finishsed
            // otherrwise 
            // go round south points to nextEast I think that this must be true??
            InnerBoundary prevInner = prevIntersection.mainInner;

            if (isSoutheasternmost) {
                if (!(prevInner.equals(this))) {
//                    hasGeneratedSouthPoints = fromPrevToHere(prevIntersection, retVal);
                    followEastGoingIntersections(prevInner, retVal);

                    hasGeneratedSouthPoints = true;
                }
            } else {
                hasGeneratedSouthPoints = followWestGoingIntersections(prevInner, retVal, prevIntersection);
            }
        }


        if (!hasGeneratedSouthPoints) {
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

                // this is OK because we are going from nextWest round to nextEast
                // not true
                // should be changed to get points between - best to do that anyway
                List<Coordinate> pointsBetween = eastInner.getPointsBetween(eastPoint, eastInner.nextEast, 56, true);
//            List<Coordinate> pointsTo = eastInner.getSouthPoints(false);
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

                // get points from intersection end point
                // until next waet                             
                hasGeneratedSouthPoints = true;
                List<Coordinate> pointsBetween = this.getPointsBetween(eastPoint, nextEast, 56, true);

                // want to go from nextIntersection start pt
                // to eastIntersection end pt
                // both should be on the same inner??
                for (int j = pointsBetween.size() - 1; j >= 0; --j) {
                    Coordinate thePoint = pointsBetween.get(j);
                    pointsList.add(thePoint);
                }
            }
        } else {
        }

        return hasGeneratedSouthPoints;
    }

    boolean followEastBackHome(Intersection nextIntersection, List<Coordinate> pointList) {
        // going east now
        boolean hasGeneratedSouthPoints = false;

        InnerBoundary theNextInner = nextIntersection.mainInner;

        if (!theNextInner.equals(this)) {
            // find next intersection for this
            int endIndex = -1;
            Intersection fromThis = null;
            for (Intersection otherIntersection : theNextInner.theOtherIntersections) {
                if (otherIntersection.mainInner == this) {
                    endIndex = GeoUtils.findIntersectSegmentIndex(otherIntersection.endPt, theNextInner.getPoints());
                    // cannot be -1 anymore
                    fromThis = otherIntersection;
                }
            }

            Intersection nextEastIntersection = theNextInner.theEastIntersection;

            if (endIndex == -1) {
                List<Coordinate> pointsTo = theNextInner.getSouthPoints(false);
                for (Coordinate thePoint : pointsTo) {
                    pointList.add(thePoint);
                }

                pointList.add(nextEastIntersection.startPt);
                pointList.add(nextEastIntersection.endPt);
            } else {
                List<Coordinate> pointsTo = theNextInner.getPointsBetween(theNextInner.getNextWest(), fromThis.endPt, 0, true);

                for (int j = pointsTo.size() - 1; j >= 0; --j) {
                    pointList.add(pointsTo.get(j));
                }

                pointList.add(fromThis.endPt);
                pointList.add(fromThis.startPt);
            }

            InnerBoundary theNextNextInner = nextEastIntersection.otherInner;

            if (theNextNextInner != null) {
                if (theNextNextInner.equals(this)) {
                    // get points from intersection end point
                    // until next waet                             
                    hasGeneratedSouthPoints = true;
                    List<Coordinate> pointsBetween = this.getPointsBetween(nextEastIntersection.endPt, nextWest, 56, false);

                    // want to go from nextIntersection start pt
                    // to eastIntersection end pt
                    // both should be on the same inner??
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }
                } else {
                    // add the west intersection points
                    List<Coordinate> pointsForNextInner = theNextNextInner.getSouthPoints(false);
                    for (Coordinate thePoint : pointsForNextInner) {
                        pointList.add(thePoint);
                    }
                    // add the east intersection points

                    Intersection nextNextEastIntersection = theNextNextInner.theEastIntersection;
                    pointList.add(nextNextEastIntersection.startPt);
                    pointList.add(nextNextEastIntersection.endPt);

                    InnerBoundary theNextNextNextInner = nextNextEastIntersection.otherInner;

                    if (theNextNextNextInner != null) {
                        if (theNextNextNextInner.equals(this)) {
                            // get points from intersection end point
                            // until next waet                             
                            hasGeneratedSouthPoints = true;
                            List<Coordinate> pointsBetween = this.getPointsBetween(nextNextEastIntersection.endPt, nextWest, 56, false);

                            // want to go from nextIntersection start pt
                            // to eastIntersection end pt
                            // both should be on the same inner??
                            for (Coordinate thePoint : pointsBetween) {
                                pointList.add(thePoint);
                            }
                        } else {
                            List<Coordinate> pointsForOtherInner = theNextNextNextInner.getSouthPoints(false);
                            for (Coordinate thePoint : pointsForOtherInner) {
                                pointList.add(thePoint);
                            }
                            // add the east intersection points

                            Intersection nextNextNextEastIntersection = theNextNextNextInner.theEastIntersection;
                            pointList.add(nextNextNextEastIntersection.startPt);
                            pointList.add(nextNextNextEastIntersection.endPt);

                            InnerBoundary theNextNextNextNextInner = nextNextNextEastIntersection.otherInner;
    
                            if (theNextNextNextNextInner != null) {
                                if (theNextNextNextNextInner.equals(this)) {
                                    // get points from intersection end point
                                    // until next waet                             
                                    hasGeneratedSouthPoints = true;
                                    List<Coordinate> pointsBetween = this.getPointsBetween(nextNextNextEastIntersection.endPt, nextWest, 56, false);

                                    // want to go from nextIntersection start pt
                                    // to eastIntersection end pt
                                    // both should be on the same inner??
                                    for (Coordinate thePoint : pointsBetween) {
                                        pointList.add(thePoint);
                                    }
                                }
                            }   
                        }
                    }
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
}
