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
//        if(inner.getTheWestIntersection().outer == null){
//            return;
//        }

        generatedPoints = new ArrayList<>();

        Intersection innerEast = inner.getTheEastIntersection();
        Intersection innerWest = inner.getTheWestIntersection();
        
        // from the should generate north the west isntersection has to go to outer
        if (innerWest.outer != null) {
            getTopInitialWest(generatedPoints);

            Intersection nextIntersection = getTopWestOuter(generatedPoints);
  
            if(innerEast.otherInner != null &&
                    innerEast.otherInner.equals(nextIntersection.mainInner)){
                List<Coordinate> pointsTo = nextIntersection.mainInner.getPointsBetween(nextIntersection.mainInner.getNextWest(), innerEast.endPt, false);
                generatedPoints.addAll(pointsTo);
                generatedPoints.add(innerEast.endPt);
                generatedPoints.add(innerEast.startPt);
                return;
            }
            
            if (nextIntersection.mainInner.getTheEastIntersection().otherInner != null &&
                    inner.equals(nextIntersection.mainInner.getTheEastIntersection().otherInner)){
                generatedPoints.clear();
                generatedPoints.add(inner.getPoints().get(inner.getNorthIndex()));
                generatedPoints.add(inner.getNextWest());
                generatedPoints.add(innerWest.endPt);
                generatedPoints.add(nextIntersection.endPt);
                generatedPoints.add(nextIntersection.startPt);
                List<Coordinate> pointsTo = nextIntersection.mainInner.getPointsBetween(nextIntersection.startPt, nextIntersection.mainInner.getNextEast(), false);
                generatedPoints.addAll(pointsTo);
                generatedPoints.add(nextIntersection.mainInner.getNextEast());
                generatedPoints.add(nextIntersection.mainInner.getTheEastIntersection().startPt);
                generatedPoints.add(nextIntersection.mainInner.getTheEastIntersection().endPt);
                return;
            }     
            
            if (nextIntersection.mainInner != inner) {
                Intersection topEastIntersection = getTopEast(nextIntersection, generatedPoints);

                                        if ( true){
                return;
            }
                if (topEastIntersection != null) {
                    Intersection nextOuterIntersection = getTopEastOuter(topEastIntersection, generatedPoints);

                    if (nextOuterIntersection != null) {
                        if (!nextOuterIntersection.isEast) {
                            Intersection nextTopEastIntersection = getTopEast(nextOuterIntersection, generatedPoints);

                            if (nextTopEastIntersection != null) {
                                nextOuterIntersection = getTopEastOuter(nextTopEastIntersection, generatedPoints);
                            }
                        }
                    }
                    
                    // going west again - well tending that way anyway
                    if (!(inner.getTheEastIntersection().endPt.equals(nextOuterIntersection.endPt))) {
                        getTopWestBackHome(nextOuterIntersection, generatedPoints);
                    }

                    generatedPoints.add(inner.getTheEastIntersection().endPt);
                    generatedPoints.add(inner.getTheEastIntersection().startPt);
                } else {
                    // these should already have been added but make sure for deubugging
                    generatedPoints.add(inner.getTheEastIntersection().endPt);
                    generatedPoints.add(inner.getTheEastIntersection().startPt);
                    generatedPoints.add(inner.getNextEast());
                }
            }
        } else {
            if (inner.getTheEastIntersection().otherInner == null) {
                return;
            }

            getTopInitialWest(generatedPoints);

            InnerBoundary theWestInner = inner.getTheWestIntersection().otherInner;
            InnerBoundary theEastInner = inner.getTheEastIntersection().otherInner;

            List<Coordinate> pointsTo = theWestInner.getPointsBetween(inner.getTheWestIntersection().endPt, theWestInner.getNextEast(), false);
            generatedPoints.addAll(pointsTo);

            generatedPoints.add(theWestInner.getTheEastIntersection().startPt);
            generatedPoints.add(theWestInner.getTheEastIntersection().endPt);

            InnerBoundary nextInner = theWestInner.getTheEastIntersection().otherInner;

            if (nextInner != null) {
                if (theEastInner.equals(nextInner)) {
                    List<Coordinate> pointsToNext = nextInner.getPointsBetween(theWestInner.getTheEastIntersection().endPt, inner.getTheEastIntersection().endPt, false);

                    for (Coordinate thePoint : pointsToNext) {
                        generatedPoints.add(thePoint);
                    }
                } else {
                    List<Coordinate> pointsToNext = nextInner.getPointsBetween(theWestInner.getTheEastIntersection().endPt, nextInner.getNextEast(), false);

                    for (Coordinate thePoint : pointsToNext) {
                        generatedPoints.add(thePoint);
                    }

                    generatedPoints.add(nextInner.getTheEastIntersection().startPt);
                    generatedPoints.add(nextInner.getTheEastIntersection().endPt);

                    InnerBoundary nextNextInner = nextInner.getTheEastIntersection().otherInner;
                    
                    if(nextNextInner == null){
                        Intersection topEastOuter = getTopEastOuter(nextInner.getTheEastIntersection(), generatedPoints);
                        getTopWestBackHome(topEastOuter, generatedPoints);                       
                    } else {
                        if (theEastInner.equals(nextNextInner)) {
                            List<Coordinate> pointsTo2 = nextNextInner.getPointsBetween(nextInner.getTheEastIntersection().endPt, inner.getTheEastIntersection().endPt, false);
                                generatedPoints.addAll(pointsTo2);
                        } else {
                            List<Coordinate> pointsTo2 = theEastInner.getPointsBetween(theEastInner.getNextWest(), inner.getTheEastIntersection().endPt, false);
                                generatedPoints.addAll(pointsTo2);
                        }
                    }
                }

                generatedPoints.add(inner.getTheEastIntersection().endPt);
                generatedPoints.add(inner.getTheEastIntersection().startPt);
            } else {
                Intersection topEastOuter = getTopEastOuter(theWestInner.getTheEastIntersection(), generatedPoints);
                if(theEastInner != null){
                    if(theEastInner == topEastOuter.mainInner){
                        List<Coordinate> pointsBetween = theEastInner.getPointsBetween(topEastOuter.startPt, inner.getTheEastIntersection().endPt, false);
                        generatedPoints.addAll(pointsBetween);
                    }
                }
                
                generatedPoints.add(inner.getTheEastIntersection().endPt);
                generatedPoints.add(inner.getTheEastIntersection().startPt);                
            }
        }
        
        if(generatedPoints.size() < 6){
            int y = 0;
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
 
    // precon - prev point is west end pt
    // west intersection goes to another inner
    void getTopWestToOuter(List<Coordinate> pointList) {
        Intersection theWest = inner.getTheWestIntersection();
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

    // trace east above the curretn inner boundary
    // terminates either when found the western intersection with outer
    // or the inner that connect to the east of thr current inner
    Intersection getTopEast(Intersection nextIntersection, List<Coordinate> pointList) {
        Intersection topEastOuterIntersection = null;

        InnerBoundary innerForNextIntersection = nextIntersection.mainInner;
        Intersection nextEastIntersection = innerForNextIntersection.getTheEastIntersection();
        InnerBoundary innerForNextEastIntersection = nextEastIntersection == null ? null : nextEastIntersection.otherInner;

//        if(innerForNextEastIntersection == null){
//            return nextEastIntersection;
//        }
        
        pointList.add(nextIntersection.startPt);

        if (inner.getTheEastIntersection().otherInner == nextEastIntersection.mainInner) {
            List<Coordinate> pointsTo = innerForNextIntersection.getPointsBetween(nextEastIntersection.startPt, inner.getTheEastIntersection().endPt, false);
            for (Coordinate thePoint : pointsTo) {
                pointList.add(thePoint);
            }
            pointList.add(inner.getTheEastIntersection().endPt);
            pointList.add(inner.getTheEastIntersection().startPt);
            return null;
        } else {
            // normal case for innerForNextIntersection
            Coordinate lastPoint = pointList.get(pointList.size() - 1);
            List<Coordinate> pointsTo = innerForNextIntersection.getPointsToNextEastgoingIntersection(lastPoint);    
            Intersection nextSoutheastIntersection = innerForNextIntersection.getNextSoutheastIntersection();

            pointList.addAll(pointsTo);

            if (innerForNextEastIntersection != null &&
                    nextSoutheastIntersection.otherInner.equals(innerForNextEastIntersection)) {
                pointList.add(nextEastIntersection.startPt);
                pointList.add(nextEastIntersection.endPt);
            } else {
                nextEastIntersection = nextSoutheastIntersection;
                innerForNextEastIntersection = nextSoutheastIntersection.mainInner;
                
                pointList.add(nextSoutheastIntersection.endPt);

                if(nextEastIntersection.outer == null){
                    pointList.add(nextSoutheastIntersection.startPt);
                }
            }
        }

        if (nextEastIntersection.outer != null) {
            topEastOuterIntersection = nextEastIntersection;
        } else {
            Intersection nextNextEastIntersection = innerForNextEastIntersection.getTheEastIntersection();
            InnerBoundary innerForNextNextEastIntersection = nextNextEastIntersection == null ? null : nextNextEastIntersection.otherInner;
            Coordinate nextNextEastStartPt = nextNextEastIntersection == null ? null : nextNextEastIntersection.endPt;

            if (inner.getTheEastIntersection().otherInner == nextNextEastIntersection.mainInner) {
                List<Coordinate> pointsTo = innerForNextIntersection.getPointsBetween(innerForNextEastIntersection.getNextWest(), inner.getTheEastIntersection().endPt, false);
                for (Coordinate thePoint : pointsTo) {
                    pointList.add(thePoint);
                }
                pointList.add(inner.getTheEastIntersection().endPt);
                pointList.add(inner.getTheEastIntersection().startPt);
                return null;
            } else {
                // normal case for innerForNextIntersection
                Coordinate lastPoint = pointList.get(pointList.size() - 1);
                List<Coordinate> pointsToNext = innerForNextEastIntersection.getPointsToNextEastgoingIntersection(lastPoint);    
                Intersection nextNextSoutheastIntersection = innerForNextEastIntersection.getNextSoutheastIntersection();

                pointList.addAll(pointsToNext);

                if (innerForNextNextEastIntersection != null &&
                        nextNextSoutheastIntersection.otherInner.equals(innerForNextNextEastIntersection)) {
                    pointList.add(nextNextEastIntersection.startPt);
                    pointList.add(nextNextEastIntersection.endPt);
                } else {
                    nextNextEastIntersection = nextNextSoutheastIntersection;
                    innerForNextNextEastIntersection = nextNextSoutheastIntersection.mainInner;

                    pointList.add(nextNextSoutheastIntersection.endPt);

                    if(nextNextEastIntersection.outer == null){
                        pointList.add(nextNextSoutheastIntersection.startPt);
                    }
                }               
            }

            if (nextNextEastIntersection.outer != null) {
                topEastOuterIntersection = nextNextEastIntersection;
            } else {
                Intersection nextNextNextEastIntersection = innerForNextNextEastIntersection.getTheEastIntersection();
                InnerBoundary innerForNextNextNextEastIntersection = nextNextNextEastIntersection.otherInner;

                if (inner.getTheEastIntersection().otherInner == nextNextNextEastIntersection.mainInner) {
                    List<Coordinate> pointsTo = innerForNextNextEastIntersection.getPointsBetween(nextNextEastStartPt, inner.getTheEastIntersection().endPt, false);
                    pointList.addAll(pointsTo);
                    pointList.add(inner.getTheEastIntersection().endPt);
                    pointList.add(inner.getTheEastIntersection().startPt);
                    return null;
                } else {
                    Coordinate lastPoint = pointList.get(pointList.size() - 1);
                    List<Coordinate> pointsTo = innerForNextNextEastIntersection.getPointsToNextEastgoingIntersection(lastPoint);    
                    Intersection nextNextNextSoutheastIntersection = innerForNextNextEastIntersection.getNextSoutheastIntersection();
                    
                    pointList.addAll(pointsTo);

                    if (innerForNextNextNextEastIntersection != null &&
                            nextNextNextSoutheastIntersection.otherInner.equals(innerForNextNextNextEastIntersection)) {
                        pointList.add(nextNextNextEastIntersection.startPt);
                        pointList.add(nextNextNextEastIntersection.endPt);
                    } else {
                        nextNextNextEastIntersection = nextNextNextSoutheastIntersection;
                        innerForNextNextNextEastIntersection = nextNextNextSoutheastIntersection.mainInner;

                        pointList.add(nextNextNextSoutheastIntersection.endPt);
                        
                        if(nextNextNextEastIntersection.outer == null){
                            pointList.add(nextNextNextSoutheastIntersection.startPt);
                        }
                    }
                }

                if (nextNextNextEastIntersection.outer != null) {
                    topEastOuterIntersection = nextNextNextEastIntersection;
                } else {
                    Intersection nextNextNextNextEastIntersection = innerForNextNextNextEastIntersection.getTheEastIntersection();
                    InnerBoundary innerForNextNextNextNextEastIntersection = nextNextNextNextEastIntersection.otherInner;

                    if (inner.getTheEastIntersection().otherInner == nextNextNextNextEastIntersection.mainInner) {
                        List<Coordinate> pointsTo = innerForNextNextNextEastIntersection.getPointsBetween(nextNextNextEastIntersection.endPt, inner.getTheEastIntersection().endPt, false);
                        pointList.addAll(pointsTo);
                        pointList.add(inner.getTheEastIntersection().endPt);
                        pointList.add(inner.getTheEastIntersection().startPt);
                        return null;
                    } else {
                        Coordinate lastPoint = pointList.get(pointList.size() - 1);
                        List<Coordinate> pointsTo = innerForNextNextNextEastIntersection.getPointsToNextEastgoingIntersection(lastPoint);    
                        Intersection nextNextNextNextSoutheastIntersection = innerForNextNextNextEastIntersection.getNextSoutheastIntersection();

                        pointList.addAll(pointsTo);

                        if (innerForNextNextNextNextEastIntersection != null &&
                                nextNextNextNextSoutheastIntersection.otherInner.equals(innerForNextNextNextNextEastIntersection)) {
                            pointList.add(nextNextNextNextEastIntersection.startPt);
                            pointList.add(nextNextNextNextEastIntersection.endPt);
                        } else {
                            nextNextNextNextEastIntersection = nextNextNextNextSoutheastIntersection;
                            innerForNextNextNextNextEastIntersection = nextNextNextNextSoutheastIntersection.mainInner;

                            pointList.add(nextNextNextNextSoutheastIntersection.endPt);

                            if(nextNextNextNextEastIntersection.outer == null){
                                pointList.add(nextNextNextNextSoutheastIntersection.startPt);
                            }
                        }
                    }

                    if (nextNextNextNextEastIntersection.outer != null) {
                        topEastOuterIntersection = nextNextNextNextEastIntersection;
                    } else {
                        Intersection nextNextNextNextNextEastIntersection = innerForNextNextNextNextEastIntersection.getTheEastIntersection();
                        InnerBoundary innerForNextNextNextNextNextEastIntersection = nextNextNextNextNextEastIntersection.otherInner;
                      
                        if (inner.getTheEastIntersection().otherInner == nextNextNextNextNextEastIntersection.mainInner) {
                            List<Coordinate> pointsTo = innerForNextNextNextNextEastIntersection.getPointsBetween(nextNextNextNextEastIntersection.endPt, inner.getTheEastIntersection().endPt, false);
                            pointList.addAll(pointsTo);

                            pointList.add(inner.getTheEastIntersection().endPt);
                            pointList.add(inner.getTheEastIntersection().startPt);
                            return null;
                        } else {
                            Coordinate lastPoint = pointList.get(pointList.size() - 1);
                            List<Coordinate> pointsTo = innerForNextNextNextNextEastIntersection.getPointsToNextEastgoingIntersection(lastPoint);    
                            Intersection nextNextNextNextNextSoutheastIntersection = innerForNextNextNextNextEastIntersection.getNextSoutheastIntersection();
                            
                            pointList.addAll(pointsTo);

                            if (innerForNextNextNextNextEastIntersection != null &&
                                    nextNextNextNextNextSoutheastIntersection.otherInner.equals(innerForNextNextNextNextEastIntersection)) {
                                pointList.add(nextNextNextNextNextEastIntersection.startPt);
                                pointList.add(nextNextNextNextNextEastIntersection.endPt);
                            } else {
                                nextNextNextNextNextEastIntersection = nextNextNextNextNextSoutheastIntersection;
                                innerForNextNextNextNextNextEastIntersection = nextNextNextNextNextSoutheastIntersection.mainInner;

                                pointList.add(nextNextNextNextNextSoutheastIntersection.endPt);

                                if(nextNextNextNextNextEastIntersection.outer == null){
                                    pointList.add(nextNextNextNextNextSoutheastIntersection.startPt);
                                }
                            }
                        }
                        
                        if (nextNextNextNextNextEastIntersection.outer != null) {
                            topEastOuterIntersection = nextNextNextNextNextEastIntersection;
                        } else {
                            return null;
                        }
                    }
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
        InnerBoundary nextWestInner = nextOuterIntersection.mainInner;
        Intersection nextWestIntersection = nextWestInner.getTheWestIntersection();

        pointList.add(nextOuterIntersection.startPt);

        List<Intersection> otherIntersections = nextWestInner.getTheOtherIntersections();

        int otherIndex = -1;
        Intersection theOtherIntersection = null;

        if (inner.getTheEastIntersection().otherInner == nextWestInner) {
            otherIndex = inner.getTheEastIntersection().endIndex;
            theOtherIntersection = inner.getTheEastIntersection();
        } else {
            for (Intersection otherIntersection : otherIntersections) {
                if ((theOtherIntersection == null) || (theOtherIntersection != null
                        && theOtherIntersection.endPt.getLatitude() < otherIntersection.endPt.getLatitude())) {
                    otherIndex = otherIntersection.endIndex;
                    theOtherIntersection = otherIntersection;
                }
            }
        }

        if (theOtherIntersection != null) {
            List<Coordinate> pointsToNext = nextWestInner.getPointsBetween(nextWestInner.getNextEast(), theOtherIntersection.endPt, false);

            pointList.addAll(pointsToNext);

            pointList.add(theOtherIntersection.endPt);
            pointList.add(theOtherIntersection.startPt);

            // TODO - this should finish when the inner equals theEastIntersection.otherInner
            if (inner.getTheEastIntersection().otherInner != nextWestInner) {
                pointList.add(theOtherIntersection.endPt);
                pointList.add(theOtherIntersection.startPt);
                pointList.add(theOtherIntersection.mainInner.getPoints().get(theOtherIntersection.mainInner.getNorthIndex()));
                pointList.add(theOtherIntersection.mainInner.getNextWest());
                List<Coordinate> pointsToNextAgain = theOtherIntersection.mainInner.getPointsBetween(theOtherIntersection.mainInner.getNextWest(), theOtherIntersection.startPt, false);
                pointList.addAll(pointsToNextAgain);
            }
            
            pointList.add(inner.getTheEastIntersection().endPt);
            pointList.add(inner.getTheEastIntersection().startPt);
            pointList.add(inner.getNextEast());
        } else {
            // todo might need to add multiple more easterly inners before this
            // 
            InnerBoundary otherInner = inner.getTheEastIntersection().otherInner;
            
            if(otherInner != null){
            pointList.add(inner.getTheEastIntersection().otherInner.getTheEastIntersection().endPt);
            pointList.add(inner.getTheEastIntersection().otherInner.getTheEastIntersection().startPt);
            List<Coordinate> pointsToNext = inner.getTheEastIntersection().otherInner.getPointsBetween(inner.getTheEastIntersection().otherInner.getTheEastIntersection().startPt, inner.getTheEastIntersection().endPt, false);

            for (Coordinate thePoint : pointsToNext) {
                pointList.add(thePoint);
            }
            }

//            pointList.add(nextWestIntersection.endPt);
        }


        pointList.add(inner.getTheEastIntersection().endPt);
        pointList.add(inner.getTheEastIntersection().startPt);
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
        // debug code
//        if (generatedPoints.isEmpty()) {
//            return inner.getClosedBoundary();
//        }

        return generatedPoints;
    }

    @Override
    public boolean mustBeAdded() {
        return getInner().shouldGenerateNorth();
    }

    @Override
    public boolean isNorth() {
        return true;
    }
}
