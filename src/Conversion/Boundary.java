
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import java.util.List;

/**
 *
 * @author alan
 */
public interface Boundary {

    int getNorthIndex();

    List<Coordinate> getPoints();

    int getSouthIndex();

    boolean isIsClockwise();
    
    boolean isInner();
    
}
