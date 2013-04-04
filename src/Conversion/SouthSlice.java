package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author alan
 */
public class SouthSlice implements Slice {

    private OuterBoundary outer;
    private InnerBoundary inner;
    private List<Intersection> outerIntersections;
    private Placemark placemark;
    private OuterIndices outerIndices;
    private List<Coordinate> theBottomPoints;

    SouthSlice(OuterBoundary theOuter, InnerBoundary theMainInner, Placemark thePlacemark) {
        outer = theOuter;
        inner = theMainInner;
        placemark = thePlacemark;
        outerIndices = new OuterIndices();
        theBottomPoints = new ArrayList<>();
    }

    @Override
    public List<Coordinate> getGeneratedPoints() {
        List<Coordinate> thePoints = new ArrayList<>();
        // debug code
//        if(theBottomPoints.isEmpty()){
//        thePoints.addAll(inner.getClosedBoundary());
//            return inner.getClosedBoundary();
//        }
        thePoints.addAll(theBottomPoints);
        
        return thePoints;
    }

    boolean followWestGoingIntersections(InnerBoundary innerForNextIntersection,
            List<Coordinate> pointList,
            Intersection prevIntersection) {
        boolean hasGeneratedSouthPoints = false;

        if (innerForNextIntersection == inner) {
            List<Coordinate> pointsBetween = inner.getPointsBetween(prevIntersection.endPt, inner.getNextEast(), false);
            for (Coordinate thePoint : pointsBetween) {
                pointList.add(thePoint);
            }

            return true;
        }

        pointList.add(innerForNextIntersection.getNextEast());
        pointList.add(innerForNextIntersection.getPoints().get(innerForNextIntersection.getNorthIndex()));
        pointList.add(innerForNextIntersection.getNextWest());

        pointList.add(innerForNextIntersection.getTheWestIntersection().startPt);
        pointList.add(innerForNextIntersection.getTheWestIntersection().endPt);

        if (innerForNextIntersection.getTheWestIntersection().outer == null) {
            InnerBoundary westerlyInner = innerForNextIntersection.getTheWestIntersection().otherInner;
            Intersection theNextIntersection = outer.getNextIntersection(westerlyInner.getTheWestIntersection());

            if (westerlyInner == inner) {
                List<Coordinate> pointsBetween = inner.getPointsBetween(innerForNextIntersection.getTheWestIntersection().endPt, inner.getNextEast(), false);
                for (Coordinate thePoint : pointsBetween) {
                    pointList.add(thePoint);
                }

                return true;
            } else {
                if (inner.getTheWestIntersection().otherInner == westerlyInner) {
                    List<Coordinate> pointsBetween = westerlyInner.getPointsBetween(innerForNextIntersection.getTheWestIntersection().endPt, inner.getTheWestIntersection().endPt, false);
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }

                    pointList.add(inner.getTheWestIntersection().endPt);

                    return false;
                } else {
                    List<Coordinate> pointsBetween = westerlyInner.getPointsBetween(innerForNextIntersection.getTheWestIntersection().endPt, westerlyInner.getNextEast(), false);
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }

                    pointList.add(westerlyInner.getNextEast());
                    pointList.add(westerlyInner.getPoints().get(westerlyInner.getNorthIndex()));
                    pointList.add(westerlyInner.getNextWest());

                    pointList.add(westerlyInner.getTheWestIntersection().startPt);
                    pointList.add(westerlyInner.getTheWestIntersection().endPt);

                    if (westerlyInner.getTheWestIntersection().outer == null) {
                        Intersection nextIntersection = outer.getNextIntersection(westerlyInner.getTheWestIntersection());

                        if (nextIntersection != null) {
                            if (westerlyInner.getTheWestIntersection().endIndex == nextIntersection.endIndex) {
                                pointList.add(westerlyInner.getTheWestIntersection().endPt);
                                pointList.add(nextIntersection.endPt);
                            } else {
                                outer.followFromTo(westerlyInner.getTheWestIntersection().endIndex,
                                        nextIntersection.endIndex,
                                        pointList,
                                        nextIntersection.endPt,
                                        nextIntersection.endPt);
                            }

                            IndexPair outerIndex = new IndexPair(westerlyInner.getTheWestIntersection().endIndex, nextIntersection.endIndex);
                            outerIndices.add(outerIndex);

                            hasGeneratedSouthPoints = followEastBackHome(nextIntersection, pointList);
                        }
                    } else {
                        Intersection nextIntersection = outer.getNextIntersection(westerlyInner.getTheWestIntersection());

                        if (westerlyInner.getTheWestIntersection().endIndex == nextIntersection.endIndex) {
                            pointList.add(westerlyInner.getTheWestIntersection().endPt);
                            pointList.add(nextIntersection.endPt);
                        } else {
                            outer.followFromTo(westerlyInner.getTheWestIntersection().endIndex,
                                    nextIntersection.endIndex,
                                    pointList,
                                    nextIntersection.endPt,
                                    nextIntersection.endPt);
                        }

                        IndexPair outerIndex = new IndexPair(westerlyInner.getTheWestIntersection().endIndex, nextIntersection.endIndex);
                        outerIndices.add(outerIndex);

                        hasGeneratedSouthPoints = followEastBackHome(nextIntersection, pointList);
                    }
                }
            }

