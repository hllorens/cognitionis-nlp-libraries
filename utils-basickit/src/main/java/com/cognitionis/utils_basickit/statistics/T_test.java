package com.cognitionis.utils_basickit.statistics;

import com.cognitionis.utils_basickit.StringUtils;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class T_test {

    /*
     * Returns a t-statistic for a before-after paired experiment of 10 samples
     *
     */
    public static String paired_t_test(double[] data) {
        String output;
        double t = 0.0;
        double two_tail = -1.0;
        boolean significant = false;

        int count = data.length;

        if (count != 10) {
            System.err.println("Paired t-test only for 10 samples...");
            System.exit(0);
        }

        double mean = 0.0;
        for (int fold = 0; fold < 10; fold++) {
            mean += data[fold];
        }
        mean /= count;

        double variance = 0.0;
        for (int fold = 0; fold < 10; fold++) {
            variance += Math.pow(data[fold] - mean, 2);
        }
        variance /= count;

        double stdev = Math.sqrt(variance);

        // paired t-test
        t = (mean - 0.0) / (stdev / Math.sqrt(count));

        if (t >= 1.38) {
            two_tail = 0.2;
            significant = true;
        }
        if (t >= 1.83) {
            two_tail = 0.1;
        }
        if (t >= 2.26) {
            two_tail = 0.05;
        }
        if (t >= 3.25) {
            two_tail = 0.001;
        }

        output = "mean=" + StringUtils.twoDecPosS(mean) + " stdev=" + StringUtils.twoDecPosS(stdev) + " t=" + StringUtils.twoDecPosS(t) + " two_tail=" + two_tail + " one_tail=" + StringUtils.twoDecPosS(two_tail / 2) + " confidence>95=" + significant;

        return output;
    }

    public static String latex_t_test(String ttest) {

        String output;
        String tout = "";
        String[] ttestarr = ttest.trim().split(" ");
        if (Double.parseDouble(ttestarr[0].substring(5)) < 0.0) {
            tout = "negative";
        }else{
        if (ttest.endsWith("false")) {
            tout = ttestarr[2].substring(2) + " (not-sg)";
        }
        }
        if (tout.equals("")) {
            String p=ttestarr[4].substring(9);
            if(p.equals("0.00")){
                p+="5";
            }
            tout = ttestarr[2].substring(2) + " (" + p + ")";
        }

        output = ttestarr[0].substring(5) + " \t& " + ttestarr[1].substring(6) + " 	\t& " + tout + "\\\\";

        return output;
    }


}
