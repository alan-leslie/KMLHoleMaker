package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    List<Coordinate> outerIntersects;
    OuterBoundary theOuter;
    List<BoundaryIntersect> allIntersects;
    BoundaryIntersectComparator intersectComparator;

    Intersection(InnerBoundary theInner, OuterBoundary theOuter, boolean theDirection) {
        mainInner = theInner;
        outer = theOuter;
        this.theOuter = theOuter;
        isEast = theDirection;
        allIntersects = new ArrayList<>();

        if (isEast) {
            startPt = theInner.getNextEast();
            endPt = GeoUtils.findIntersect(startPt, 90.0, outer.getPoints());
            outerIntersects = GeoUtils.findIntersects(startPt, 90.0, outer.getPoints());
        } else {
            startPt = theInner.getNextWest();
            endPt = GeoUtils.findIntersect(startPt, -90.0, outer.getPoints());
            outerIntersects = GeoUtils.findIntersects(startPt, -90.0, outer.getPoints());
        }

        EndPointComparator endPointComparator = new EndPointComparator(startPt);
        Collections.sort(outerIntersects, endPointComparator);
        // double check that the first is the same as endPt
        if(!outerIntersects.get(0).equals(endPt)){
            // throw exception
        }
        endIndex = GeoUtils.findIntersectSegmentIndex(endPt, outer.getPoints());
        
        for(Coordinate theOuterIntersect: outerIntersects){
            BoundaryIntersect theIntersect = new BoundaryIntersect(outer, theOuterIntersect);
            allIntersects.add(theIntersect);     
        }
        
        intersectComparator = new BoundaryIntersectComparator(startPt);
        Collections.sort(allIntersects, intersectComparator);
    }

    void updateIntersection(InnerBoundary innerBoundary) {
        Coordinate newEndPt = null;

        if (isEast) {
            newEndPt = GeoUtils.findIntersect(startPt, 90.0, innerBoundary.getPoints());
        } else {
            newEndPt = GeoUtils.findIntersect(startPt, -90.0, innerBoundary.getPoints());
        }

        if (newEndPt != null) {
            BoundaryIntersect theIntersect = new BoundaryIntersect(innerBoundary, newEndPt);
            allIntersects.add(theIntersect);
            Collections.sort(allIntersects, intersectComparator);
            
            double distanceToCurrent = GeoUtils.distance(startPt, endPt);
            double distanceToNew = GeoUtils.distance(startPt, newEndPt);

            // need to check whether it is closer to the inner 
            // than the curent end              
            if (distanceToNew < distanceToCurrent) {
                endPt = newEndPt;
                endIndex = GeoUtils.findIntersectSegmentIndex(endPt, innerBoundary.getPoints());
                otherInner = innerBoundary;
                outer = null;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.startPt);
        hash = 79 * hash + Objects.hashCode(this.endPt);
        hash = 79 * hash + this.endIndex;
        hash = 79 * hash + (this.isEast ? 1 : 0);
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
        final Intersection other = (Intersection) obj;
        if (!Objects.equals(this.startPt, other.startPt)) {
            return false;
        }
        if (!Objects.equals(this.endPt, other.endPt)) {
            return false;
        }
        if (this.endIndex != other.endIndex) {
            return false;
        }
        if (this.isEast != other.isEast) {
            return false;
        }
        return true;
    }
}
