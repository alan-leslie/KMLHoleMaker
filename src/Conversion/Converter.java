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
 * @author al
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
        List<Boundary> allInnerBoundaryIs = new ArrayList<>();

        for (Feature theFeature : theDocumentFeatures) {
            Folder theFolder = (Folder) theFeature;
            List<Feature> theObjects = theFolder.getFeature();
            Folder theConvertedFolder = theFolder.clone();
            List<Feature> theConvertedObjects = new ArrayList<>();

            for (Feature theObject : theObjects) {
                Placemark thePlacemark = (Placemark) theObject;

                try {
                    Polygon thePolygon = (Polygon) thePlacemark.getGeometry();
                    Boundary outer = thePolygon.getOuterBoundaryIs();
                    List<Boundary> innerBoundaryIs = thePolygon.getInnerBoundaryIs();
                    allInnerBoundaryIs.addAll(innerBoundaryIs);
                    OuterBoundary theOuter = new OuterBoundary(outer.getLinearRing().getCoordinates());

                    if (!innerBoundaryIs.isEmpty()) {
                        // precon - outer must be closed polygon
                        // more than two different points

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

                        // lots of two way connections here but safe enough 
                        // because they are set now - read only from now on

                        for (Intersection theIntersection : theIntersections) {
                            if (theIntersection.otherInner != null) {
                                theIntersection.otherInner.addOtherIntersection(theIntersection);
                            }
                        }

                        InnerBoundaryComparator latComparator = new InnerBoundaryComparator();
                        Collections.sort(innerBoundaries, latComparator);

                        // set the southesternmost
                        boolean southEastFound = false;
                        for (i = innerBoundaries.size() - 1; i >= 0 && !southEastFound; --i) {
                            InnerBoundary theInner = innerBoundaries.get(i);

                            if (theInner.getTheEastIntersection().outer != null) {
                                theInner.setIsSoutheasternmost(true);
                                southEastFound = true;
                            }
                        }

                        i = 0;
                        int noOfGenerated = 0;
                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark northPlacemark = thePlacemark.clone();
                            Polygon northPolygon = (Polygon) northPlacemark.getGeometry();
                            if (inner.shouldGenerateNorth()) {
                                List<Coordinate> northCoords = inner.getTopPoints();
                                if (!northCoords.isEmpty()) {
                                    northPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(northCoords);
                                    northPolygon.setInnerBoundaryIs(emptyInner);
                                    theConvertedObjects.add(northPlacemark);
                                }

                                ++noOfGenerated;
                            }

                            ++i;
                        }

                        i = 0;
                        Collections.reverse(innerBoundaries);
                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark southPlacemark = thePlacemark.clone();
                            Polygon southPolygon = (Polygon) southPlacemark.getGeometry();
                            boolean shouldGenerateSouth = true;
                            if (innerBoundaries.size() > 1 && i == innerBoundaries.size() - 1) {
                                InnerBoundary prevInner = innerBoundaries.get(i - 1);
                                if (prevInner.shouldGenerateNorth()) {
                                    shouldGenerateSouth = false;
                                }
                            }
                            if (shouldGenerateSouth) {
                                List<Coordinate> southCoords = inner.getBottomPoints();

                                if (!southCoords.isEmpty()) {
                                    southPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
                                    southPolygon.setInnerBoundaryIs(emptyInner);
                                    theConvertedObjects.add(southPlacemark);
                                }
                            }

                            ++i;
                        }
                    }
                } catch (ClassCastException exc) {
                    // ...
                }
            }

            for (Feature theObject : theObjects) {
                Placemark thePlacemark = (Placemark) theObject;

                try {
                    Polygon thePolygon = (Polygon) thePlacemark.getGeometry();
                    Boundary outer = thePolygon.getOuterBoundaryIs();
                    List<Boundary> innerBoundaryIs = thePolygon.getInnerBoundaryIs();

                    if (outer == null) {
                        Placemark unchangedPlacemark = thePlacemark.clone();
                        theConvertedObjects.add(unchangedPlacemark);
                    } else {
                        if (innerBoundaryIs == null || innerBoundaryIs.isEmpty()) {
                            List<Coordinate> outerPoints = outer.getLinearRing().getCoordinates();
                            boolean alreadyProcessed = false;

                            for (Boundary theBoundary : allInnerBoundaryIs) {
                                List<Coordinate> processedBoundaryPoints = theBoundary.getLinearRing().getCoordinates();

                                if (processedBoundaryPoints.contains(outerPoints.get(0))) {
                                    alreadyProcessed = true;
                                }
                            }

                            if (!alreadyProcessed) {
                                Placemark unchangedPlacemark = thePlacemark.clone();
                                theConvertedObjects.add(unchangedPlacemark);
                            }
                        }
                    }

                } catch (ClassCastException ex) {
                    //...
                }
            }

            theConvertedFolder.setFeature(theConvertedObjects);
            theConvertedFeatures.add(theConvertedFolder);
        }

        theConvertedDocument.setFeature(theConvertedFeatures);
    }
}
