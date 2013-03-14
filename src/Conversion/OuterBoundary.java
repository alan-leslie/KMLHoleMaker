/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.List;

// TODO - putb in invariants that identify this as a boundary
// polygon 
// at leats four points
// start and end are the same

/**
 *
 * @author alan
 */
public class OuterBoundary {
    private final List<Coordinate> points;
    private final boolean isClockwise;
    private final int northIndex;
    private final int southIndex;
    
    OuterBoundary(List<Coordinate> thePoints){
        points = thePoints;
        
        northIndex = GeoUtils.nothernmostIndex(points);
        southIndex = GeoUtils.southernmostIndex(points);
        
        Coordinate nextEast = GeoUtils.nextEasterlyPoint(points, northIndex);
        Coordinate nextWest = GeoUtils.nextWesterlyPoint(points, northIndex);
        
        Coordinate directionCheck;
        
        if(northIndex > 0){
            directionCheck = points.get(northIndex - 1);
        } else {
            directionCheck = points.get(points.size() - 2);            
        }
        
        isClockwise = directionCheck.equals(nextEast) ? false : true;        
    }  

    public List<Coordinate> getPoints() {
        return points;
    }

    public boolean isIsClockwise() {
        return isClockwise;
    }

    public int getNorthIndex() {
        return northIndex;
    }

    public int getSouthIndex() {
        return southIndex;
    }
    
    
}
