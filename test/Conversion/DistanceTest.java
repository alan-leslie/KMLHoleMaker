/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
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
public class DistanceTest {
    
    public DistanceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
    public void testDistance() {
        Coordinate pt1 = new Coordinate(1.0D, 56.0D);
        Coordinate pt2 = new Coordinate(0.0D, 56.0D);
        double distance = GeoUtils.distance(pt1, pt2);  
        assertEquals(62.18D, distance, 0.1D);   
    }
}
