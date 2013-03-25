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
public class SouthSlice {

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

    List<Coordinate> getBottomPoints() {
        return theBottomPoints;
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
            inner.SetSouthLoopback(true);

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

            // need round trip to itself west
        } else {
            Intersection nextIntersection = outer.getNextIntersection(innerForNextIntersection.getTheWestIntersection());
            List<Coordinate> outerPoints = outer.getPoints();
            int i = innerForNextIntersection.getTheWestIntersection().endIndex;

            if (innerForNextIntersection.getTheWestIntersection().endIndex == nextIntersection.endIndex) {
                pointList.add(innerForNextIntersection.getTheWestIntersection().endPt);
                pointList.add(nextIntersection.endPt);
            } else {
                outer.followFromTo(innerForNextIntersection.getTheWestIntersection().endIndex,
                        nextIntersection.endIndex,
                        pointList,
                        prevIntersection.endPt,
                        nextIntersection.endPt);
            }
            
            IndexPair outerIndex = new IndexPair(innerForNextIntersection.getTheWestIntersection().endIndex, nextIntersection.endIndex);
            outerIndices.add(outerIndex);

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
                if (theNextNextInner.equals(inner)) {
                    hasGeneratedSouthPoints = true;
                    List<Coordinate> pointsBetween = inner.getPointsBetween(nextEastIntersection.endPt, inner.getNextEast(), false);
                    for (Coordinate thePoint : pointsBetween) {
                        pointList.add(thePoint);
                    }
                } else {
                    Intersection nextNextEastIntersection = theNextNextInner.getTheEastIntersection();

                    List<Coordinate> pointsForNextInner = theNextNextInner.getPointsBetween(nextEastIntersection.endPt, theNextNextInner.getNextEast(), false);
                    for (Coordinate thePoint : pointsForNextInner) {
                        pointList.add(thePoint);
                    }

                    pointList.add(nextNextEastIntersection.startPt);
                    pointList.add(nextNextEastIntersection.endPt);

                    InnerBoundary theNextNextNextInner = nextNextEastIntersection.otherInner;

                    if (theNextNextNextInner != null) {
                        if (theNextNextNextInner.equals(inner)) {
                            hasGeneratedSouthPoints = true;
                            List<Coordinate> pointsBetween = inner.getPointsBetween(nextNextEastIntersection.endPt, inner.getNextEast(), false);

                            for (Coordinate thePoint : pointsBetween) {
                                pointList.add(thePoint);
                            }
                        } else {
                            List<Coordinate> pointsForOtherInner = theNextNextNextInner.getSouthPoints(false);
                            for (Coordinate thePoint : pointsForOtherInner) {
                                pointList.add(thePoint);
                            }

                            Intersection nextNextNextEastIntersection = theNextNextNextInner.getTheEastIntersection();
                            pointList.add(nextNextNextEastIntersection.startPt);
                            pointList.add(nextNextNextEastIntersection.endPt);

                            InnerBoundary theNextNextNextNextInner = nextNextNextEastIntersection.otherInner;

                            if (theNextNextNextNextInner != null) {
                                if (theNextNextNextNextInner.equals(inner)) {
                                    hasGeneratedSouthPoints = true;
                                    List<Coordinate> pointsBetween = inner.getPointsBetween(nextNextNextEastIntersection.endPt, inner.getNextEast(), false);
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

        if (nextEastInner != null && !(nextEastInner.equals(this))) {
            List<Coordinate> pointsBetween = nextEastInner.getPointsBetween(nextEastIntersection.endPt, nextEastInner.getNextEast(), false);
            for (Coordinate thePoint : pointsBetween) {
                pointList.add(thePoint);
            }

            Intersection nextEastEast = nextEastInner.getTheEastIntersection();

            pointList.add(nextEastEast.startPt);
            pointList.add(nextEastEast.endPt);

            InnerBoundary nextNextEastInner = nextEastEast.otherInner;

            if (nextNextEastInner != null && !(nextNextEastInner.equals(this))) {
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
    
    public Placemark getPlacemark() {
        return placemark;
    }
    
    public Polygon getPolygon() {
        return (Polygon) placemark.getGeometry();
    }

    public OuterIndices getOuterIndices() {
        return outerIndices;
    }

    void generatePoints() {
        if (inner.getTheEastIntersection().outer == null) {
            return;
        }

        boolean hasGeneratedSouthPoints = false;

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

                List<Coordinate> southPoints = nextInner.getSouthPoints(false);

                for (Coordinate thePoint : southPoints) {
                    theBottomPoints.add(thePoint);
                }

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

                    nextInner = nextNextIntersection.mainInner;
                    nextIntersection = nextNextIntersection;
                    
                    IndexPair nextOuterIndex = new IndexPair(nextInner.getTheEastIntersection().endIndex, nextNextIntersection.endIndex);
                    outerIndices.add(nextOuterIndex);
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

        List<Integer> theNextWestIndex = new ArrayList<>();

//        if (nextEastIntersection != null) {
        for (int i = 0; i < theBottomPoints.size(); ++i) {
            if (theBottomPoints.get(i).equals(inner.getNextWest())) {
                theNextWestIndex.add(i);
//                    Coordinate theNext = theBottomPoints.get(i + 1);
//                    Coordinate thePrev = theBottomPoints.get(i - 1);
                int x = 0;
            }
        }
//        }

        theBottomPoints.add(inner.getNextEast());

//        theBottomPoints.add(inner.getTheEastIntersection().startPt);

//        if (inner.GetSouthLoopback() == true) {
//            return new ArrayList<>();
//        } else {
//            return theBottomPoints;
//        }
    }
}
