
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
        return (size1 > size2 ? -1 : (size1 == size2 ? 0 : 1));
    }
}
