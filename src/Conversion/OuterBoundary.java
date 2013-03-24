/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.ArrayList;
import java.util.Collections;
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
    private List<Intersection> intersections;

    OuterBoundary(List<Coordinate> thePoints) {
        points = thePoints;

        northIndex = GeoUtils.nothernmostIndex(points);
        southIndex = GeoUtils.southernmostIndex(points);

        Coordinate nextEast = GeoUtils.nextEasterlyPoint(points, northIndex);
        Coordinate nextWest = GeoUtils.nextWesterlyPoint(points, northIndex);

        Coordinate directionCheck;

        if (northIndex > 0) {
            directionCheck = points.get(northIndex - 1);
        } else {
            directionCheck = points.get(points.size() - 2);
        }

        isClockwise = directionCheck.equals(nextEast) ? false : true;

        intersections = new ArrayList<>();
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

    public void addIntersection(Intersection theIntersection) {
        // need this to be sorted???
        intersections.add(theIntersection);
        IntersectionEndIndexComparator theComparator = new IntersectionEndIndexComparator(points);
        Collections.sort(intersections, theComparator);
    }

    public Intersection getNextIntersection(Intersection targetIntersection) {
        Intersection nextIntersection = null;
        int intSize = intersections.size();
        int i = 0;

        if (targetIntersection.outer != null) {
            boolean foundTargetIntersection = false;
            while (!foundTargetIntersection) {
                Intersection other = intersections.get(i);
                if (other.endPt.equals(targetIntersection.endPt)) {
                    foundTargetIntersection = true;
                } else {
                    ++i;
                }
            }

            if (i > 0) {
                Intersection prevIntersection = intersections.get(i - 1);
                int x = 0;
            }

            if ((i + 1) < intersections.size()) {
                nextIntersection = intersections.get(i + 1);
            } else {
                nextIntersection = intersections.get(0);
            }
        }

        if (nextIntersection == null) {
            int x = 0;
        }

        return nextIntersection;
    }

    // postcon - must have a prev cos the boundary is circular
    public Intersection getPrevIntersection(Intersection theIntersection) {
        Intersection prevIntersection = null;

        if (theIntersection.outer != null) {
            int i = 0;
            boolean found = false;
            while (!found) {
                Intersection other = intersections.get(i);
                if (other.endPt.equals(theIntersection.endPt)) {
                    found = true;
                } else {
                    ++i;
                }
            }

            if ((i - 1) >= 0) {
                prevIntersection = intersections.get(i - 1);
            } else {
                prevIntersection = intersections.get(intersections.size() - 1);
            }
        }

        if (prevIntersection == null) {
            int x = 0;
        }

        return prevIntersection;
    }

    void followFromTo(int startIndex,
            int endIndex,
            List<Coordinate> pointList,
            Coordinate prevIntersectionEndPt,
            Coordinate nextIntersectionEndPt) {
        int i = startIndex;

        if (startIndex == endIndex) {
            pointList.add(points.get(startIndex));
            pointList.add(points.get(startIndex + 1));
            return;
        }

        if (startIndex < endIndex) {
            while (i < endIndex) {
                pointList.add(points.get(i + 1));
                ++i;
            }

            pointList.add(nextIntersectionEndPt);  // TODO - not sure if this is added twice
        } else {
            while (i < points.size() - 1) {
                pointList.add(points.get(i + 1));
                ++i;
            }

            i = 0;
            while (i < endIndex) {
                pointList.add(points.get(i + 1));
                ++i;
            }

            pointList.add(prevIntersectionEndPt);
        }
    }
}
