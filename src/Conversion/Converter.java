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

                        for (Boundary innerBoundary : innerBoundaryIs) {
                            List<Boundary> emptyInner = new ArrayList<>();
                            InnerBoundary theInner = new InnerBoundary(innerBoundary.getLinearRing().getCoordinates());

                            Placemark northPlacemark = thePlacemark.clone();
                            Polygon northPolygon = (Polygon) northPlacemark.getGeometry();
                            List<Coordinate> northCoords = getNorthSlice(theInner, theOuter);
                            northPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(northCoords);
                            northPolygon.setInnerBoundaryIs(emptyInner);
                            theConvertedObjects.add(northPlacemark);

                            Placemark southPlacemark = thePlacemark.clone();
                            Polygon southPolygon = (Polygon) southPlacemark.getGeometry();
                            List<Coordinate> southCoords = getSouthSlice(theInner, theOuter);
                            southPolygon.getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
                            southPolygon.setInnerBoundaryIs(emptyInner);
                            theConvertedObjects.add(southPlacemark);
                        }
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

    // TODO - sort out for anticlockwise polygons
    static List<Coordinate> getSouthSlice(InnerBoundary inner, OuterBoundary outer) {
        List<Coordinate> newCoords = new ArrayList<>();

        Coordinate nextEast = inner.getNextEast();
        Coordinate nextWest = inner.getNextWest();

        Coordinate eastOuter = GeoUtils.findIntersect(nextEast, 90.0, outer.getPoints());
        Coordinate westOuter = GeoUtils.findIntersect(nextWest, -90.0, outer.getPoints());
        int eastIndex = GeoUtils.findIntersectSegmentIndex(eastOuter, outer.getPoints());
        int westIndex = GeoUtils.findIntersectSegmentIndex(westOuter, outer.getPoints());

        newCoords.add(eastOuter);

        // different pattern depending on whether clockwise or not
        // if east less than west may not mean the it it is clockwise???
        if (eastIndex < westIndex) {
            for (int i = eastIndex; i < westIndex; ++i) {
                Coordinate segmentEnd = outer.getPoints().get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            for (int i = westIndex; i < westIndex; ++i) {
                Coordinate segmentStart = i == 0 ? outer.getPoints().get(outer.getPoints().size() - 2) : outer.getPoints().get(i - 1);
                Coordinate segmentEnd = outer.getPoints().get(i);

                newCoords.add(segmentEnd);
            }
        }

        newCoords.add(westOuter);
        newCoords.add(nextWest);

        List<Coordinate> southPoints = inner.getSouthPoints();
        for(Coordinate thePoint: southPoints){
            newCoords.add(thePoint);
        }
        
        newCoords.add(nextEast);
        newCoords.add(eastOuter);

        return newCoords;
    }

    static List<Coordinate> getNorthSlice(InnerBoundary inner, OuterBoundary outer) {
        List<Coordinate> newCoords = new ArrayList<>();
        int northIndex = inner.getNorthIndex();

        Coordinate nextEast = inner.getNextEast();
        Coordinate nextWest = inner.getNextWest();

        Coordinate eastOuter = GeoUtils.findIntersect(nextEast, 90.0, outer.getPoints());
        Coordinate westOuter = GeoUtils.findIntersect(nextWest, -90.0, outer.getPoints());
        int eastIndex = GeoUtils.findIntersectSegmentIndex(eastOuter, outer.getPoints());
        int westIndex = GeoUtils.findIntersectSegmentIndex(westOuter, outer.getPoints());

        newCoords.add(eastOuter);
        newCoords.add(nextEast);
        newCoords.add(inner.getPoints().get(northIndex));
        newCoords.add(nextWest);
        newCoords.add(westOuter);

        // different pattern depending on whether clockwise or not
        if (eastIndex < westIndex) {
            int noOfSegments = outer.getPoints().size() - 1;
            for (int i = westIndex; i < noOfSegments; ++i) {
                Coordinate segmentEnd = outer.getPoints().get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }

            for (int i = 0; i < eastIndex; ++i) {
                Coordinate segmentEnd = outer.getPoints().get(i + 1);
                newCoords.add(new Coordinate(segmentEnd.getLongitude(), segmentEnd.getLatitude()));
            }
        } else {
            for (int i = westIndex; i < westIndex; ++i) {
                Coordinate segmentStart = i == 0 ? outer.getPoints().get(outer.getPoints().size() - 2) : outer.getPoints().get(i - 1);
                Coordinate segmentEnd = outer.getPoints().get(i);

                newCoords.add(segmentEnd);
            }
        }

        newCoords.add(eastOuter);

        return newCoords;
    }
}
