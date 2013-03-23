package Main;

/*
 * 
 *
 */
import Conversion.Converter;
import Conversion.Filter;
import Conversion.Period;
import Conversion.PeriodMap;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author al
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    // TODO -
    public static void main(String[] args) {
        Properties properties = new Properties();
        FileInputStream is = null;

        try {
            is = new FileInputStream("KMLHoleMaker.properties");
            properties.load(is);
        } catch (IOException e) {
            // ...
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    /* .... */
                }
            }
        }

        String theInputFile = properties.getProperty("InputFileName", "pentagon.kml");
        String theOutputFile = properties.getProperty("OutputFileName", "output.kml");
        String theInputDirectory = properties.getProperty("InputDirectoryPath", "");
        String theOutputDirectory = properties.getProperty("outputDirectoryPath", "./converted");

        Logger theLogger = Main.makeLogger();

        File dir = new File(theInputDirectory);
        Filter nf = new Filter(".kml");
        String[] strs = dir.list(nf);
        PeriodMap thePeriods = PeriodMap.getInstance();

        for (int i = 0; i < strs.length; i++) {
            FileInputStream fis = null;
            String inputFileName = theInputDirectory + "/" + strs[i];
            
            System.out.println("Processing file:" + inputFileName);

            try {
                File file = new File(inputFileName);
                if (file == null) {
                    return;
                }

                fis = new FileInputStream(file);

                Kml theKML = Kml.unmarshal(fis);
                
                String theName = strs[i].substring(0, strs[i].length() - 4);
                String theURL = "http://data.london.gov.uk/datastore/package/policeuk-crime-data";
                String theTitleStart = "London robberies ";
                String theTileMiddle = ": occurrence density = ";
                String theDescriptionStart = "This layer shows locations of robberies in London for ";
                String theDescriptionMiddle = "with a occurence density of ";
                String theDescriptionEnd = ".";
                
                int underscoreIndex = 0;
                String theOccurenceDensity = "2";
                String monthStr = "Mar";
                String theYearStr = "11";; 
                
                String theNamePrefix = "policeMar11";
                
                if(!theName.equalsIgnoreCase("innerBoundTest")){
                    theOccurenceDensity = theName.substring(underscoreIndex + 1);
                    underscoreIndex = theName.indexOf("_");
                    monthStr = theName.substring(6, 9);
                    theYearStr = theName.substring(9, 11);
                    theNamePrefix = theName.substring(0, underscoreIndex);
                }

                String theDate = monthStr + " 20" + theYearStr;
                
                String theTitle = theTitleStart + theDate + theTileMiddle + theOccurenceDensity;
                String theDescription = theDescriptionStart + theDate + theDescriptionMiddle + theOccurenceDensity + theDescriptionEnd; 

                Period thePeriod = thePeriods.getPeriod(theNamePrefix);

                Converter theConverter = new Converter(theKML, theTitle, theDescription, theURL, thePeriod, theLogger);
                theConverter.convert();
                Kml theConvrtedKML = theConverter.getConvertedKML();
                
                theOutputFile = theOutputDirectory + "/" + strs[i];

                theConvrtedKML.marshal(new File(theOutputFile));
                // todo - check that output is being closed
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (null != fis) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        /* .... */
                    }
                }
            }
        }
    }

    /**
     *
     * @return - valid logger (single file).
     */
    static Logger makeLogger() {
        Logger lgr = Logger.getLogger("KMLHoleMaker");
        lgr.setUseParentHandlers(false);
        lgr.addHandler(simpleFileHandler());
        return lgr;
    }

    /**
     *
     * @return - valid file handler for logger.
     */
    private static FileHandler simpleFileHandler() {
        try {
            FileHandler hdlr = new FileHandler("KMLHoleMaker.log");
            hdlr.setFormatter(new SimpleFormatter());
            return hdlr;
        } catch (Exception e) {
            System.out.println("Failed to create log file");
            return null;
        }
    }
}
