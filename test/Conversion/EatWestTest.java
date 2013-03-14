package Conversion;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
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
public class EatWestTest {
    static List<Coordinate> pentagonOuter = new ArrayList<>();
    static List<Coordinate> pentagonInner = new ArrayList<>();
    static List<Coordinate> scaleneEast = new ArrayList<>();
    static List<Coordinate> scaleneWest = new ArrayList<>();
    
    public EatWestTest() {       
    }
    
    @BeforeClass
    public static void setUpClass() {
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
        
        scaleneEast.add(new Coordinate(0.0D, 0.0D));
        scaleneEast.add(new Coordinate(0.0D, -5.0D));
        scaleneEast.add(new Coordinate(10.0D, -10.0D));
        scaleneEast.add(new Coordinate(0.0D, 0.0D));

        scaleneWest.add(new Coordinate(0.0D, 0.0D));
        scaleneWest.add(new Coordinate(0.0D, -5.0D));
        scaleneWest.add(new Coordinate(-10.0D, -10.0D));
        scaleneWest.add(new Coordinate(0.0D, 0.0D));
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testNortherlyPentagon() {
        int theNorthOuter = GeoUtils.nothernmostIndex(pentagonOuter);
        assertEquals(1, theNorthOuter);
        int theSouthOuter = GeoUtils.southernmostIndex(pentagonOuter);
        assertEquals(3, theSouthOuter);
        int theNorthInner = GeoUtils.nothernmostIndex(pentagonInner);
        assertEquals(1, theNorthInner);
        int theSouthInner = GeoUtils.southernmostIndex(pentagonInner);
        assertEquals(3, theSouthInner);
    }
    
    @Test
    public void testEasterlyPentagon() {
        int theNorthOuter = GeoUtils.nothernmostIndex(pentagonOuter);
        Coordinate theEasterly = GeoUtils.nextEasterlyPoint(pentagonOuter, theNorthOuter);
        assertEquals(theEasterly.getLongitude(), -77.053155D, 0.000001D);
        assertEquals(theEasterly.getLatitude(), 38.87053268D, 0.000001D);
    }

    @Test
    public void testWesterlyPentagon() {
        int theNorthOuter = GeoUtils.nothernmostIndex(pentagonOuter);
        Coordinate theWesterly = GeoUtils.nextWesterlyPoint(pentagonOuter, theNorthOuter);
        assertEquals(theWesterly.getLongitude(), -77.05788457660967D, 0.000001D);
        assertEquals(theWesterly.getLatitude(), 38.87253259892824D, 0.000001D);
    }
    
    @Test
    public void testEastScalene() {
        int theNorth1 = GeoUtils.nothernmostIndex(scaleneEast);
        Coordinate theEast1 = GeoUtils.nextEasterlyPoint(scaleneEast, theNorth1);
        assertEquals(10.0D, theEast1.getLongitude(), 0.000001D);
        assertEquals(-10.0D, theEast1.getLatitude(), 0.000001D);
        
        int theNorth2 = GeoUtils.nothernmostIndex(scaleneWest);
        Coordinate theEast2 = GeoUtils.nextEasterlyPoint(scaleneWest, theNorth2);
        assertEquals(0.0D, theEast2.getLongitude(), 0.000001D);
        assertEquals(-5.0D, theEast2.getLatitude(), 0.000001D);
    }
    
    @Test
    public void testWestScalene() {
        int theNorth1 = GeoUtils.nothernmostIndex(scaleneEast);
        Coordinate theWest1 = GeoUtils.nextWesterlyPoint(scaleneEast, theNorth1);
        assertEquals(0.0D, theWest1.getLongitude(), 0.000001D);
        assertEquals(-5.0D, theWest1.getLatitude() , 0.000001D);

        int theNorth2 = GeoUtils.nothernmostIndex(scaleneWest);
        Coordinate theWest2 = GeoUtils.nextWesterlyPoint(scaleneWest, theNorth2);
        assertEquals(-10.0D, theWest2.getLongitude(), 0.000001D);
        assertEquals(-10.0D, theWest2.getLatitude(), 0.000001D);
    }
}
