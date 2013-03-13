/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alan
 */
public class PolygonTest {

    static List<Coordinate> pentagonOuter = new ArrayList<>();
    static List<Coordinate> pentagonInner = new ArrayList<>();
    static Boundary thePentagon = new Boundary();

    public PolygonTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        pentagonOuter.add(new Coordinate(-77.05788457660967, 38.87253259892824));
        pentagonOuter.add(new Coordinate(-77.05465973756702, 38.87291016281703));
        pentagonOuter.add(new Coordinate(-77.05315536854791, 38.87053267794386));
        pentagonOuter.add(new Coordinate(-77.05552622493516, 38.868757801256));
        pentagonOuter.add(new Coordinate(-77.05844056290393, 38.86996206506943));
        pentagonOuter.add(new Coordinate(-77.05788457660967, 38.87253259892824));
        // pentagon outer
//                        <coordinates> -77.05788457660967,38.87253259892824,100
//                  -77.05465973756702,38.87291016281703,100
//                  -77.05315536854791,38.87053267794386,100
//                  -77.05552622493516,38.868757801256,100
//                  -77.05844056290393,38.86996206506943,100
//                  -77.05788457660967,38.87253259892824,100 </coordinates>

        pentagonInner.add(new Coordinate(-77.05668055019126, 38.87154239798456));
        pentagonInner.add(new Coordinate(-77.05542625960818, 38.87167890344077));
        pentagonInner.add(new Coordinate(-77.05485125901024, 38.87076535397792));
        pentagonInner.add(new Coordinate(-77.05577677433152, 38.87008686581446));
        pentagonInner.add(new Coordinate(-77.05691162017543, 38.87054446963351));
        pentagonInner.add(new Coordinate(-77.05668055019126, 38.87154239798456));
//                <coordinates> -77.05668055019126,38.87154239798456,100
//                  -77.05542625960818,38.87167890344077,100
//                  -77.05485125901024,38.87076535397792,100
//                  -77.05577677433152,38.87008686581446,100
//                  -77.05691162017543,38.87054446963351,100
//                  -77.05668055019126,38.87154239798456,100 </coordinates>

        LinearRing theRing = new LinearRing();
        theRing.setCoordinates(pentagonOuter);
        thePentagon.setLinearRing(theRing);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPolygonPartition() {
        int northIndex = Converter.nothernmostIndex(pentagonInner);

        Coordinate nextEast = Converter.nextEasterlyPoint(pentagonInner, northIndex);
        Coordinate nextWest = Converter.nextWesterlyPoint(pentagonInner, northIndex);

        Coordinate eastOuter = Converter.findIntersect(nextEast, 90.0, thePentagon);
        int eastIndex = Converter.findIntersectSegmentIndex(nextEast, 90.0, thePentagon);
        assertEquals(1, eastIndex);
        assertEquals(38.87076534375156D, eastOuter.getLatitude(), 0.000001D);
        assertEquals(-77.05330258436979D, eastOuter.getLongitude(), 0.000001D);
        
        Coordinate westOuter = Converter.findIntersect(nextWest, -90.0, thePentagon);
        int westIndex = Converter.findIntersectSegmentIndex(nextWest, 270.0D, thePentagon);
        assertEquals(4, westIndex);
        assertEquals(38.87154238940861D, westOuter.getLatitude(), 0.000001D);
        assertEquals(-77.05809875598462D, westOuter.getLongitude(), 0.000001D);             
    }
}
