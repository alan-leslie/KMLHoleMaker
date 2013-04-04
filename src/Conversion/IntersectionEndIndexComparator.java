
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author alan
 */
public class IntersectionEndIndexComparator implements Comparator<Intersection> {

    private final List<Coordinate> outerPoints;

    IntersectionEndIndexComparator(List<Coordinate> outerPoints) {
        this.outerPoints = outerPoints;
    }

    @Override
    public int compare(Intersection o1, Intersection o2) {
        if (o1.endIndex < o2.endIndex) {
            return -1;
        } else {
            if (o1.endIndex == o2.endIndex) {
                Coordinate startPoint = outerPoints.get(o1.endIndex - 1);
                double distanceTo1 = GeoUtils.distance(startPoint, o1.endPt);
                double distanceTo2 = GeoUtils.distance(startPoint, o2.endPt);

                return (distanceTo1 < distanceTo2 ? -1 : (distanceTo1 == distanceTo2 ? 0 : 1));
            } else {
                return 1;
            }
        }
    }
}
