/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alan
 */
public class Slicer {
    private final OuterBoundary outer;
    
    Slicer(OuterBoundary theBoundary){
        outer = theBoundary;
    }
    
    public void addInner(InnerBoundary theInner){
        
    }
    
    // TODO - sort out for anticlockwise polygons
    static List<Coordinate> getInnerSouthSlice(InnerBoundary inner, OuterBoundary outer) {
        List<Coordinate> newCoords = new ArrayList<>();

        Coordinate nextEast = inner.getNextEast();
        Coordinate nextWest = inner.getNextWest();

        Coordinate eastOuter = GeoUtils.findIntersect(nextEast, 90.0, outer.getPoints());
        Coordinate westOuter = GeoUtils.findIntersect(nextWest, -90.0, outer.getPoints());
        int eastIndex = GeoUtils.findIntersectSegmentIndex(eastOuter, outer.getPoints());
        int westIndex = GeoUtils.findIntersectSegmentIndex(westOuter, outer.getPoints());

        newCoords.add(eastOuter);

        // different pattern depending on whether clockwise or not
        // if east less than west may not mean the it it is clockwise???
        if (eastIndex < westIndex) {
            for (int i = eastIndex; i < westIndex; ++i) {
                Coordinate segmentEnd = outer.getPoints().get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            for (int i = westIndex; i < westIndex; ++i) {
                Coordinate segmentStart = i == 0 ? outer.getPoints().get(outer.getPoints().size() - 2) : outer.getPoints().get(i - 1);
                Coordinate segmentEnd = outer.getPoints().get(i);

                newCoords.add(segmentEnd);
            }
        }

        newCoords.add(westOuter);
        newCoords.add(nextWest);

        List<Coordinate> southPoints = inner.getSouthPoints(false);
        for(Coordinate thePoint: southPoints){
            newCoords.add(thePoint);
        }
        
        newCoords.add(nextEast);
        newCoords.add(eastOuter);

        return newCoords;
    }

    static List<Coordinate> getInnerNorthSlice(InnerBoundary inner, OuterBoundary outer) {
        List<Coordinate> newCoords = new ArrayList<>();
        int northIndex = inner.getNorthIndex();

        Coordinate nextEast = inner.getNextEast();
        Coordinate nextWest = inner.getNextWest();

        Coordinate eastOuter = GeoUtils.findIntersect(nextEast, 90.0, outer.getPoints());
        Coordinate westOuter = GeoUtils.findIntersect(nextWest, -90.0, outer.getPoints());
        int eastIndex = GeoUtils.findIntersectSegmentIndex(eastOuter, outer.getPoints());
        int westIndex = GeoUtils.findIntersectSegmentIndex(westOuter, outer.getPoints());

        newCoords.add(eastOuter);
        newCoords.add(nextEast);
        newCoords.add(inner.getPoints().get(northIndex));
        newCoords.add(nextWest);
        newCoords.add(westOuter);

        // different pattern depending on whether clockwise or not
        if (eastIndex < westIndex) {
            int noOfSegments = outer.getPoints().size() - 1;
            for (int i = westIndex; i < noOfSegments; ++i) {
                Coordinate segmentEnd = outer.getPoints().get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }

            for (int i = 0; i < eastIndex; ++i) {
                Coordinate segmentEnd = outer.getPoints().get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            for (int i = westIndex; i < westIndex; ++i) {
                Coordinate segmentStart = i == 0 ? outer.getPoints().get(outer.getPoints().size() - 2) : outer.getPoints().get(i - 1);
                Coordinate segmentEnd = outer.getPoints().get(i);

                newCoords.add(segmentEnd);
            }
        }

        newCoords.add(eastOuter);

        return newCoords;
    }
}
