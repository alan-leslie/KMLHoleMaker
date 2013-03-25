/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author alan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({Conversion.DistanceTest.class, Conversion.OuterIndicesTest.class, Conversion.PolygonTest.class, Conversion.IntersectionTest.class, Conversion.EatWestTest.class})
public class ConverterTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
