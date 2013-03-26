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
public class NorthSlice implements Slice {

    private OuterBoundary outer;
    private InnerBoundary inner;
    private List<Intersection> outerIntersections;
    private Placemark placemark;
    private List<Coordinate> generatedPoints;
    // want a record of outer indices covered
    // a list of pairs??
    // need similar in south so I can tell if already generated
    // could be pairs of indices or pairs of intersections
    private OuterIndices outerIndices;

    NorthSlice(OuterBoundary theOuter, InnerBoundary theMainInner, Placemark thePlacemark) {
        outer = theOuter;
        inner = theMainInner;
        placemark = thePlacemark;
        outerIndices = new OuterIndices();
        generatedPoints = new ArrayList<>();
    }

    public void addIntersection(Intersection intersectionWithOuter) {
        outerIntersections.add(intersectionWithOuter);
    }

    @Override
    public void generatePoints() {
        generatedPoints = new ArrayList<>();

        getTopInitialWest(generatedPoints);
        // from the should generate north the west isntersection has to go to outer
        if (inner.getTheWestIntersection().outer != null) {
            Intersection nextIntersection = getTopWestOuter(generatedPoints);

            if (nextIntersection.mainInner != inner) {
                Intersection topEastIntersection = getTopEast(nextIntersection, generatedPoints);

                if (topEastIntersection != null) {
                    Intersection nextOuterIntersection = getTopEastOuter(topEastIntersection, generatedPoints);

                    // going west again - well tending that way anyway
                    getTopWestBackHome(nextOuterIntersection, generatedPoints);

                    generatedPoints.add(inner.getTheEastIntersection().endPt);
                    generatedPoints.add(inner.getTheEastIntersection().startPt);
                } else {
                    generatedPoints.add(inner.getNextEast());
                }
            }
        }

//        generatedPoints.add(theEastIntersection.endPt);
//        generatedPoints.add(theEastIntersection.startPt);
    }

    // debug code to help find if a point is duplicated
    List<Integer> getThePointIndex(Coordinate thePoint) {
        List<Integer> thePointIndices = new ArrayList<>();

        // debug code
        for (int i = 0; i < generatedPoints.size(); ++i) {
            if (generatedPoints.get(i).equals(inner.getNextWest())) {
                thePointIndices.add(i);
                Coordinate theNext = generatedPoints.get(i + 1);
                Coordinate thePrev = generatedPoints.get(i - 1);
                int x = 0;
            }
        }

        return thePointIndices;
    }

    void getTopInitialWest(List<Coordinate> pointList) {
        pointList.add(inner.getTheEastIntersection().startPt);
        pointList.add(inner.getNextEast());
        pointList.add(inner.getPoints().get(inner.getNorthIndex()));
        pointList.add(inner.getNextWest());
        pointList.add(inner.getTheWestIntersection().endPt);
    }

