/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;

/**
 *
 * @author alan
 */
public class Slice {
    private Coordinate northEast;
    private Coordinate northWest;
    private Coordinate southEast;
    private Coordinate southWest;
    
    private int neRef;
    private int nwRef;
    private int seRef;
    private int swRef;  
    
    Slice(){
        
    }
    
    public Coordinate getNorthEast() {
        return northEast;
    }

    public void setNorthEast(Coordinate northEast) {
        this.northEast = northEast;
    }

    public Coordinate getNorthWest() {
        return northWest;
    }

    public void setNorthWest(Coordinate northWest) {
        this.northWest = northWest;
    }

    public Coordinate getSouthEast() {
        return southEast;
    }

    public void setSouthEast(Coordinate southEast) {
        this.southEast = southEast;
    }

    public Coordinate getSouthWest() {
        return southWest;
    }

    public void setSouthWest(Coordinate southWest) {
        this.southWest = southWest;
    }

    public int getNeRef() {
        return neRef;
    }

    public void setNeRef(int neRef) {
        this.neRef = neRef;
    }

    public int getNwRef() {
        return nwRef;
    }

    public void setNwRef(int nwRef) {
        this.nwRef = nwRef;
    }

    public int getSeRef() {
        return seRef;
    }

    public void setSeRef(int seRef) {
        this.seRef = seRef;
    }

    public int getSwRef() {
        return swRef;
    }

    public void setSwRef(int swRef) {
        this.swRef = swRef;
    }
}
