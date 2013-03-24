package Conversion;

import java.io.File;
import java.io.FilenameFilter;

public class Filter implements FilenameFilter {

    protected String pattern;

    public Filter(String str) {
        pattern = str;
    }

    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(pattern.toLowerCase());
    }

    public static void main(String args[]) {

//    if (args.length != 1) {
//       System.err.println ("usage: java Filter   ex. java Filter java");
//       return;
//    }

        Filter nf = new Filter(".kml");

        // current directory
        File dir = new File(".");
        String[] strs = dir.list(nf);

        for (int i = 0; i < strs.length; i++) {
            System.out.println(strs[i]);
        }
    }
}