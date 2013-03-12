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
public class IntersectionTest {
    static List<Coordinate> pentagonOuter = new ArrayList<>();
    static List<Coordinate> pentagonInner = new ArrayList<>();
    static List<Coordinate> scaleneEast = new ArrayList<>();
    static List<Coordinate> scaleneWest = new ArrayList<>();

    static List<Coordinate> northSegment = new ArrayList<>();
    static List<Coordinate> southSegment = new ArrayList<>();
    static List<Coordinate> eastSegment = new ArrayList<>();
    static List<Coordinate> westSegment = new ArrayList<>();

    public IntersectionTest() {
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
        
        scaleneEast.add(new Coordinate(0.0D, 50.0D));
        scaleneEast.add(new Coordinate(10.0D, 50.0D));
        scaleneEast.add(new Coordinate(5.0D, 50.0D));
        scaleneEast.add(new Coordinate(0.0D, 50.0D));

        scaleneWest.add(new Coordinate(0.0D, 50.0D));
        scaleneWest.add(new Coordinate(-10.0D, 50.0D));
        scaleneWest.add(new Coordinate(-5.0D, 50.0D));
        scaleneWest.add(new Coordinate(0.0D, 50.0D));
        
        northSegment.add(new Coordinate(0.0D, 0.0D));
        northSegment.add(new Coordinate(0.0D, 50.0D));
        
        southSegment.add(new Coordinate(0.0D, 0.0D));
        southSegment.add(new Coordinate(0.0D, -50.0D));

        eastSegment.add(new Coordinate(0.0D, 0.0D));
        eastSegment.add(new Coordinate(50.0D, 0.0D));
        
        westSegment.add(new Coordinate(0.0D, 0.0D));
        westSegment.add(new Coordinate(-50.0D, 0.0D));
   }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testIsInSegment() {
        Coordinate coord1 = new Coordinate(0.0D, 20.0D);
        boolean inSeg1 = Converter.isInSegment(northSegment.get(0), northSegment.get(1), coord1);
        assertEquals(true, inSeg1);      
        boolean inSeg2 = Converter.isInSegment(southSegment.get(0), southSegment.get(1), coord1);
        assertEquals(false, inSeg2); 
        
        Coordinate coord2 = new Coordinate(20.0D, 0.0D);
        boolean inSeg3 = Converter.isInSegment(eastSegment.get(0), eastSegment.get(1), coord2);
        assertEquals(true, inSeg3);      
        boolean inSeg4 = Converter.isInSegment(westSegment.get(0), westSegment.get(1), coord2);
        assertEquals(false, inSeg4);      
    }
    
    @Test
    public void testBearings() {
         double brng1 = Converter.getInitialBearing(northSegment.get(0), northSegment.get(1));       
         assertEquals(0.0D, brng1, 0.001D);      
         double brng2 = Converter.getInitialBearing(southSegment.get(0), southSegment.get(1));       
         assertEquals(180.0D, brng2, 0.001D);      
         double brng3 = Converter.getInitialBearing(eastSegment.get(0), eastSegment.get(1));       
         assertEquals(90.0D, brng3, 0.001D);      
         double brng4 = Converter.getInitialBearing(westSegment.get(0), westSegment.get(1));       
         assertEquals(-90.0D, brng4, 0.001D);      
    }  
    
    @Test
    public void testSimpleIntersect() {
         Coordinate testPoint1 = new Coordinate(0.0D, 0.0D);
         Coordinate testPoint2 = new Coordinate(10.0D, -10.0D);
         Coordinate theIntersect1 = Converter.calculateLatLonIntersection(testPoint1, 90.0D, testPoint2, 0.0D);       
         assertEquals(0.0D, theIntersect1.getLatitude(), 0.001D);      
         assertEquals(10.0D, theIntersect1.getLongitude(), 0.001D); 
         
         Coordinate testPoint3 = new Coordinate(10.0D, 10.0D);
         Coordinate theIntersect2 = Converter.calculateLatLonIntersection(testPoint1, -90.0D, testPoint3, 0.0D);       
         assertEquals(0.0D, theIntersect2.getLatitude(), 0.001D);      
         assertEquals(-170.0D, theIntersect2.getLongitude(), 0.001D);  
         
         Coordinate testPoint4 = new Coordinate(-10.0D, 10.0D);
         Coordinate theIntersect3 = Converter.calculateLatLonIntersection(testPoint1, 44.5615D, testPoint4, 0.0D);       
         assertEquals(10.0D, theIntersect3.getLatitude(), 0.001D);      
         assertEquals(170.0D, theIntersect3.getLongitude(), 0.001D);      
    }        
}
