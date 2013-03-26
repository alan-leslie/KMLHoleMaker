
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import java.util.List;

/**
 *
 * @author alan
 */
public interface Slice {

    OuterIndices getOuterIndices();
    
    InnerBoundary getInner();

    Placemark getPlacemark();

    Polygon getPolygon();

    void generatePoints();
    
    List<Coordinate> getGeneratedPoints();

    public boolean mustBeAdded();
}
