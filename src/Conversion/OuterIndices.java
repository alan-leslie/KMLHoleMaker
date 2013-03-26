
package Conversion;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alan
 */
public class OuterIndices {
    private List<IndexPair> outerIndices;
    
    OuterIndices(){
        outerIndices = new ArrayList<>();
    }
    
    void add(IndexPair thePair){
        outerIndices.add(thePair);
    }
    
    int noOfPairs(){
        return outerIndices.size();
    }
    
    boolean contains(OuterIndices other){
        boolean retVal = false;
        int noOfMatches = 0;
        
        for(IndexPair thePair: outerIndices){
            for(IndexPair theOther: other.outerIndices){
                if(thePair.equals(theOther)){
                    ++noOfMatches;
                }           
            }           
        }
        
        if(noOfMatches == other.outerIndices.size()){
            retVal = true;
        }
        
        return retVal;
    } 
}
