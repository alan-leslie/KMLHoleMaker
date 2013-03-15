package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author al TDOD - remove dupicate points add intermediate points
 */
public class Converter {

    private Kml theInputKML = null;
    private Kml theConvertedKML = null;
    private Logger theLogger = null;

    public Converter(Kml theKML, Logger theLogger) {
        this.theInputKML = theKML;
        this.theLogger = theLogger;
    }

    public Kml getInputKML() {
        return theInputKML;
    }

    public Kml getConvertedKML() {
        return theConvertedKML;
    }

    public void convert() {
        theConvertedKML = theInputKML.clone();
        Document theDocument = (Document) theInputKML.getFeature();
        List<Feature> theDocumentFeatures = theDocument.getFeature();
        Document theConvertedDocument = (Document) theConvertedKML.getFeature();
        List<Feature> theConvertedFeatures = new ArrayList<>();

        for (Feature theFeature : theDocumentFeatures) {
            Folder theFolder = (Folder) theFeature;
            List<Feature> theObjects = theFolder.getFeature();
            Folder theConvertedFolder = theFolder.clone();
            List<Feature> theConvertedObjects = new ArrayList<>();

            for (Feature theObject : theObjects) {
                Placemark thePlacemark = (Placemark) theObject;
                // this not going to work cos there can only be one
                // outer per placemear 
                // so need to replace single placemark with multiples

                try {
                    Polygon thePolygon = (Polygon) thePlacemark.getGeometry();
                    Boundary outer = thePolygon.getOuterBoundaryIs();
                    List<Boundary> innerBoundaryIs = thePolygon.getInnerBoundaryIs();
                    List<Boundary> newInner = new ArrayList<>();
                    OuterBoundary theOuter = new OuterBoundary(outer.getLinearRing().getCoordinates());

                    if (!(innerBoundaryIs.isEmpty())) {
                        // precon - outer must be closed polygon
                        // more than two different points

                        // this is the important bit and it should create new placemarks
                        // but start with cutting the pentagon
                        LinearRing newRing = thePolygon.getOuterBoundaryIs().getLinearRing().clone();
                        List<InnerBoundary> innerBoundaries = new ArrayList<>();
                        List<Intersection> theIntersections = new ArrayList<>();
                        List<Boundary> emptyInner = new ArrayList<>();

                        int i = 0;
                        for (Boundary innerBoundary : innerBoundaryIs) {
                            InnerBoundary theInner = new InnerBoundary(i, theOuter, innerBoundary.getLinearRing().getCoordinates());
                            innerBoundaries.add(theInner);
                            ++i;
                        }

                        for (InnerBoundary inner : innerBoundaries) {
                            Intersection west = new Intersection(inner, theOuter, false);
                            Intersection east = new Intersection(inner, theOuter, true);

                            for (InnerBoundary innerBoundary2 : innerBoundaries) {
                                if (!inner.equals(innerBoundary2)) {
                                    west.updateIntersection(innerBoundary2);
                                    east.updateIntersection(innerBoundary2);
                                }
                            }

                            theIntersections.add(east);
                            theIntersections.add(west);

                            inner.addEast(east);
                            inner.addWest(west);

                            if (east.outer != null) {
                                east.outer.addIntersection(east);
                            }

                            if (west.outer != null) {
                                west.outer.addIntersection(west);
                            }
                        }

                        // lots fo two way connections here but safe enough 
                        // because they are set now - read only from now on

                        for (Intersection theIntersection : theIntersections) {
                            if (theIntersection.otherInner != null) {
                                theIntersection.otherInner.addOtherIntersection(theIntersection);
                            }
                        }

                        // TODO - most north generated by northernmost inner
                        // that goes west to outer
                        // most south generated by southernmost inner that 
                        // goes east to outer
                        // otherwise generated by getBottom 
                        // still another special case for get top where
                        // west goes to outer bound but east to inner
                        // also probably for southe where east goes to outer
                        // but west goes to inner
                
                        i = 0;
                        InnerBoundaryComparator latComparator = new InnerBoundaryComparator();
                        Collections.sort(innerBoundaries, latComparator);
                        
                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark northPlacemark = thePlacemark.clone();
                            Polygon northPolygon = (Polygon) northPlacemark.getGeometry();
                            if (!(inner.getNorthIndex() == 42 || 
                                    inner.getNorthIndex() == 0 ||
                                    inner.getNorthIndex() == 55 ||
                                    inner.getNorthIndex() == 36)) {
                                List<Coordinate> northCoords = inner.getTopPoints();
                                if (!northCoords.isEmpty()) {
                                    northPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(northCoords);
                                    northPolygon.setInnerBoundaryIs(emptyInner);
                                    theConvertedObjects.add(northPlacemark);
                                }
                            }
                            ++i;
                        }

                        i = 0;
                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark southPlacemark = thePlacemark.clone();
                            Polygon southPolygon = (Polygon) southPlacemark.getGeometry();
//                            if (i == innerBoundaries.size() - 3) {
                            List<Coordinate> southCoords = inner.getBottomPoints();

                            if (!southCoords.isEmpty()) {
                                southPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
                                southPolygon.setInnerBoundaryIs(emptyInner);
                                theConvertedObjects.add(southPlacemark);
                            }
//                            }

                            ++i;
                        }
//                        i = 0;
//                        for (Boundary innerBoundary : innerBoundaryIs) {
//                            List<Boundary> emptyInner = new ArrayList<>();
//                            InnerBoundary theInner = new InnerBoundary(i, innerBoundary.getLinearRing().getCoordinates());
//                            ++i;
//                            
//                            Placemark northPlacemark = thePlacemark.clone();
//                            Polygon northPolygon = (Polygon) northPlacemark.getGeometry();
//                            List<Coordinate> northCoords = null; //getNorthSlice(theInner, theOuter);
//                            northPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(northCoords);
//                            northPolygon.setInnerBoundaryIs(emptyInner);
//                            theConvertedObjects.add(northPlacemark);
//
//                            Placemark southPlacemark = thePlacemark.clone();
//                            Polygon southPolygon = (Polygon) southPlacemark.getGeometry();
//                            List<Coordinate> southCoords = null; //getSouthSlice(theInner, theOuter);
//                            southPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
//                            southPolygon.setInnerBoundaryIs(emptyInner);
//                            theConvertedObjects.add(southPlacemark);
//                        }
                    }
                } catch (ClassCastException exc) {
                    // ...
                }
            }

            theConvertedFolder.setFeature(theConvertedObjects);
            theConvertedFeatures.add(theConvertedFolder);
        }

        theConvertedDocument.setFeature(theConvertedFeatures);
    }
}
