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
public class IntersectionEndIndexComparator implements Comparator<Intersection>{
    @Override
    public int compare(Intersection o1, Intersection o2) {
        return (o1.endIndex < o2.endIndex ? -1 : (o1.endIndex == o2.endIndex ? 0 : 1));
    }    
}
