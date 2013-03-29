
package Conversion;

import java.util.Comparator;

/**
 *
 * @author alan
 */
public class SliceComparator implements Comparator<Slice> {
    @Override
    public int compare(Slice o1, Slice o2) {
        int size1 = o1.getOuterIndices().noOfPairs();
        int size2 = o2.getOuterIndices().noOfPairs();
        
        if(size1 > size2){
            return -1;
        }
        
        if(size1 < size2){
            return 1;
        }
        
        if(o1.isNorth()){
            if(o2.isNorth()){
                return 0;
            } else {
            return -1;
            }
        } else {
            if(o2.isNorth()){
                return 1;
            }
        }
        
        return 0;
    }
}
