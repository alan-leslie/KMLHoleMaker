
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author alan
 */
public class BoundaryIntersectComparator implements Comparator<BoundaryIntersect> {

    private final Coordinate startPoint;

    BoundaryIntersectComparator(Coordinate startPt) {
        startPoint = startPt;
    }

    @Override
    public int compare(BoundaryIntersect o1, BoundaryIntersect o2) {
        double distanceTo1 = GeoUtils.distance(startPoint, o1.getIntersectPoint());
        double distanceTo2 = GeoUtils.distance(startPoint, o2.getIntersectPoint());

        return (distanceTo1 < distanceTo2 ? -1 : (distanceTo1 == distanceTo2 ? 0 : 1));
    }
}
