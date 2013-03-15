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
public class Intersection {
    InnerBoundary mainInner;
    OuterBoundary outer;
    InnerBoundary otherInner;
    
    Coordinate startPt;
    Coordinate endPt;
    int endIndex;
    
    boolean isEast;
    
    Intersection(InnerBoundary theInner, OuterBoundary theOuter, boolean theDirection){
        mainInner = theInner;
        outer = theOuter;
        isEast = theDirection;
        
        if(isEast){
            startPt = theInner.getNextEast();       
            endPt = GeoUtils.findIntersect(startPt, 90.0, outer.getPoints());
        } else {
            startPt = theInner.getNextWest();
            endPt = GeoUtils.findIntersect(startPt, -90.0, outer.getPoints());
        }

        endIndex = GeoUtils.findIntersectSegmentIndex(endPt, outer.getPoints());
    }

    void updateIntersection(InnerBoundary innerBoundary) {
        Coordinate newEndPt = null;
        
        if(isEast){
            newEndPt = GeoUtils.findIntersect(startPt, 90.0, innerBoundary.getPoints());
        } else {
            newEndPt = GeoUtils.findIntersect(startPt, -90.0, innerBoundary.getPoints());
        } 
        
        if(newEndPt != null){
            // need to check whether it is closer to the inner 
            // than the curent end
            endPt = newEndPt;
            endIndex = GeoUtils.findIntersectSegmentIndex(endPt, innerBoundary.getPoints());
            otherInner = innerBoundary; 
            outer = null;  
        }
    }
}
