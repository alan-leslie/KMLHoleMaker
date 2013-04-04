/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.Comparator;

/**
 *
 * @author alan
 */
public class EndPointComparator implements Comparator<Coordinate> {
    Coordinate startPoint;
    
    EndPointComparator(Coordinate theStartPt){
        this.startPoint = theStartPt;
        
    }

    @Override
    public int compare(Coordinate o1, Coordinate o2) {
        double distanceTo1 = GeoUtils.distance(startPoint, o1);
        double distanceTo2 = GeoUtils.distance(startPoint, o2);

        return (distanceTo1 < distanceTo2 ? -1 : (distanceTo1 == distanceTo2 ? 0 : 1));
    }  
}
