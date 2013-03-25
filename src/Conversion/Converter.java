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
                        int noOfGenerated = 0;
                        List<NorthSlice> northSlices = new ArrayList<>();
                        List<SouthSlice> southSlices = new ArrayList<>();
                        
                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark northPlacemark = getPlacemarkCleanCopy(thePlacemark);

//                            Polygon northPolygon = (Polygon) northPlacemark.getGeometry();
                            if (inner.shouldGenerateNorth()) {
//                                if(i > 16){
                                NorthSlice theNorthSlice = new NorthSlice(theOuter, inner, northPlacemark);
                                northSlices.add(theNorthSlice);
//                                }

                                ++noOfGenerated;
                            }

                            ++i;
                        }
                        
                        for(NorthSlice northSlice: northSlices){
                            List<Coordinate> northCoords = northSlice.getTopPoints();
                            
                            if (!northCoords.isEmpty()) {
                                northSlice.getPolygon().getOuterBoundaryIs().getLinearRing().setCoordinates(northCoords);
                                northSlice.getPolygon().setInnerBoundaryIs(emptyInner);
                                theConvertedObjects.add(northSlice.getPlacemark());
                            }                          
                        }

                        i = 0;
                        Collections.reverse(innerBoundaries);
                        for (InnerBoundary inner : innerBoundaries) {
                            Placemark southPlacemark = getPlacemarkCleanCopy(thePlacemark);

//                            Polygon southPolygon = (Polygon) southPlacemark.getGeometry();
                                SouthSlice theSouthSlice = new SouthSlice(theOuter, inner, southPlacemark);
                                southSlices.add(theSouthSlice);
//                            }

                            ++i;
                        }
                        
                        for(SouthSlice southSlice: southSlices){
                            boolean shouldAddSouth = true;
                            // todo - if prev north generated and...
                            // don't generate south
                            // has to come down to something like ihf the intersections
                            // have been done done't do them again...
                            southSlice.generatePoints();
                            OuterIndices southIndices = southSlice.getOuterIndices();
                            
                            for(NorthSlice northSlice: northSlices){
                                OuterIndices northIndices = northSlice.getOuterIndices();
                                
                                if(northIndices.contains(southIndices)){
                                    shouldAddSouth = false;
                                }
                                // if the indexPairs in this slice are already 
                                // covered by the northSlice then don't generate'
                            }
//                            if (innerBoundaries.size() > 1 && i == innerBoundaries.size() - 1) {
//                                InnerBoundary prevInner = innerBoundaries.get(i - 1);
//                                if (prevInner.shouldGenerateNorth()) {
//                                    shouldGenerateSouth = false;
//                                }
//
//                                // try a test of inner boundary east goest to outer
//                                // and there is an other intersection on the east sie that
//                                // goes to outer
//                            }
                            if (shouldAddSouth) {
                                List<Coordinate> southCoords = southSlice.getBottomPoints();

                                if (!southCoords.isEmpty()) {
                                    southSlice.getPolygon().getOuterBoundaryIs().getLinearRing().setCoordinates(southCoords);
                                    southSlice.getPolygon().setInnerBoundaryIs(emptyInner);
                                    theConvertedObjects.add(southSlice.getPlacemark());
                                }     
                            }
                        }                        
                    }
                } catch (ClassCastException exc) {
                    // ...
                }
            }

            List<Feature> unconvertedObjects = collectUnconverted(theObjects, allInnerBoundaryIs);
            for(Feature unconverted: unconvertedObjects){
//                theConvertedObjects.add(unconverted);
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
        theTimeSpan.setBegin(thePeriod.getStartDate().toString());
        theTimeSpan.setEnd(thePeriod.getEndDate().toString());
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
