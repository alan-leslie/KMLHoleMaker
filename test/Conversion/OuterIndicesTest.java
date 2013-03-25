/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

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
public class OuterIndicesTest {
    
    public OuterIndicesTest() {
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
    public void testContains() {
        OuterIndices ind1 = new OuterIndices();
        
        boolean doesContain1 = ind1.contains(ind1);
        assertEquals(true, doesContain1);
        
        IndexPair pair1 = new IndexPair(1,2);
        IndexPair pair2 = new IndexPair(3,4);
        IndexPair pair3 = new IndexPair(5,6);
        
        ind1.add(pair1);
        ind1.add(pair2);
        ind1.add(pair3);
        
        boolean doesContain2 = ind1.contains(ind1);
        assertEquals(true, doesContain2);
        
        
        OuterIndices ind2 = new OuterIndices();

        boolean doesContain3 = ind1.contains(ind2);
        assertEquals(true, doesContain3);
        
        ind2.add(pair3);
        ind2.add(pair2);
   
        boolean doesContain4 = ind1.contains(ind2);
        assertEquals(true, doesContain4);
        
        IndexPair pair4 = new IndexPair(7,8);
        ind2.add(pair4);
        
        boolean doesContain5 = ind1.contains(ind2);
        assertEquals(false, doesContain5);
    }
}
