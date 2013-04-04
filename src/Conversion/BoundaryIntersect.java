/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.Objects;

/**
 *
 * @author alan
 */
public class BoundaryIntersect {
    private Boundary boundary;
    private Coordinate intersect;
    
    BoundaryIntersect(Boundary theBoundary, Coordinate theIntersect){
        boundary = theBoundary;
        intersect = theIntersect;
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public Coordinate getIntersectPoint() {
        return intersect;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.boundary);
        hash = 53 * hash + Objects.hashCode(this.intersect);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BoundaryIntersect other = (BoundaryIntersect) obj;
        if (!Objects.equals(this.boundary, other.boundary)) {
            return false;
        }
        if (!Objects.equals(this.intersect, other.intersect)) {
            return false;
        }
        return true;
    } 
}