            // need round trip to itself west
        } else {
            // need to ensure it has hit the outer first
            Intersection westOuterIntersection = null;
            Intersection nextWestIntersection = innerForNextIntersection.getTheWestIntersection();

            if (nextWestIntersection.outer == null) {
                InnerBoundary nextNextWest = nextWestIntersection.otherInner;
                Intersection nextNextWestIntersection = nextNextWest.getTheWestIntersection();

                pointList.add(nextNextWest.getNextEast());
                pointList.add(nextNextWest.getNorth());
                pointList.add(nextNextWest.getNextWest());

                if (nextNextWestIntersection.outer == null) {
                } else {
                    westOuterIntersection = nextNextWestIntersection;
                }
            } else {
                westOuterIntersection = nextWestIntersection;
            }

            Intersection nextIntersection = outer.getNextIntersection(westOuterIntersection);

            if (westOuterIntersection.endIndex == nextIntersection.endIndex) {
                pointList.add(westOuterIntersection.endPt);
                pointList.add(nextIntersection.endPt);
            } else {
                outer.followFromTo(westOuterIntersection.endIndex,
                        nextIntersection.endIndex,
                        pointList,
                        nextIntersection.endPt,
                        nextIntersection.endPt);
            }

            IndexPair outerIndex = new IndexPair(westOuterIntersection.endIndex, nextIntersection.endIndex);
            outerIndices.add(outerIndex);

            if (nextIntersection.isEast) {
                InnerBoundary nextNextWest = nextIntersection.mainInner;
                Intersection nextNextWestIntersection = nextNextWest.getTheWestIntersection();

                pointList.add(nextNextWest.getNextEast());
                pointList.add(nextNextWest.getNorth());
                pointList.add(nextNextWest.getNextWest());

                pointList.add(nextNextWestIntersection.endPt);

                if (nextNextWestIntersection.outer != null) {
                    nextIntersection = outer.getNextIntersection(nextNextWestIntersection);

                    if (nextNextWestIntersection.endIndex == nextIntersection.endIndex) {
                        pointList.add(nextNextWestIntersection.endPt);
                        pointList.add(nextIntersection.endPt);
                    } else {
                        outer.followFromTo(nextNextWestIntersection.endIndex,
                                nextIntersection.endIndex,
                                pointList,
                                nextIntersection.endPt,
                                nextIntersection.endPt);
                    }

                    IndexPair outerIndex1 = new IndexPair(nextNextWestIntersection.endIndex, nextIntersection.endIndex);
                    outerIndices.add(outerIndex1);
                }
            } else {
//                InnerBoundary nextNextNextWest = nextIntersection.mainInner;
//                Intersection nextNextNextWestIntersection = nextNextNextWest.getTheWestIntersection();  
            }

            hasGeneratedSouthPoints = followEastBackHome(nextIntersection, pointList);
        }

        return hasGeneratedSouthPoints;
    }

    boolean followEastBackHome(Intersection nextIntersection, List<Coordinate> pointList) {
        boolean hasGeneratedSouthPoints = false;
        InnerBoundary theNextInner = nextIntersection.mainInner;

        if (!theNextInner.equals(inner)) {
            int endIndex = -1;
            Intersection fromThis = null;

            if (inner.getTheWestIntersection().otherInner == theNextInner) {
                endIndex = GeoUtils.findIntersectSegmentIndex(inner.getTheWestIntersection().endPt, theNextInner.getPoints());
                fromThis = inner.getTheWestIntersection();
            }

            Intersection nextEastIntersection = theNextInner.getTheEastIntersection();
            InnerBoundary theNextNextInner = nextEastIntersection.otherInner;

            if (endIndex == -1) {
                List<Coordinate> pointsTo = theNextInner.getSouthPoints(false);
                pointList.addAll(pointsTo);
                
                pointList.add(nextEastIntersection.startPt);
                pointList.add(nextEastIntersection.endPt);
            } else {
                nextEastIntersection = fromThis;
                theNextNextInner = nextEastIntersection.mainInner;

                List<Coordinate> pointsTo = theNextInner.getPointsBetween(theNextInner.getNextWest(), fromThis.endPt, false);
                pointList.addAll(pointsTo);

                pointList.add(fromThis.endPt);
                pointList.add(fromThis.startPt);
            }

            if (theNextNextInner != null) {
                if (theNextNextInner.equals(inner)) {
                    hasGeneratedSouthPoints = true;
                    List<Coordinate> pointsBetween = inner.getPointsBetween(nextEastIntersection.endPt, inner.getNextEast(), false);
                    pointList.addAll(pointsBetween);
                } else {
                    if (inner.getTheWestIntersection().otherInner == theNextNextInner) {
                        pointList.add(nextEastIntersection.endPt);
                        List<Coordinate> pointsForNextInner = theNextNextInner.getPointsBetween(nextEastIntersection.endPt, inner.getTheWestIntersection().endPt, false);
                        pointList.addAll(pointsForNextInner);
                        pointList.add(inner.getTheWestIntersection().endPt);
                        pointList.add(inner.getTheWestIntersection().startPt);
                        return false;
                    } 
                    
                    List<Intersection> otherIntersections = theNextNextInner.getTheOtherIntersections();

                    Intersection nextNextEastIntersection = theNextNextInner.getTheEastIntersection();                   
                    for(Intersection theOtherIntersection :otherIntersections){
                        if(!theOtherIntersection.isEast){
                            if(nextNextEastIntersection.startPt.getLatitude() > theOtherIntersection.startPt.getLatitude()){
                                nextNextEastIntersection = theOtherIntersection; 
                            }
                        }
                    }

                    List<Coordinate> pointsForNextInner = theNextNextInner.getPointsBetween(nextEastIntersection.endPt, nextNextEastIntersection.endPt, false);
                    pointList.addAll(pointsForNextInner);
                    
                    pointList.add(nextNextEastIntersection.startPt);
                    InnerBoundary theNextNextMainInner = nextNextEastIntersection.mainInner;
                    
                    List<Coordinate> pointsForNextNextInner = theNextNextMainInner.getSouthPoints(false);
                    
                    if(theNextNextMainInner.equals(inner.getTheWestIntersection().otherInner)){
                        pointsForNextNextInner = theNextNextMainInner.getPointsBetween(theNextNextMainInner.getNextWest(), inner.getTheWestIntersection().endPt, false);                        
                        pointList.addAll(pointsForNextNextInner);
                        pointList.add(inner.getTheWestIntersection().endPt);
                        return false;
                    }
                    
                    pointList.addAll(pointsForNextNextInner);

                    InnerBoundary theNextNextNextInner = nextNextEastIntersection.otherInner;

                    if (theNextNextNextInner != null) {
                        if (theNextNextNextInner.equals(inner)) {
                            hasGeneratedSouthPoints = true;
                            List<Coordinate> pointsBetween = inner.getPointsBetween(nextNextEastIntersection.endPt, inner.getNextEast(), false);
                            pointList.addAll(pointsBetween);
                        } else {
                            List<Coordinate> pointsForOtherInner = theNextNextNextInner.getSouthPoints(false);
                            pointList.addAll(pointsForOtherInner);

                            Intersection nextNextNextEastIntersection = theNextNextNextInner.getTheEastIntersection();
                            pointList.add(nextNextNextEastIntersection.startPt);
                            pointList.add(nextNextNextEastIntersection.endPt);

                            InnerBoundary theNextNextNextNextInner = nextNextNextEastIntersection.otherInner;

                            if (theNextNextNextNextInner != null) {
                                if (theNextNextNextNextInner.equals(inner)) {
                                    hasGeneratedSouthPoints = true;
                                    List<Coordinate> pointsBetween = inner.getPointsBetween(nextNextNextEastIntersection.endPt, inner.getNextEast(), false);
                                    pointList.addAll(pointsBetween);
                                }
                            }
                        }
                    }
                }
            } else {
                Intersection nextOuterIntersection = outer.getNextIntersection(nextEastIntersection);

                if (nextEastIntersection.endIndex == nextIntersection.endIndex) {
                    pointList.add(nextEastIntersection.endPt);
                    pointList.add(nextOuterIntersection.endPt);
                } else {
                    outer.followFromTo(nextEastIntersection.endIndex,
                            nextOuterIntersection.endIndex,
                            pointList,
                            nextOuterIntersection.endPt,
                            nextOuterIntersection.endPt);
                }

                IndexPair outerIndex = new IndexPair(nextEastIntersection.endIndex, nextOuterIntersection.endIndex);
                outerIndices.add(outerIndex);
            }
        }

        return hasGeneratedSouthPoints;
    }

    // get the points from the next intersection on the outer back
    // to this.
    void followEastGoingIntersections(InnerBoundary innerForNextIntersection, List<Coordinate> pointList) {
        Intersection nextEastIntersection = innerForNextIntersection.getTheEastIntersection();

        if (!inner.isIsSoutheasternmost()) {
            List<Coordinate> pointsTo = innerForNextIntersection.getSouthPoints(false);
            for (Coordinate thePoint : pointsTo) {
                pointList.add(thePoint);
            }

            pointList.add(nextEastIntersection.startPt);
            pointList.add(nextEastIntersection.endPt);
        }

        InnerBoundary nextEastInner = nextEastIntersection.otherInner;

        if (nextEastInner != null && !(nextEastInner.equals(inner))) {
            List<Coordinate> pointsBetween = nextEastInner.getPointsBetween(nextEastIntersection.endPt, nextEastInner.getNextEast(), false);
            for (Coordinate thePoint : pointsBetween) {
                pointList.add(thePoint);
            }

            Intersection nextEastEast = nextEastInner.getTheEastIntersection();

            pointList.add(nextEastEast.startPt);
            pointList.add(nextEastEast.endPt);

            InnerBoundary nextNextEastInner = nextEastEast.otherInner;

            if (nextNextEastInner != null && !(nextNextEastInner.equals(inner))) {
                List<Coordinate> pointsBetween2 = nextNextEastInner.getPointsBetween(nextEastEast.endPt, nextNextEastInner.getNextEast(), false);
                for (Coordinate thePoint : pointsBetween2) {
                    pointList.add(thePoint);
                }

                Intersection nextEastEastEast = nextNextEastInner.getTheEastIntersection();

                pointList.add(nextEastEastEast.startPt);
                pointList.add(nextEastEastEast.endPt);
            }
        }

        Coordinate lastAddedPoint = pointList.get(pointList.size() - 1);
        List<Coordinate> southPoints = inner.getPointsBetween(lastAddedPoint, inner.getNextEast(), false);

        for (Coordinate thePoint : southPoints) {
            pointList.add(thePoint);
        }
    }

    @Override
    public Placemark getPlacemark() {
        return placemark;
    }

    @Override
    public Polygon getPolygon() {
        return (Polygon) placemark.getGeometry();
    }

    @Override
    public OuterIndices getOuterIndices() {
        return outerIndices;
    }

    @Override
    public void generatePoints() {
//            List<Coordinate> complete = inner.getClosedBoundary();
//
//            for (Coordinate thePoint : complete) {
//                theBottomPoints.add(thePoint);
//            }
        
        if(inner.getIndex() == 15){
            getBottomInitialEast(theBottomPoints);
            return;
        }

        if (inner.getTheEastIntersection().outer == null) {
            InnerBoundary nextEastInner = inner.getTheEastIntersection().otherInner;
            int eastEndIndex = inner.getTheEastIntersection().endIndex;

            if (eastEndIndex == nextEastInner.getNorthIndex()) {
                theBottomPoints.add(inner.getTheEastIntersection().startPt);
                theBottomPoints.add(inner.getTheEastIntersection().endPt);

                theBottomPoints.add(nextEastInner.getNextWest());

                theBottomPoints.add(nextEastInner.getTheWestIntersection().startPt);
                theBottomPoints.add(nextEastInner.getTheWestIntersection().endPt);

                Intersection nextIntersection = outer.getNextIntersection(nextEastInner.getTheWestIntersection());

                if (nextIntersection != null) {
                    if (nextEastInner.getTheWestIntersection().endIndex == nextIntersection.endIndex) {
                        theBottomPoints.add(nextEastInner.getTheWestIntersection().endPt);
                        theBottomPoints.add(nextIntersection.endPt);
                    } else {
                        outer.followFromTo(nextEastInner.getTheWestIntersection().endIndex,
                                nextIntersection.endIndex,
                                theBottomPoints,
                                nextIntersection.endPt,
                                nextIntersection.endPt);
                    }

                    IndexPair outerIndex = new IndexPair(inner.getTheEastIntersection().endIndex, nextIntersection.endIndex);
                    outerIndices.add(outerIndex);

                    if (nextIntersection.mainInner == inner) {
                        theBottomPoints.add(nextIntersection.endPt);
                        theBottomPoints.add(nextIntersection.startPt);

                        List<Coordinate> southPoints = inner.getSouthPoints(false);

                        for (Coordinate thePoint : southPoints) {
                            theBottomPoints.add(thePoint);
                        }
                    }
                }

            }

            return;
        }

        boolean hasGeneratedSouthPoints = false;
        theBottomPoints.add(inner.getTheEastIntersection().startPt);
        theBottomPoints.add(inner.getTheEastIntersection().endPt);
        Intersection nextIntersection = outer.getNextIntersection(inner.getTheEastIntersection());

        if (nextIntersection != null) {
            if (inner.getTheEastIntersection().endIndex == nextIntersection.endIndex) {
                theBottomPoints.add(inner.getTheEastIntersection().endPt);
                theBottomPoints.add(nextIntersection.endPt);
            } else {
                outer.followFromTo(inner.getTheEastIntersection().endIndex,
                        nextIntersection.endIndex,
                        theBottomPoints,
                        nextIntersection.endPt,
                        nextIntersection.endPt);
            }

            IndexPair outerIndex = new IndexPair(inner.getTheEastIntersection().endIndex, nextIntersection.endIndex);
            outerIndices.add(outerIndex);

            InnerBoundary nextInner = nextIntersection.mainInner;

            if (!nextIntersection.isEast) {
                theBottomPoints.add(nextIntersection.startPt);
                theBottomPoints.add(nextInner.getNextWest());
                
                if(nextIntersection.mainInner == inner){
                    List<Coordinate> southPoints = inner.getSouthPoints(false);
                    theBottomPoints.addAll(southPoints);
                    theBottomPoints.add(inner.getNextEast());
                    return;               
                }

                List<Coordinate> southPoints = nextInner.getSouthPoints(false);
                theBottomPoints.addAll(southPoints);

                theBottomPoints.add(nextInner.getNextEast());
                theBottomPoints.add(nextInner.getTheEastIntersection().endPt);

                Intersection nextNextIntersection = outer.getNextIntersection(nextInner.getTheEastIntersection());

                if (nextNextIntersection != null) {
                    if (inner.getTheEastIntersection().endIndex == nextIntersection.endIndex) {
                        theBottomPoints.add(inner.getTheEastIntersection().endPt);
                        theBottomPoints.add(nextNextIntersection.endPt);
                    } else {
                        outer.followFromTo(nextInner.getTheEastIntersection().endIndex,
                                nextNextIntersection.endIndex,
                                theBottomPoints,
                                nextNextIntersection.endPt,
                                nextNextIntersection.endPt);
                    }

                    IndexPair nextOuterIndex = new IndexPair(nextInner.getTheEastIntersection().endIndex, nextNextIntersection.endIndex);
                    outerIndices.add(nextOuterIndex);

                    nextInner = nextNextIntersection.mainInner;
                    nextIntersection = nextNextIntersection;
                }
            }

            if (inner.isIsSoutheasternmost()) {
                if (!(nextInner.equals(inner))) {
                    followEastGoingIntersections(nextInner, theBottomPoints);
                    hasGeneratedSouthPoints = true;
                }
            } else {
                hasGeneratedSouthPoints = followWestGoingIntersections(nextInner, theBottomPoints, nextIntersection);
            }
        }

        if (!hasGeneratedSouthPoints) {
            double prevPointLongitude = theBottomPoints.get(theBottomPoints.size() - 1).getLongitude();
            Coordinate northPoint = inner.getPoints().get(inner.getNorthIndex());

            if (prevPointLongitude > northPoint.getLongitude()) {
                theBottomPoints.add(northPoint);
            }

            theBottomPoints.add(inner.getNextWest());

            List<Coordinate> southPoints = inner.getSouthPoints(false);
            for (Coordinate thePoint : southPoints) {
                theBottomPoints.add(thePoint);
            }
        }

        theBottomPoints.add(inner.getNextEast());
    }

    @Override
    public InnerBoundary getInner() {
        return inner;
    }

    // debug code to help find is a point has been duplicated
    List<Integer> getNextWestIndex() {
        List<Integer> theNextWestIndex = new ArrayList<>();

//        if (nextEastIntersection != null) {
        for (int i = 0; i < theBottomPoints.size(); ++i) {
            if (theBottomPoints.get(i).equals(inner.getNextWest())) {
                theNextWestIndex.add(i);
//                    Coordinate theNext = theBottomPoints.get(i + 1);
//                    Coordinate thePrev = theBottomPoints.get(i - 1);
            }
        }
//        }
        return theNextWestIndex;
    }

    @Override
    public boolean mustBeAdded() {
        return getInner().isIsSoutheasternmost();
    }

    @Override
    public boolean isNorth() {
        return false;
    }
    
    Intersection getBottomInitialEast(List<Coordinate> pointList) {
        Intersection initialEast = inner.getTheEastIntersection();
        pointList.add(inner.getNextEast());

        if (inner.getTheEastIntersection().outer != null) {
            pointList.add(inner.getTheEastIntersection().startPt);
            pointList.add(inner.getTheEastIntersection().endPt);
            
            Intersection nextIntersection = outer.getNextIntersection(inner.getTheEastIntersection());
            
            if(nextIntersection != null){
                if (initialEast.endIndex == nextIntersection.endIndex) {
                    pointList.add(initialEast.endPt);
                    pointList.add(nextIntersection.endPt);
                } else {
                    outer.followFromTo(initialEast.endIndex,
                            nextIntersection.endIndex,
                            pointList,
                            nextIntersection.endPt,
                            nextIntersection.endPt);
                }

                IndexPair outerIndex = new IndexPair(initialEast.endIndex, nextIntersection.endIndex);
                outerIndices.add(outerIndex);   
                
                InnerBoundary nextEastInner = nextIntersection.mainInner;                   
                Intersection innerSouthEasternmost = nextEastInner.getNextSoutheastIntersection();

                List<Coordinate> pointsForNextInner = nextEastInner.getSouthPoints(false);
                
                if(innerSouthEasternmost != null){
                    pointsForNextInner = nextEastInner.getPointsBetween(nextEastInner.getNextWest(), innerSouthEasternmost.endPt, false);
                }
                
                pointList.addAll(pointsForNextInner);
                
                if(innerSouthEasternmost != null){
                    pointList.add(innerSouthEasternmost.endPt);
                    pointList.add(innerSouthEasternmost.startPt); 
                    
                    Intersection nextWestSideIntersection = innerSouthEasternmost.mainInner.getNextOtherIntersection(innerSouthEasternmost, false);       

                    if(nextWestSideIntersection != null){
                        List<Coordinate> pointsBetween = nextWestSideIntersection.otherInner.getPointsBetween(innerSouthEasternmost.startPt, nextWestSideIntersection.endPt, false);
                        pointList.addAll(pointsBetween);
                        pointList.add(nextWestSideIntersection.endPt);
                        pointList.add(nextWestSideIntersection.startPt);
                        
                        InnerBoundary nextNextWestInner = nextWestSideIntersection.mainInner;
                        pointList.add(nextNextWestInner.getNextEast());
                        pointList.add(nextNextWestInner.getNorth());
                        pointList.add(nextNextWestInner.getNextWest());
                        
                        pointList.add(nextNextWestInner.getTheWestIntersection().startPt);
                        pointList.add(nextNextWestInner.getTheWestIntersection().endPt);
                        
                        Intersection finalIntersection = outer.getNextIntersection(nextNextWestInner.getTheWestIntersection());

                        if(nextIntersection != null){
                            if (nextNextWestInner.getTheWestIntersection().endIndex == finalIntersection.endIndex) {
                                pointList.add(nextNextWestInner.getTheWestIntersection().endPt);
                                pointList.add(finalIntersection.endPt);
                            } else {
                                outer.followFromTo(nextNextWestInner.getTheWestIntersection().endIndex,
                                        finalIntersection.endIndex,
                                        pointList,
                                        finalIntersection.endPt,
                                        finalIntersection.endPt);
                            }

                            IndexPair finalOuterIndex = new IndexPair(nextNextWestInner.getTheWestIntersection().endIndex, finalIntersection.endIndex);
                            outerIndices.add(finalOuterIndex);  
                            
                            List<Coordinate> southPoints = inner.getSouthPoints(false);
                            pointList.addAll(southPoints);
                        }
                    }
                } else {
                    pointList.add(nextEastInner.getTheEastIntersection().startPt);
                    pointList.add(nextEastInner.getTheEastIntersection().endPt);                                        
                }
            }
        }
        
        if(true){      
            return null;
        }
        
        Intersection easternmostIntersect = null;
        
        if (initialEast.outer != null){
            // easy case straight to outer with no complications
            if(initialEast.allIntersects.size() == 1){
                pointList.add(initialEast.startPt);
                pointList.add(initialEast.endPt);
                easternmostIntersect = initialEast;
            } else {
                List<BoundaryIntersect> allIntersects = initialEast.allIntersects;
                // find segment index of next 
                // followFromTo
                // could assert that the second is an outer
                // should have been done in initial set up
                BoundaryIntersect closingIntersection = allIntersects.get(1);
                Coordinate nextIntersectPoint = closingIntersection.getIntersectPoint();
     

                BoundaryIntersect nextIntersection = allIntersects.get(2);
                if(nextIntersection.getBoundary().isInner()){
                   pointList.add(nextIntersection.getIntersectPoint());
                   // add from this point to next southerly other intersection pt
                   // then turn to go west or delegate to another function
                } else {
                    if(initialEast.allIntersects.size() == 3){
                        pointList.add(nextIntersection.getIntersectPoint());
                        easternmostIntersect = initialEast;
                    } else {
                        pointList.add(nextIntersection.getIntersectPoint());

                        Coordinate nextNextIntersectPoint = nextIntersection.getIntersectPoint();
                        int nextEndPtIndex = GeoUtils.findIntersectSegmentIndex(nextIntersectPoint, outer.getPoints());
                        Coordinate nextNextNextIntersectPoint = allIntersects.get(3).getIntersectPoint();
                        int nextNextEndPtIndex = GeoUtils.findIntersectSegmentIndex(nextNextNextIntersectPoint, outer.getPoints());
//                        if (nextEndPtIndex == nextNextEndPtIndex) {
//                            pointList.add(nextNextIntersectPoint);
//                            pointList.add(nextNextNextIntersectPoint);
//                        } else {
//                            outer.followFromTo(nextEndPtIndex,
//                                    nextNextEndPtIndex,
//                                    pointList,
//                                    nextNextNextIntersectPoint,
//                                    nextNextNextIntersectPoint);
//                        }
//
//                        IndexPair nextOuterIndex = new IndexPair(nextEndPtIndex, nextNextEndPtIndex);
//                        outerIndices.add(outerIndex);
                    }
                }

                // TDOD - cover more complex cases
                // next intersection from initial east must be with an outer
                // follow outer from first to second
                // next intersection from that is either inner or outer
                // 1) outer 
                // if it is the furthest easternmost 
                // follow from start to end and return new easternmost
                // else repeat above
                // 2) inner 
                // if there is an intersect to that inner that is south (and on the west side) of this
                // folow to that intersect the start going west ... how?
                // else
                // follow inner south until the next intersection for this on the east side
            }
        } else {
            InnerBoundary nextEastInner = inner.getTheEastIntersection().otherInner;
            int eastEndIndex = inner.getTheEastIntersection().endIndex;

            // case where intersect is between north and nextWest
            if (eastEndIndex == nextEastInner.getNorthIndex()) {
                pointList.add(inner.getTheEastIntersection().startPt);
                pointList.add(inner.getTheEastIntersection().endPt);

                pointList.add(nextEastInner.getNextWest());

                pointList.add(nextEastInner.getTheWestIntersection().startPt);
                pointList.add(nextEastInner.getTheWestIntersection().endPt);

                Intersection nextIntersection = outer.getNextIntersection(nextEastInner.getTheWestIntersection());
 
                // not sure that this makes sense 
                // need to double check jan case where this happens to 
                // see exactly what the scenario is
                if (nextIntersection != null) {
                    easternmostIntersect = nextIntersection;                    
                }
            } else {
                // TODO 
            }

        }
        
        return easternmostIntersect;
    }

    Intersection getBottomWestOuter(List<Coordinate> pointList) {
        Intersection nextIntersection = outer.getNextIntersection(inner.getTheWestIntersection());

        if (nextIntersection != null) {
            if (inner.getTheWestIntersection().endIndex == nextIntersection.endIndex) {
                pointList.add(inner.getTheWestIntersection().endPt);
                pointList.add(nextIntersection.endPt);
            } else {
                outer.followFromTo(inner.getTheWestIntersection().endIndex,
                        nextIntersection.endIndex,
                        pointList,
                        nextIntersection.endPt,
                        nextIntersection.endPt);
            }

            IndexPair outerIndex = new IndexPair(inner.getTheWestIntersection().endIndex, nextIntersection.endIndex);
            outerIndices.add(outerIndex);
        }

        return nextIntersection;
    }

    Intersection getBottomEastOuter(Intersection outerEast, List<Coordinate> pointList) {
        Intersection nextIntersection = outer.getNextIntersection(outerEast);

        if (nextIntersection != null) {
            if (outerEast.endIndex == nextIntersection.endIndex) {
                pointList.add(outerEast.endPt);
                pointList.add(nextIntersection.endPt);
            } else {
                outer.followFromTo(outerEast.endIndex,
                        nextIntersection.endIndex,
                        pointList,
                        nextIntersection.endPt,
                        nextIntersection.endPt);
            }

            IndexPair outerIndex = new IndexPair(outerEast.endIndex, nextIntersection.endIndex);
            outerIndices.add(outerIndex);
        }

        return nextIntersection;
    }
    boolean getBottomEastBackHome(Intersection nextOuterIntersection, List<Coordinate> pointList) {
        boolean hasGeneratedSouthPoints = false;

        // cases to cover 
        //1 ) straight back home
        if (nextOuterIntersection.mainInner == inner) {
            pointList.add(nextOuterIntersection.endPt);
            pointList.add(nextOuterIntersection.startPt);

            List<Coordinate> southPoints = inner.getSouthPoints(false);
            pointList.addAll(southPoints);
            hasGeneratedSouthPoints = true;
            return hasGeneratedSouthPoints;
        } 
        
        return hasGeneratedSouthPoints;
    }
}
