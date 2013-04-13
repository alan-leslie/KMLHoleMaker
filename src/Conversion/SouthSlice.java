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
        if (theBottomPoints.size() > 0) {
            int theExtremeIndex = getMostSouthIndex();
            Coordinate prevPoint = new Coordinate(theBottomPoints.get(theExtremeIndex).getLongitude() + 1.0, theBottomPoints.get(theExtremeIndex).getLongitude() + 1.0);

            for (int i = theExtremeIndex; i < theBottomPoints.size(); ++i) {
                if (!(prevPoint.equals(theBottomPoints.get(i)))) {
                    thePoints.add(theBottomPoints.get(i));
                }
                prevPoint = theBottomPoints.get(i);
            }

            for (int i = 0; i <= theExtremeIndex; ++i) {
                if (!(prevPoint.equals(theBottomPoints.get(i)))) {
                    thePoints.add(theBottomPoints.get(i));
                }

                prevPoint = theBottomPoints.get(i);
            }
        }

//        thePoints.addAll(theBottomPoints);

        return thePoints;
    }

    boolean followWestGoingIntersections(InnerBoundary innerForNextIntersection,
            List<Coordinate> pointList,
            Intersection prevIntersection) {
        boolean hasGeneratedSouthPoints = false;

        if (innerForNextIntersection == inner) {
            List<Coordinate> pointsBetween = inner.getPointsBetween(prevIntersection.endPt, inner.getNextEast(), false);
            pointList.addAll(pointsBetween);

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
                    pointList.addAll(pointsBetween);
        
                    pointList.add(westerlyInner.getNextEast());
                    pointList.add(westerlyInner.getPoints().get(westerlyInner.getNorthIndex()));
                    pointList.add(westerlyInner.getNextWest());

                    pointList.add(westerlyInner.getTheWestIntersection().startPt);
                    pointList.add(westerlyInner.getTheWestIntersection().endPt);

                    if (westerlyInner.getTheWestIntersection().outer == null) {
                        if (inner.getTheWestIntersection().otherInner == westerlyInner.getTheWestIntersection().otherInner) {
                            InnerBoundary lastInner = inner.getTheWestIntersection().otherInner;
                            List<Coordinate> lastInnerPointsBetween = lastInner.getPointsBetween(westerlyInner.getTheWestIntersection().endPt, inner.getTheWestIntersection().endPt, false);
                            pointList.addAll(lastInnerPointsBetween);

                            pointList.add(inner.getTheWestIntersection().endPt);
                            pointList.add(inner.getTheWestIntersection().startPt);
                            return false;
                        } else {
                            int x = 0; // working hard if we get here
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
                        
                        if(nextIntersection.isEast){
                            Intersection nextNextIntersection = outer.getNextIntersection(nextIntersection);

                            if (nextIntersection.endIndex == nextNextIntersection.endIndex) {
                                pointList.add(nextIntersection.endPt);
                                pointList.add(nextNextIntersection.endPt);
                            } else {
                                outer.followFromTo(nextIntersection.endIndex,
                                        nextNextIntersection.endIndex,
                                        pointList,
                                        nextNextIntersection.endPt,
                                        nextNextIntersection.endPt);
                            }

                            IndexPair nextOuterIndex = new IndexPair(nextIntersection.endIndex, nextNextIntersection.endIndex);
                            outerIndices.add(nextOuterIndex);   
                            
                            nextIntersection = nextNextIntersection;
                        }

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
                Intersection nextSoutheastIntersection = theNextInner.getNextSoutheastIntersection();

                if (nextSoutheastIntersection.equals(theNextInner.getTheEastIntersection())) {
                    pointList.addAll(pointsTo);

                    pointList.add(nextEastIntersection.startPt);
                    pointList.add(nextEastIntersection.endPt);
                } else {
                    pointsTo = theNextInner.getPointsBetween(theNextInner.getNextWest(), nextSoutheastIntersection.endPt, false);

                    pointList.addAll(pointsTo);

                    pointList.add(nextSoutheastIntersection.endPt);
                    pointList.add(nextSoutheastIntersection.startPt);
                    theNextNextInner = nextSoutheastIntersection.mainInner;
                    nextEastIntersection = nextSoutheastIntersection;
                }
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
                    Coordinate endPoint = nextNextEastIntersection.startPt;
                    for (Intersection theOtherIntersection : otherIntersections) {
                        if (!theOtherIntersection.isEast) {
                            if (nextNextEastIntersection.startPt.getLatitude() > theOtherIntersection.startPt.getLatitude()) {
                                nextNextEastIntersection = theOtherIntersection;
                                endPoint = theOtherIntersection.endPt;
                            }
                        }
                    }

                    Coordinate lastPoint = pointList.get(pointList.size() - 1);
                    List<Coordinate> pointsForNextInner = theNextNextInner.getPointsBetween(lastPoint, endPoint, false);
                    pointList.addAll(pointsForNextInner);
 
                    if (endPoint.equals(nextNextEastIntersection.startPt)) {
                        pointList.add(nextNextEastIntersection.startPt);
                        pointList.add(nextNextEastIntersection.endPt);
                    } else {
                        pointList.add(nextNextEastIntersection.endPt);
                        pointList.add(nextNextEastIntersection.startPt);
                    }

                    InnerBoundary theNextNextMainInner = nextNextEastIntersection.mainInner;

                    List<Coordinate> pointsForNextNextInner = theNextNextMainInner.getSouthPoints(false);

                    if (theNextNextMainInner.equals(inner.getTheWestIntersection().otherInner)) {
                        Coordinate lastPoint2 = pointList.get(pointList.size() - 1);
                        pointsForNextNextInner = theNextNextMainInner.getPointsBetween(lastPoint2, inner.getTheWestIntersection().endPt, false);
                        pointList.addAll(pointsForNextNextInner);
                        pointList.add(inner.getTheWestIntersection().endPt);
                        return false;
                    }
//
//                    pointList.addAll(pointsForNextNextInner);
                    
                    InnerBoundary theNextNextNextInner = nextNextEastIntersection.otherInner;

                    if (theNextNextNextInner != null) {
                        Coordinate lastPoint3 = pointList.get(pointList.size() - 1);

                        if (theNextNextNextInner.equals(inner)) {
                            hasGeneratedSouthPoints = true;
                            List<Coordinate> pointsBetween = inner.getPointsBetween(nextNextEastIntersection.endPt, inner.getNextEast(), false);
                            pointList.addAll(pointsBetween);
                        } else {
                            if (inner.getTheWestIntersection().otherInner != null){
                                if(inner.getTheWestIntersection().otherInner.equals(theNextNextNextInner)) {
                                    List<Coordinate> pointsToInner = theNextNextNextInner.getPointsBetween(lastPoint3, inner.getTheWestIntersection().endPt, false);
                                    pointList.addAll(pointsToInner);
                                    return false;
                                }                         
                            }
                            
                            List<Coordinate> pointsForOtherInner = theNextNextNextInner.getPointsBetween(lastPoint3, theNextNextNextInner.getNextWest(), false);
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

                if (nextOuterIntersection.isEast) {
                    Intersection nextNextOuterIntersection = outer.getNextIntersection(nextOuterIntersection);

                    if (nextNextOuterIntersection != null) {
                        if (nextOuterIntersection.endIndex == nextNextOuterIntersection.endIndex) {
                            pointList.add(nextOuterIntersection.endPt);
                            pointList.add(nextNextOuterIntersection.endPt);
                        } else {
                            outer.followFromTo(nextOuterIntersection.endIndex,
                                    nextNextOuterIntersection.endIndex,
                                    pointList,
                                    nextNextOuterIntersection.endPt,
                                    nextNextOuterIntersection.endPt);
                        }

                        IndexPair nextOuterIndex = new IndexPair(nextOuterIntersection.endIndex, nextNextOuterIntersection.endIndex);
                        outerIndices.add(nextOuterIndex);
                    }
                }

//                pointList.add(nextOuterIntersection.endPt);
//                pointList.add(nextOuterIntersection.startPt);
//                
//                List<Coordinate> nextSouth = nextOuterIntersection.mainInner.getSouthPoints(false);
//                pointList.addAll(nextSouth);              
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
            if (nextEastInner.equals(inner.getTheWestIntersection().otherInner)) {
                List<Coordinate> pointsBetween = nextEastInner.getPointsBetween(nextEastIntersection.endPt, inner.getTheWestIntersection().endPt, false);
                pointList.addAll(pointsBetween);
                pointList.add(inner.getTheWestIntersection().endPt);
                pointList.add(inner.getTheWestIntersection().startPt);
            } else {
                List<Coordinate> pointsBetween = nextEastInner.getPointsBetween(nextEastIntersection.endPt, nextEastInner.getNextEast(), false);
                pointList.addAll(pointsBetween);

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

    public void getBottomIndex8() {
//            List<Coordinate> complete = inner.getClosedBoundary();

        // for special case doesn't hit this but I want it kept so that 
        // I can integerate the code later
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

                if (nextIntersection.mainInner.equals(inner)) {
                    List<Coordinate> southPoints = inner.getSouthPoints(false);
                    theBottomPoints.addAll(southPoints);
                    theBottomPoints.add(inner.getNextEast());
                    return;
                } else {
                    List<Coordinate> nextSouthPoints = nextInner.getSouthPoints(false);
                    Intersection nextOtherIntersection = nextInner.getNextOtherIntersection(nextIntersection, false);

                    if (nextOtherIntersection != null) {
                        nextSouthPoints = nextInner.getPointsBetween(nextIntersection.startPt, nextOtherIntersection.endPt, false);
                    }

                    theBottomPoints.addAll(nextSouthPoints);

                    if (nextOtherIntersection != null) {
                        theBottomPoints.add(nextOtherIntersection.endPt);
                        theBottomPoints.add(nextOtherIntersection.startPt);
                        InnerBoundary firstWestInner = nextOtherIntersection.mainInner;
                        theBottomPoints.add(firstWestInner.getNextEast());
                        theBottomPoints.add(firstWestInner.getNorth());
                        theBottomPoints.add(firstWestInner.getNextWest());
                        theBottomPoints.add(firstWestInner.getTheWestIntersection().startPt);
                        theBottomPoints.add(firstWestInner.getTheWestIntersection().endPt);

                        if (firstWestInner.getTheWestIntersection().outer != null) {
                            Intersection midIntersection = outer.getNextIntersection(firstWestInner.getTheWestIntersection());

                            if (nextIntersection != null) {
                                if (firstWestInner.getTheWestIntersection().endIndex == midIntersection.endIndex) {
                                    theBottomPoints.add(firstWestInner.getTheWestIntersection().endPt);
                                    theBottomPoints.add(midIntersection.endPt);
                                } else {
                                    outer.followFromTo(firstWestInner.getTheWestIntersection().endIndex,
                                            midIntersection.endIndex,
                                            theBottomPoints,
                                            midIntersection.endPt,
                                            midIntersection.endPt);
                                }

                                IndexPair midIndex = new IndexPair(firstWestInner.getTheWestIntersection().endIndex, midIntersection.endIndex);
                                outerIndices.add(midIndex);

                                theBottomPoints.add(midIntersection.endPt);
                                theBottomPoints.add(midIntersection.startPt);

                                InnerBoundary nextWestInner = midIntersection.mainInner;

                                theBottomPoints.add(nextWestInner.getNextEast());
                                theBottomPoints.add(nextWestInner.getNorth());
                                theBottomPoints.add(nextWestInner.getNextWest());

                                theBottomPoints.add(nextWestInner.getTheWestIntersection().startPt);
                                theBottomPoints.add(nextWestInner.getTheWestIntersection().endPt);

                                if (nextWestInner.getTheWestIntersection().outer != null) {
                                    Intersection westIntersection = outer.getNextIntersection(nextWestInner.getTheWestIntersection());

                                    if (westIntersection != null) {
                                        if (nextWestInner.getTheWestIntersection().endIndex == westIntersection.endIndex) {
                                            theBottomPoints.add(nextWestInner.getTheWestIntersection().endPt);
                                            theBottomPoints.add(westIntersection.endPt);
                                        } else {
                                            outer.followFromTo(nextWestInner.getTheWestIntersection().endIndex,
                                                    westIntersection.endIndex,
                                                    theBottomPoints,
                                                    westIntersection.endPt,
                                                    westIntersection.endPt);
                                        }

                                        IndexPair westIndex = new IndexPair(nextWestInner.getTheWestIntersection().endIndex, westIntersection.endIndex);
                                        outerIndices.add(westIndex);

                                        if (!westIntersection.isEast) {
                                            theBottomPoints.add(westIntersection.endPt);
                                            theBottomPoints.add(westIntersection.startPt);

                                            InnerBoundary topWestInner = westIntersection.mainInner;

                                            theBottomPoints.add(topWestInner.getNextWest());

                                            List<Coordinate> westSouthPoints = topWestInner.getSouthPoints(false);
                                            theBottomPoints.addAll(westSouthPoints);

                                            theBottomPoints.add(topWestInner.getNextEast());
                                            theBottomPoints.add(topWestInner.getTheEastIntersection().startPt);
                                            theBottomPoints.add(topWestInner.getTheEastIntersection().endPt);

                                            if (topWestInner.getTheEastIntersection().outer != null) {
                                                Intersection lastIntersection = outer.getNextIntersection(topWestInner.getTheEastIntersection());

                                                if (lastIntersection != null) {
                                                    if (topWestInner.getTheEastIntersection().endIndex == lastIntersection.endIndex) {
                                                        theBottomPoints.add(topWestInner.getTheEastIntersection().endPt);
                                                        theBottomPoints.add(lastIntersection.endPt);
                                                    } else {
                                                        outer.followFromTo(topWestInner.getTheEastIntersection().endIndex,
                                                                lastIntersection.endIndex,
                                                                theBottomPoints,
                                                                lastIntersection.endPt,
                                                                lastIntersection.endPt);
                                                    }

                                                    IndexPair lastIndex = new IndexPair(topWestInner.getTheEastIntersection().endIndex, lastIntersection.endIndex);
                                                    outerIndices.add(lastIndex);

                                                    theBottomPoints.add(lastIntersection.endPt);
                                                    theBottomPoints.add(lastIntersection.startPt);

                                                    List<Coordinate> mainSouthPoints = inner.getSouthPoints(false);
                                                    theBottomPoints.addAll(mainSouthPoints);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public void generatePoints(String theTitle) {
//            List<Coordinate> complete = inner.getClosedBoundary();
//
//            for (Coordinate thePoint : complete) {
//                theBottomPoints.add(thePoint);
//            }
//        if (inner.getIndex() == 7) {  // for april
//            getBottomIndex7(theBottomPoints);
//            return;
//        }
//
        if (theTitle.contains("Jul ") && inner.getIndex() == 8) { //for june
            getBottomIndex8();
            return;
        }

        if (theTitle.contains("Apr ") && inner.getIndex() == 15) {
            getBottomInitialEast(theBottomPoints);
            return;
        }
        
        if (theTitle.contains("Jul ") && inner.getIndex() == 9) {
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

                if (nextIntersection.mainInner == inner) {
                    List<Coordinate> southPoints = inner.getSouthPoints(false);
                    theBottomPoints.addAll(southPoints);
                    theBottomPoints.add(inner.getNextEast());
                    return;
                }

                List<Coordinate> southPoints = nextInner.getSouthPoints(false);
                theBottomPoints.addAll(southPoints);

                theBottomPoints.add(nextInner.getNextEast());
                theBottomPoints.add(nextInner.getTheEastIntersection().endPt);

                if (nextInner.getTheEastIntersection().outer != null) {
                    Intersection nextNextIntersection = outer.getNextIntersection(nextInner.getTheEastIntersection());

                    if (nextNextIntersection != null) {
                        if (nextInner.getTheEastIntersection().endIndex == nextNextIntersection.endIndex) {
                            theBottomPoints.add(nextInner.getTheEastIntersection().endPt);
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
                } else {
                    InnerBoundary nextNextInner = nextInner.getTheEastIntersection().otherInner;
                    Coordinate lastPoint = theBottomPoints.get(theBottomPoints.size() - 1);
                    List<Coordinate> pointsBetween = nextNextInner.getPointsBetween(lastPoint, nextNextInner.getNextEast(), false);

                    theBottomPoints.addAll(pointsBetween);

                    theBottomPoints.add(nextNextInner.getNextEast());
                    theBottomPoints.add(nextNextInner.getTheEastIntersection().endPt);

                    if (nextNextInner.getTheEastIntersection().outer != null) {
                        Intersection nextNextNextIntersection = outer.getNextIntersection(nextNextInner.getTheEastIntersection());

                        if (nextNextNextIntersection != null) {
                            if (nextNextInner.getTheEastIntersection().endIndex == nextNextNextIntersection.endIndex) {
                                theBottomPoints.add(nextNextInner.getTheEastIntersection().endPt);
                                theBottomPoints.add(nextNextNextIntersection.endPt);
                            } else {
                                outer.followFromTo(nextNextInner.getTheEastIntersection().endIndex,
                                        nextNextNextIntersection.endIndex,
                                        theBottomPoints,
                                        nextNextNextIntersection.endPt,
                                        nextNextNextIntersection.endPt);
                            }

                            IndexPair nextNextOuterIndex = new IndexPair(nextNextInner.getTheEastIntersection().endIndex, nextNextNextIntersection.endIndex);
                            outerIndices.add(nextNextOuterIndex);

                            nextInner = nextNextNextIntersection.mainInner;
                            nextIntersection = nextNextNextIntersection;
                        }
                    }
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

            if (nextIntersection != null) {
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

                if (!innerSouthEasternmost.startPt.equals(nextEastInner.getNextEast())) {
                    pointsForNextInner = nextEastInner.getPointsBetween(nextEastInner.getNextWest(), innerSouthEasternmost.endPt, false);
                }

                pointList.addAll(pointsForNextInner);

                if (!innerSouthEasternmost.startPt.equals(nextEastInner.getNextEast())) {
                    pointList.add(innerSouthEasternmost.endPt);
                    pointList.add(innerSouthEasternmost.startPt);

                    Intersection nextWestSideIntersection = innerSouthEasternmost.mainInner.getNextOtherIntersection(innerSouthEasternmost, false);

                    if (nextWestSideIntersection != null) {
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

                        if (nextIntersection != null) {
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

                    if (nextEastInner.getTheEastIntersection().outer != null) {
                        Intersection nextEastIntersection = outer.getNextIntersection(nextEastInner.getTheEastIntersection());

                        if (nextEastIntersection != null) {
                            if (nextEastInner.getTheEastIntersection().endIndex == nextEastIntersection.endIndex) {
                                pointList.add(nextEastInner.getTheEastIntersection().endPt);
                                pointList.add(nextEastIntersection.endPt);
                            } else {
                                outer.followFromTo(nextEastInner.getTheEastIntersection().endIndex,
                                        nextEastIntersection.endIndex,
                                        pointList,
                                        nextEastIntersection.endPt,
                                        nextEastIntersection.endPt);
                            }

                            IndexPair nextEastOuterIndex = new IndexPair(nextEastInner.getTheEastIntersection().endIndex, nextEastIntersection.endIndex);
                            outerIndices.add(nextEastOuterIndex);

                            if (!nextEastIntersection.isEast) {
                                InnerBoundary nextNextEastInner = nextEastIntersection.mainInner;
                                pointList.add(nextNextEastInner.getNextWest());
                                Intersection nextOtherIntersection = nextNextEastInner.getNextOtherIntersection(nextEastIntersection, false);

                                if (nextOtherIntersection.isEast) {
                                    List<Coordinate> pointsBetween = nextNextEastInner.getPointsBetween(nextNextEastInner.getNextWest(), nextOtherIntersection.endPt, false);
                                    pointList.addAll(pointsBetween);
                                    pointList.add(nextOtherIntersection.endPt);
                                    pointList.add(nextOtherIntersection.startPt);
                                    InnerBoundary firstWestInner = nextOtherIntersection.mainInner;
                                    pointList.add(firstWestInner.getNextEast());
                                    pointList.add(firstWestInner.getNorth());
                                    pointList.add(firstWestInner.getNextWest());
                                    pointList.add(firstWestInner.getTheWestIntersection().startPt);
                                    pointList.add(firstWestInner.getTheWestIntersection().endPt);

                                    if (firstWestInner.getTheWestIntersection().outer != null) {
                                        Intersection nextWestIntersection = outer.getNextIntersection(firstWestInner.getTheWestIntersection());

                                        if (nextWestIntersection != null) {
                                            if (firstWestInner.getTheWestIntersection().endIndex == nextWestIntersection.endIndex) {
                                                pointList.add(firstWestInner.getTheWestIntersection().endPt);
                                                pointList.add(nextWestIntersection.endPt);
                                            } else {
                                                outer.followFromTo(firstWestInner.getTheWestIntersection().endIndex,
                                                        nextWestIntersection.endIndex,
                                                        pointList,
                                                        nextWestIntersection.endPt,
                                                        nextWestIntersection.endPt);
                                            }

                                            IndexPair nextWestOuterIndex = new IndexPair(firstWestInner.getTheWestIntersection().endIndex, nextWestIntersection.endIndex);
                                            outerIndices.add(nextWestOuterIndex);

                                            pointList.add(nextWestIntersection.endPt);
                                            pointList.add(nextWestIntersection.startPt);

                                            InnerBoundary nextNextWestInner = nextWestIntersection.mainInner;
                                            pointList.add(nextNextWestInner.getNextEast());
                                            pointList.add(nextNextWestInner.getNorth());
                                            pointList.add(nextNextWestInner.getNextWest());
                                            pointList.add(nextNextWestInner.getTheWestIntersection().startPt);
                                            pointList.add(nextNextWestInner.getTheWestIntersection().endPt);

                                            if (nextNextWestInner.getTheWestIntersection().outer != null) {
                                                Intersection topEastIntersection = outer.getNextIntersection(nextNextWestInner.getTheWestIntersection());

                                                if (topEastIntersection != null) {
                                                    if (nextNextWestInner.getTheWestIntersection().endIndex == topEastIntersection.endIndex) {
                                                        pointList.add(nextNextWestInner.getTheWestIntersection().endPt);
                                                        pointList.add(topEastIntersection.endPt);
                                                    } else {
                                                        outer.followFromTo(nextNextWestInner.getTheWestIntersection().endIndex,
                                                                topEastIntersection.endIndex,
                                                                pointList,
                                                                topEastIntersection.endPt,
                                                                topEastIntersection.endPt);
                                                    }

                                                    IndexPair finalWestOuterIndex = new IndexPair(nextNextWestInner.getTheWestIntersection().endIndex, topEastIntersection.endIndex);
                                                    outerIndices.add(finalWestOuterIndex);

                                                    pointList.add(topEastIntersection.endPt);
                                                    pointList.add(topEastIntersection.startPt);

                                                    InnerBoundary topEastInner = topEastIntersection.mainInner;
                                                    List<Coordinate> southPoints = topEastInner.getSouthPoints(false);
                                                    pointList.add(topEastInner.getNextWest());
                                                    pointList.addAll(southPoints);
                                                    pointList.add(topEastInner.getNextEast());
//                                                    pointList.add(topEastInner.getTheEastIntersection().startPt);
//                                                    pointList.add(topEastInner.getTheEastIntersection().endPt);

//                                                    if(topEastInner.getTheEastIntersection().outer != null){
//                                                        Intersection topNextEastIntersection = outer.getNextIntersection(topEastInner.getTheEastIntersection());

//                                                        if(topNextEastIntersection != null){
//                                                            if (topEastInner.getTheEastIntersection().endIndex == topNextEastIntersection.endIndex) {
//                                                                pointList.add(topEastInner.getTheEastIntersection().endPt);
//                                                                pointList.add(topNextEastIntersection.endPt);
//                                                            } else {
//                                                                outer.followFromTo(topEastInner.getTheEastIntersection().endIndex,
//                                                                        topNextEastIntersection.endIndex,
//                                                                        pointList,
//                                                                        topNextEastIntersection.endPt,
//                                                                        topNextEastIntersection.endPt);
//                                                            }
//
//                                                            IndexPair anotherWestOuterIndex = new IndexPair(topEastInner.getTheEastIntersection().endIndex, topNextEastIntersection.endIndex);
//                                                            outerIndices.add(finalWestOuterIndex);
//                                                            
//                                                            pointList.add(topNextEastIntersection.endPt);
//                                                            pointList.add(topNextEastIntersection.startPt);
//                                                            List<Coordinate> theInnerSouthPoints = inner.getSouthPoints(false);
//                                                            pointList.addAll(theInnerSouthPoints);
//                                                            pointList.add(inner.getNextEast());
//                                                        }

//                                                  }

                                                }
                                            }



                                        }
                                    }
                                }

                                if (true) {
                                    return null;
                                }

                                pointList.addAll(nextNextEastInner.getSouthPoints(false));
                                pointList.add(nextNextEastInner.getNextEast());
                                pointList.add(nextNextEastInner.getTheEastIntersection().startPt);
                                pointList.add(nextNextEastInner.getTheEastIntersection().endPt);

                                if (nextNextEastInner.getTheEastIntersection().outer != null) {
                                    Intersection nextNextEastIntersection = outer.getNextIntersection(nextNextEastInner.getTheEastIntersection());

                                    if (nextNextEastIntersection != null) {
                                        if (nextNextEastInner.getTheEastIntersection().endIndex == nextNextEastIntersection.endIndex) {
                                            pointList.add(nextNextEastInner.getTheEastIntersection().endPt);
                                            pointList.add(nextNextEastIntersection.endPt);
                                        } else {
                                            outer.followFromTo(nextNextEastInner.getTheEastIntersection().endIndex,
                                                    nextNextEastIntersection.endIndex,
                                                    pointList,
                                                    nextNextEastIntersection.endPt,
                                                    nextNextEastIntersection.endPt);
                                        }

                                        IndexPair finalEastOuterIndex = new IndexPair(nextNextEastInner.getTheEastIntersection().endIndex, nextNextEastIntersection.endIndex);
                                        outerIndices.add(finalEastOuterIndex);

                                        pointList.add(nextNextEastIntersection.endPt);
                                        pointList.add(nextNextEastIntersection.startPt);
                                        InnerBoundary firstWestInner = nextNextEastIntersection.mainInner;
                                        pointList.add(firstWestInner.getNextEast());
                                        pointList.add(firstWestInner.getNorth());
                                        pointList.add(firstWestInner.getNextWest());
                                        pointList.add(firstWestInner.getTheWestIntersection().startPt);
                                        pointList.add(firstWestInner.getTheWestIntersection().endPt);

                                        if (firstWestInner.getTheWestIntersection().outer != null) {
                                            Intersection nextWestIntersection = outer.getNextIntersection(firstWestInner.getTheWestIntersection());

                                            if (nextWestIntersection != null) {
                                                if (firstWestInner.getTheWestIntersection().endIndex == nextWestIntersection.endIndex) {
                                                    pointList.add(firstWestInner.getTheWestIntersection().endPt);
                                                    pointList.add(nextWestIntersection.endPt);
                                                } else {
                                                    outer.followFromTo(firstWestInner.getTheWestIntersection().endIndex,
                                                            nextWestIntersection.endIndex,
                                                            pointList,
                                                            nextWestIntersection.endPt,
                                                            nextWestIntersection.endPt);
                                                }

                                                IndexPair westOuterIndex = new IndexPair(firstWestInner.getTheWestIntersection().endIndex, nextWestIntersection.endIndex);
                                                outerIndices.add(westOuterIndex);

                                                pointList.add(nextWestIntersection.endPt);
                                                pointList.add(nextWestIntersection.startPt);
                                            }

                                        }
                                    }


                                }
                            }

//                            if (nextEastInner.getTheEastIntersection().endIndex == finalEastIntersection.endIndex) {
//                                pointList.add(nextEastInner.getTheEastIntersection().endPt);
//                                pointList.add(finalEastIntersection.endPt);
//                            } else {
//                                outer.followFromTo(nextEastInner.getTheEastIntersection().endIndex,
//                                        finalEastIntersection.endIndex,
//                                        pointList,
//                                        finalEastIntersection.endPt,
//                                        finalEastIntersection.endPt);
//                            }
//
//                            IndexPair finalEastOuterIndex = new IndexPair(nextEastInner.getTheEastIntersection().endIndex, finalEastIntersection.endIndex);
//                            outerIndices.add(finalEastOuterIndex);

//                            InnerBoundary firstWestInner = finalEastIntersection.mainInner;
//                            
//                            pointList.add(firstWestInner.getNextWest());
//                            pointList.add(firstWestInner.getNorth());
//                            pointList.add(firstWestInner.getNextEast());
                        }
                    }
                }
            }
        }

        if (true) {
            return null;
        }

        Intersection easternmostIntersect = null;

        if (initialEast.outer != null) {
            // easy case straight to outer with no complications
            if (initialEast.allIntersects.size() == 1) {
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
                if (nextIntersection.getBoundary().isInner()) {
                    pointList.add(nextIntersection.getIntersectPoint());
                    // add from this point to next southerly other intersection pt
                    // then turn to go west or delegate to another function
                } else {
                    if (initialEast.allIntersects.size() == 3) {
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

    private void getBottomIndex7(List<Coordinate> pointList) {
        Intersection initialEast = inner.getTheEastIntersection();
        pointList.add(inner.getNextEast());


        if (inner.getTheEastIntersection().outer != null) {
            pointList.add(inner.getTheEastIntersection().startPt);
            pointList.add(inner.getTheEastIntersection().endPt);

            Intersection nextIntersection = outer.getNextIntersection(inner.getTheEastIntersection());

            // this bit should be dealt with by get outer east
            if (nextIntersection != null) {
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

                // going west now
                pointList.add(nextIntersection.endPt);
                pointList.add(nextIntersection.startPt);

                InnerBoundary nextInner = nextIntersection.mainInner;

                pointList.add(nextInner.getNextEast());
                pointList.add(nextInner.getNorth());
                pointList.add(nextInner.getNextWest());

                pointList.add(nextInner.getTheWestIntersection().startPt);
                pointList.add(nextInner.getTheWestIntersection().endPt);

                Intersection nextWestIntersection = outer.getNextIntersection(nextInner.getTheWestIntersection());

                // this bit should be dealt with by get outer east
                if (nextWestIntersection != null) {
                    if (nextInner.getTheWestIntersection().endIndex == nextWestIntersection.endIndex) {
                        pointList.add(nextInner.getTheWestIntersection().endPt);
                        pointList.add(nextWestIntersection.endPt);
                    } else {
                        outer.followFromTo(nextInner.getTheWestIntersection().endIndex,
                                nextWestIntersection.endIndex,
                                pointList,
                                nextWestIntersection.endPt,
                                nextWestIntersection.endPt);
                    }

                    IndexPair outerWestIndex = new IndexPair(nextInner.getTheWestIntersection().endIndex, nextWestIntersection.endIndex);
                    outerIndices.add(outerIndex);

                    pointList.add(nextWestIntersection.endPt);
                    pointList.add(nextWestIntersection.startPt);

                    InnerBoundary topWestInner = nextWestIntersection.mainInner;

                    List<Coordinate> topWestInnerPoints = topWestInner.getSouthPoints(false);
                    Intersection theSouthEast = topWestInner.getNextSoutheastIntersection();

                    if (theSouthEast != null) {
                        topWestInnerPoints = topWestInner.getPointsBetween(nextWestIntersection.startPt, theSouthEast.endPt, false);
                    }

                    pointList.addAll(topWestInnerPoints);

                    InnerBoundary nextEastInner = topWestInner.getTheEastIntersection().otherInner;

                    if (theSouthEast != null) {
                        pointList.add(theSouthEast.endPt);
                        pointList.add(theSouthEast.startPt);
                        nextEastInner = theSouthEast.mainInner;
                    } else {
                        pointList.add(topWestInner.getTheEastIntersection().startPt);
                        pointList.add(topWestInner.getTheEastIntersection().endPt);
                    }

                    List<Coordinate> nextEastInnerPoints = nextEastInner.getSouthPoints(false);
                    Intersection theNextSouthEast = nextEastInner.getNextSoutheastIntersection();

                    if (theNextSouthEast != null) {
                        nextEastInnerPoints = nextEastInner.getPointsBetween(pointList.get(pointList.size() - 1), theNextSouthEast.endPt, false);
                    }

                    pointList.addAll(nextEastInnerPoints);

                    InnerBoundary nextNextEastInner = nextEastInner.getTheEastIntersection().otherInner;

                    if (theNextSouthEast != null) {
                        pointList.add(theNextSouthEast.endPt);
                        pointList.add(theNextSouthEast.startPt);
                        nextNextEastInner = theNextSouthEast.mainInner;
                    } else {
                        pointList.add(nextEastInner.getTheEastIntersection().startPt);
                        pointList.add(nextEastInner.getTheEastIntersection().endPt);
                    }

                    List<Coordinate> nextNextEastInnerPoints = nextNextEastInner.getSouthPoints(false);
                    Intersection theNextNextSouthEast = nextNextEastInner.getNextSoutheastIntersection();

                    if (!theNextNextSouthEast.startPt.equals(nextNextEastInner.getNextEast())) {
                        nextNextEastInnerPoints = nextNextEastInner.getPointsBetween(pointList.get(pointList.size() - 1), theNextNextSouthEast.endPt, false);
                    }

                    pointList.addAll(nextNextEastInnerPoints);

                    Intersection nextNextNextEastIntersection = nextNextEastInner.getTheEastIntersection();

                    if (!theNextNextSouthEast.startPt.equals(nextNextEastInner.getNextEast())) {
                        pointList.add(theNextNextSouthEast.endPt);
                        pointList.add(theNextNextSouthEast.startPt);
                        nextNextNextEastIntersection = theNextNextSouthEast;
                        // means it is an intersect to another inner
                    } else {
                        pointList.add(nextNextEastInner.getTheEastIntersection().startPt);
                        pointList.add(nextNextEastInner.getTheEastIntersection().endPt);
                        // intersect could be to an outer

                        if (nextNextNextEastIntersection.outer != null) {
                            Intersection midIntersection = outer.getNextIntersection(nextNextNextEastIntersection);

                            // this bit should be dealt with by get outer east
                            if (midIntersection != null) {
                                if (nextNextNextEastIntersection.endIndex == midIntersection.endIndex) {
                                    pointList.add(nextNextNextEastIntersection.endPt);
                                    pointList.add(midIntersection.endPt);
                                } else {
                                    outer.followFromTo(nextNextNextEastIntersection.endIndex,
                                            midIntersection.endIndex,
                                            pointList,
                                            midIntersection.endPt,
                                            midIntersection.endPt);
                                }

                                IndexPair outerMidIndex = new IndexPair(nextNextNextEastIntersection.endIndex, midIntersection.endIndex);
                                outerIndices.add(outerMidIndex);

                                pointList.add(midIntersection.endPt);
                                pointList.add(midIntersection.startPt);

                                // back home so add the inners points
                                List<Coordinate> southPoints = inner.getSouthPoints(false);
                                pointList.addAll(southPoints);
//                                pointList.add(inner.getNextWest());
//                                pointList.add(inner.getNorth());                                
//                                pointList.add(inner.getNextEast());
                            }
                        }
                    }
                }
            }
        }
    }

    private int getMostWestIndex() {
        int mostWestIndex = 0;
        List<Coordinate> pointList = theBottomPoints;
        double mostWesterlyLon = 179.99;

        for (int i = 0; i < pointList.size(); ++i) {
            if (pointList.get(i).getLongitude() < mostWesterlyLon) {
                mostWesterlyLon = pointList.get(i).getLongitude();
                mostWestIndex = i;
            }
        }

        return mostWestIndex;
    }

    private int getMostEastIndex() {
        int mostEastIndex = 0;
        List<Coordinate> pointList = theBottomPoints;
        double mostEasterlyLon = -179.99;

        for (int i = 0; i < pointList.size(); ++i) {
            if (pointList.get(i).getLongitude() > mostEasterlyLon) {
                mostEasterlyLon = pointList.get(i).getLongitude();
                mostEastIndex = i;
            }
        }

        return mostEastIndex;
    }

    private int getMostNorthIndex() {
        int mostNorthIndex = 0;
        List<Coordinate> pointList = theBottomPoints;
        double mostNortherlyLon = -89.99;

        for (int i = 0; i < pointList.size(); ++i) {
            if (pointList.get(i).getLongitude() > mostNortherlyLon) {
                mostNortherlyLon = pointList.get(i).getLatitude();
                mostNorthIndex = i;
            }
        }

        return mostNorthIndex;
    }

    private int getMostSouthIndex() {
        int mostSouthIndex = 0;
        List<Coordinate> pointList = theBottomPoints;
        double mostSoutherlyLat = 89.99;

        for (int i = 0; i < pointList.size(); ++i) {
            if (pointList.get(i).getLongitude() < mostSoutherlyLat) {
                mostSoutherlyLat = pointList.get(i).getLatitude();
                mostSouthIndex = i;
            }
        }

        return mostSouthIndex;
    }
}
