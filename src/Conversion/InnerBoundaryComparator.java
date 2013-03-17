/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import java.util.Comparator;

/**
 *
 * @author alan
 */
public class InnerBoundaryComparator implements Comparator<InnerBoundary>{
    @Override
    public int compare(InnerBoundary o1, InnerBoundary o2) {
        double lat1 = o1.getPoints().get(o1.getNorthIndex()).getLatitude();
        double lat2 = o2.getPoints().get(o2.getNorthIndex()).getLatitude();
        return (lat1 > lat2 ? -1 : (lat1 == lat2 ? 0 : 1));
    }        
}
