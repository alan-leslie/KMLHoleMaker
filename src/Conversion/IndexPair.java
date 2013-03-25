
package Conversion;

/**
 *
 * @author alan
 */
public class IndexPair {
    private final int first;
    private final int second;
    
    IndexPair(int first, int second){
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }
    


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.first;
        hash = 97 * hash + this.second;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexPair other = (IndexPair) obj;
        if (this.first != other.first) {
            return false;
        }
        if (this.second != other.second) {
            return false;
        }
        return true;
    }
}
