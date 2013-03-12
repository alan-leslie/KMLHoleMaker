package Main;

/*
 * 
 *
 */
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

        Logger theLogger = Main.makeLogger();

        Collection featureCollection = null;
        FileInputStream fis = null;

        try {
            File file = new File(theInputFile);
            if (file == null) {
                return;
            }

            fis = new FileInputStream(file);

            Kml theKML = Kml.unmarshal(fis);
            
            Converter theConverter = new Converter(theKML, theLogger);
            theConverter.convert();
            Kml theConvrtedKML = theConverter.getConvertedKML();

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
