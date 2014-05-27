package com.cognitionis.utils_basickit;

import java.util.Arrays;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class StringUtils {

    /**
     * Returns if a char is ASCII (fastest than the generic function: existsInEncoding(char c, String encoding))
     * @param c
     * @return
     */
    public static boolean isASCII(char c) {
        return (((int) c >> 7) == 0);
    }

    /**
     * Returns if a char is ISO-8859-1 (fastest than the generic function: existsInEncoding(char c, String encoding))
     * @param c
     * @return
     */
    public static boolean isISO_8859_1(char c) {
        return (((int) c) < 256);
    }

    /**
     * Returns if a char is of an encoding
     * @param c
     * @return
     */
    public static boolean existsInEncoding(char c, String encoding) {
        try {
            String s = "" + c;
            byte bytes[] = s.getBytes(encoding);
            String s2 = new String(bytes, encoding);
            return (s2.equals(s));
        } catch (Exception e) {
            System.err.println("Errors found (StringUtils):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return false;
        }
    }

    /**
     * Returns if a string is of an encoding
     * @param s
     * @return
     */
    public static boolean existsInEncoding(String s, String encoding) {
        try {
            byte bytes[] = s.getBytes(encoding);
            String s2 = new String(bytes, encoding);
            return (s2.equals(s));
        } catch (Exception e) {
            System.err.println("Errors found (StringUtils):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return false;
        }
    }

    public static int countOccurrencesOf(String source, char pattern) {
        int count = 0;
        if (source != null) {
            int found = -1;
            int start = 0;


            while ((found = source.indexOf(pattern, start)) != -1) {
                start = found + 1;
                count++;
            }
            return count;
        } else {
            return 0;
        }
    }

    public static int countOccurrencesOf(String source, String pattern) {
        int count = 0;
        if (source != null) {
            final int len = pattern.length();
            int found = -1;
            int start = 0;


            while ((found = source.indexOf(pattern, start)) != -1) {
                start = found + len;
                count++;
            }
            return count;
        } else {
            return 0;
        }
    }

    public static String twoDecPosS(double d) {
        Double a;
        String twoDec;
        int negative_inc = 0;
        int decimal_pos = 0;

        a = (((int) Math.round((d) * 100.0)) / 100.0);
        twoDec = a.toString();

        decimal_pos = twoDec.lastIndexOf('.');
        if (decimal_pos == -1) {
            twoDec += ".";
            decimal_pos = twoDec.length();
        }
        twoDec += "00";

        twoDec = twoDec.substring(0, decimal_pos + 3);

        return twoDec;
    }

public static <T> T[] concatArray(T[] first, T[] second) {
  T[] result = Arrays.copyOf(first, first.length + second.length);
  System.arraycopy(second, 0, result, first.length, second.length);
  return result;
}


}
