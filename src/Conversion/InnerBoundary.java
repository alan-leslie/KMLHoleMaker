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
    
    InnerBoundary(List<Coordinate> thePoints){
        points = thePoints;
        
        northIndex = GeoUtils.nothernmostIndex(points);

        nextEast = GeoUtils.nextEasterlyPoint(points, northIndex);
        nextWest = GeoUtils.nextWesterlyPoint(points, northIndex);
        
        Coordinate directionCheck = points.get(northIndex - 1);
        
        isClockwise = directionCheck.equals(nextEast) ? false : true;        
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
    public List<Coordinate> getSouthPoints(){
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
}
