package Conversion;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author al
 */
public class Converter {

    private final Kml theInputKML;
    private Kml theConvertedKML = null;
    private final Logger theLogger;
    private final Period thePeriod;
    private final String theURL;
    private final String theTitle;
    private final String theDescription;

    public Converter(Kml theKML, String theTitle, String theDescription, String theURL, Period thePeriod, Logger theLogger) {
        this.theInputKML = theKML;
        this.theTitle = theTitle;
        this.theDescription = theDescription;
        this.theURL = theURL;
        this.thePeriod = thePeriod;
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
        List<StyleSelector> emptyStyles = new ArrayList<>();

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
                        List<Boundary> emptyInner = new ArrayList<>();

                        int i = 0;
                        for (Boundary innerBoundary : innerBoundaryIs) {
                            InnerBoundary theInner = new InnerBoundary(i, theOuter, innerBoundary.getLinearRing().getCoordinates());
                            innerBoundaries.add(theInner);
                            ++i;
                        }

                        List<Intersection> theIntersections = buildIntersections(innerBoundaries, theOuter);

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
                        List<Slice> theSlices = new ArrayList<>();

                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark newNorthPlacemark = getPlacemarkCleanCopy(thePlacemark);

//                            if (i == 20){  // || i == 11){ 
                            NorthSlice theNorthSlice = new NorthSlice(theOuter, inner, newNorthPlacemark);
                            theNorthSlice.generatePoints();
                            theSlices.add(theNorthSlice);
//                            }

                            Placemark newSouthPlacemark = getPlacemarkCleanCopy(thePlacemark);

//                            if(i == 10 || i == 7 || i == 11){ // || i == 12 || i == 13){
//                            if(i == 2){ // || i == 2)){
                            SouthSlice theSouthSlice = new SouthSlice(theOuter, inner, newSouthPlacemark);
                            theSouthSlice.generatePoints();
                            theSlices.add(theSouthSlice);
//                            }

                            ++i;
                        }

                        List<Slice> generatedSlices = new ArrayList<>();
                        SliceComparator sliceComparator = new SliceComparator();
                        Collections.sort(theSlices, sliceComparator);

                        i = 0;
                        for (Slice theSlice : theSlices) {
                            boolean shouldAdd = theSlice.mustBeAdded();

                            if (!shouldAdd) {
                                shouldAdd = true;
                                   
                                OuterIndices theseIndices = theSlice.getOuterIndices();
                                
                                if(theseIndices.noOfPairs() > 0){
                                    for (Slice generatedSlice : generatedSlices) {
                                        OuterIndices generatedSouthIndices = generatedSlice.getOuterIndices();

                                        if (generatedSouthIndices.contains(theseIndices)) {
                                            shouldAdd = false;
                                        }
                                    }
                                }
                            }

                            if (shouldAdd) {
//                                if(i == 1){
                                List<Coordinate> southCoords = theSlice.getGeneratedPoints();

                                if (!southCoords.isEmpty()) {
                                    generatedSlices.add(theSlice);
                                    theSlice.getPolygon().getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
                                    theSlice.getPolygon().setInnerBoundaryIs(emptyInner);
                                    theConvertedObjects.add(theSlice.getPlacemark());
                                }
//                                }
                            }

                            ++i;
                        }
                    }
                } catch (ClassCastException exc) {
                    // ...
                }
            }

            List<Feature> unconvertedObjects = collectUnconverted(theObjects, allInnerBoundaryIs);
            for (Feature unconverted : unconvertedObjects) {
                theConvertedObjects.add(unconverted);
            }

            theConvertedFolder.setFeature(theConvertedObjects);
            theConvertedFeatures.add(theConvertedFolder);
        }

        theConvertedDocument.setFeature(theConvertedFeatures);
    }

    Placemark getPlacemarkCleanCopy(Placemark thePlacemark) {
        Placemark newPlacemark = thePlacemark.clone();
        newPlacemark.setName(theTitle);
        newPlacemark.setDescription(theDescription);
        newPlacemark.setStyleSelector(new ArrayList<StyleSelector>());
        TimeSpan theTimeSpan = new TimeSpan();
        theTimeSpan.setBegin(thePeriod.getStartDate().toGMTString());
        theTimeSpan.setEnd(thePeriod.getEndDate().toGMTString());
        newPlacemark.setTimePrimitive(theTimeSpan);
        ExtendedData extendedData = newPlacemark.getExtendedData();

        if (extendedData == null) {
            extendedData = new ExtendedData();
            List<Data> adjustedExtendedData = new ArrayList<>();
            Data theURLData = new Data("Url");
            theURLData.setName("Url");
            theURLData.setValue(theURL);
            adjustedExtendedData.add(theURLData);
            extendedData.setData(adjustedExtendedData);
            newPlacemark.setExtendedData(extendedData);
        } else {
            extendedData.setSchemaData(new ArrayList<SchemaData>());
            List<Data> adjustedExtendedData = new ArrayList<>();
            Data theURLData = new Data("Url");
            theURLData.setName("Url");
            theURLData.setValue(theURL);
            adjustedExtendedData.add(theURLData);
            extendedData.setData(adjustedExtendedData);
        }
        return newPlacemark;
    }

    private List<Intersection> buildIntersections(List<InnerBoundary> innerBoundaries,
            OuterBoundary theOuter) {
        List<Intersection> theIntersections = new ArrayList<>();

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

        return theIntersections;
    }

    List<Feature> collectUnconverted(List<Feature> theObjects, List<Boundary> allInnerBoundaryIs) {
        List<Feature> theUnconvertedObjects = new ArrayList<>();

        for (Feature theObject : theObjects) {
            Placemark thePlacemark = (Placemark) theObject;

            try {
                Polygon thePolygon = (Polygon) thePlacemark.getGeometry();
                Boundary outer = thePolygon.getOuterBoundaryIs();
                List<Boundary> innerBoundaryIs = thePolygon.getInnerBoundaryIs();

                if (outer == null) {
                    Placemark unchangedPlacemark = thePlacemark.clone();
                    theUnconvertedObjects.add(unchangedPlacemark);
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
                            Placemark adjustedPlacemark = getPlacemarkCleanCopy(thePlacemark);
                            theUnconvertedObjects.add(adjustedPlacemark);
                        }
                    }
                }
            } catch (ClassCastException ex) {
                //...
            }
        }

        return theUnconvertedObjects;
    }
}