    Intersection getTopWestOuter(List<Coordinate> pointList) {
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

    Intersection getTopEast(Intersection nextIntersection, List<Coordinate> pointList) {
        Intersection topEastOuterIntersection = null;
        pointList.add(nextIntersection.startPt);

        InnerBoundary innerForNextIntersection = nextIntersection.mainInner;
        Intersection nextEastIntersection = innerForNextIntersection.getTheEastIntersection();

        if (inner.getTheEastIntersection().otherInner == nextEastIntersection.mainInner) {
            List<Coordinate> pointsTo = innerForNextIntersection.getPointsBetween(nextEastIntersection.startPt, inner.getTheEastIntersection().endPt, false);
            for (Coordinate thePoint : pointsTo) {
                pointList.add(thePoint);
            }
            pointList.add(inner.getTheEastIntersection().endPt);
            pointList.add(inner.getTheEastIntersection().startPt);
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
            InnerBoundary innerForNextEastIntersection = nextEastIntersection.otherInner;
            Intersection nextNextEastIntersection = innerForNextEastIntersection.getTheEastIntersection();

            if (inner.getTheEastIntersection().otherInner == nextNextEastIntersection.mainInner) {
                List<Coordinate> pointsTo = innerForNextIntersection.getPointsBetween(innerForNextEastIntersection.getNextWest(), inner.getTheEastIntersection().endPt, false);
                for (Coordinate thePoint : pointsTo) {
                    pointList.add(thePoint);
                }
                pointList.add(inner.getTheEastIntersection().endPt);
                pointList.add(inner.getTheEastIntersection().startPt);
                return null;
            } else {
                List<Coordinate> pointsToNext = innerForNextEastIntersection.getPointsBetween(nextEastIntersection.endPt, innerForNextEastIntersection.getNextEast(), false);

                for (Coordinate thePoint : pointsToNext) {
                    pointList.add(thePoint);
                }

                pointList.add(nextNextEastIntersection.startPt);
                pointList.add(nextNextEastIntersection.endPt);
            }

            if (nextNextEastIntersection.outer != null) {
                topEastOuterIntersection = nextNextEastIntersection;
            } else {
                InnerBoundary innerForNextNextEastIntersection = nextNextEastIntersection.otherInner;
                Intersection nextNextNextEastIntersection = innerForNextNextEastIntersection.getTheEastIntersection();

                if (inner.getTheEastIntersection().otherInner == nextNextNextEastIntersection.mainInner) {
                    List<Coordinate> pointsTo = innerForNextNextEastIntersection.getPointsBetween(nextNextNextEastIntersection.endPt, inner.getTheEastIntersection().endPt, false);
                    for (Coordinate thePoint : pointsTo) {
                        pointList.add(thePoint);
                    }
                    pointList.add(inner.getTheEastIntersection().endPt);
                    pointList.add(inner.getTheEastIntersection().startPt);
                    return null;
                } else {
                    List<Coordinate> pointsToNextNext = innerForNextNextEastIntersection.getPointsBetween(nextNextEastIntersection.endPt, innerForNextNextEastIntersection.getNextEast(), false);
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

    void getTopWestBackHome(Intersection nextOuterIntersection, List<Coordinate> pointList) {
        pointList.add(nextOuterIntersection.startPt);

        InnerBoundary nextWestInner = nextOuterIntersection.mainInner;
        Intersection nextWestIntersection = nextWestInner.getTheWestIntersection();

        List<Intersection> otherIntersections = nextWestInner.getTheOtherIntersections();

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
            pointList.add(nextWestInner.getPoints().get(nextWestInner.getNorthIndex()));
            for (Coordinate thePoint : pointsToNext) {
                pointList.add(thePoint);
            }

            // TODO - this should finish when the inner equals theEastIntersection.otherInner
            if (inner.getTheEastIntersection().otherInner != nextWestInner) {
                pointList.add(theOtherIntersection.endPt);
                pointList.add(theOtherIntersection.startPt);
                pointList.add(theOtherIntersection.mainInner.getPoints().get(theOtherIntersection.mainInner.getNorthIndex()));
                pointList.add(theOtherIntersection.mainInner.getNextWest());
                List<Coordinate> pointsToNextAgain = theOtherIntersection.mainInner.getPointsBetween(theOtherIntersection.mainInner.getNextWest(), inner.getTheEastIntersection().endPt, false);
                for (Coordinate thePoint : pointsToNextAgain) {
                    pointList.add(thePoint);
                }
            }

            pointList.add(inner.getTheEastIntersection().endPt);
            pointList.add(inner.getTheEastIntersection().startPt);
            pointList.add(inner.getNextEast());
        } else {
            for (Coordinate thePoint : pointsToNext) {
                pointList.add(thePoint);
            }

            pointList.add(nextWestIntersection.endPt);
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
    public InnerBoundary getInner() {
        return inner;
    }

    @Override
    public List<Coordinate> getGeneratedPoints() {
        return generatedPoints;
    }
}
