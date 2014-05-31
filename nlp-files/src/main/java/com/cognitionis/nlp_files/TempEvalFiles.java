package com.cognitionis.nlp_files;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import com.cognitionis.utils_basickit.FileUtils;
import com.cognitionis.utils_basickit.XmlAttribs;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class TempEvalFiles {

    public static void divide_nfolds(File file, int folds, boolean includetest) {
        try {
            // create folder data2fold
            file = create_data2fold(file, includetest);

            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE);
            int numlines = lnr.getLineNumber();
            int lines_per_fold = numlines / folds;
            int lines_margin = lines_per_fold / 5; // 20%
            System.err.println("Number of Lines: " + numlines);

            // build 10-fold array with base-segmentation.tab
            ArrayList<String[]> file_markers = get_file_markers(file, folds, lines_per_fold, lines_margin);

            // build the folds for each file given the lines per fold
            File dir = new File((new File(file.getAbsolutePath())).getParent());
            String parent_path = dir.getParent();
            for (int i = 0; i < folds; i++) {
                create_folded_data(file.getName(), parent_path, file_markers, i,"\t");
                create_folded_data("dct.tab", parent_path, file_markers, i,"\t");
                create_folded_data("timex-extents.tab", parent_path, file_markers, i,"\t");
                create_folded_data("timex-attributes.tab", parent_path, file_markers, i,"\t");
                create_folded_data("event-extents.tab", parent_path, file_markers, i,"\t");
                create_folded_data("event-attributes.tab", parent_path, file_markers, i,"\t");
                create_folded_data("base-segmentation.TempEval2-features", parent_path, file_markers, i,"\\|");
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }

    public static File create_data2fold(File file, boolean includetest) {
        File data2fold_file = null;

        try {
            File dir = new File((new File(file.getAbsolutePath())).getParent());
            String parent_path = dir.getParent();

            File newdir = new File(parent_path + File.separator + "data2fold");
            if (!newdir.exists() || !newdir.isDirectory()) {
                newdir.mkdir();
            }

            // copy all the files to newdir
            if ((new File(newdir + File.separator + file.getName()).exists())) {
                (new File(newdir + File.separator + file.getName())).delete();
                (new File(newdir + File.separator + "dct.tab")).delete();
                (new File(newdir + File.separator + "timex-extents.tab")).delete();
                (new File(newdir + File.separator + "timex-attributes.tab")).delete();
                (new File(newdir + File.separator + "event-extents.tab")).delete();
                (new File(newdir + File.separator + "event-attributes.tab")).delete();
                (new File(newdir + File.separator + "base-segmentation.TempEval2-features")).delete();
            }
            FileUtils.copyFileUtil(file, new File(newdir + File.separator + file.getName()));
            FileUtils.copyFileUtil(new File(dir + File.separator + "dct.tab"), new File(newdir + File.separator + "dct.tab"));
            FileUtils.copyFileUtil(new File(dir + File.separator + "timex-extents.tab"), new File(newdir + File.separator + "timex-extents.tab"));
            FileUtils.copyFileUtil(new File(dir + File.separator + "timex-attributes.tab"), new File(newdir + File.separator + "timex-attributes.tab"));
            FileUtils.copyFileUtil(new File(dir + File.separator + "event-extents.tab"), new File(newdir + File.separator + "event-extents.tab"));
            FileUtils.copyFileUtil(new File(dir + File.separator + "event-attributes.tab"), new File(newdir + File.separator + "event-attributes.tab"));
            FileUtils.copyFileUtil(new File(dir + File.separator + "base-segmentation.TempEval2-features"), new File(newdir + File.separator + "base-segmentation.TempEval2-features"));

            // merge test files
            if (dir.getName().endsWith("train") && includetest && (new File(parent_path + File.separator + "test")).exists()) {
                // If test-entities exists then break (bad structure)
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + file.getName()), new File(newdir + File.separator + file.getName()));
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + "dct.tab"), new File(newdir + File.separator + "dct.tab"));
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + "timex-extents.tab"), new File(newdir + File.separator + "timex-extents.tab"));
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + "timex-attributes.tab"), new File(newdir + File.separator + "timex-attributes.tab"));
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + "event-extents.tab"), new File(newdir + File.separator + "event-extents.tab"));
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + "event-attributes.tab"), new File(newdir + File.separator + "event-attributes.tab"));
                FileUtils.copyFileUtilappend(new File(parent_path + File.separator + "test" + File.separator + "base-segmentation.TempEval2-features"), new File(newdir + File.separator + "base-segmentation.TempEval2-features"));
            }


            data2fold_file = new File(newdir + File.separator + file.getName());

        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return data2fold_file;
    }

    public static ArrayList<String[]> get_file_markers(File file, int folds, int lines_per_fold, int lines_margin) {
        ArrayList<String[]> file_markers;
        String[] current_filemarker;
        int linen = 0;

        try {
            file_markers = new ArrayList<String[]>();
            current_filemarker = new String[2];
            BufferedReader reader = new BufferedReader(new FileReader(file));

            try {
                String line;
                String[] tabarr = null;
                String current_fileid = "";
                int current_fold = 1;

                while ((line = reader.readLine()) != null) {
                    linen++;
                    tabarr = line.split("\t");

                    // save the file/line possibility
                    if (!current_fileid.equals(tabarr[0]) && linen >= (lines_per_fold * current_fold - lines_margin)) {
                        current_filemarker = null;
                        current_filemarker = new String[2];
                        current_filemarker[0] = current_fileid;
                        current_fileid = tabarr[0];
                        //System.err.println(linen);
                        current_filemarker[1] = "" + (linen - 1);
                    }

                    if (linen >= (lines_per_fold * current_fold) && current_fold != folds) {
                        int foldlines = Integer.parseInt(current_filemarker[1]);
                        if (current_fold > 1) {
                            foldlines = (Integer.parseInt(current_filemarker[1]) - (Integer.parseInt((file_markers.get((current_fold - 2))[1]))));
                        }
                        System.err.println("Fold: " + current_fold + "/" + folds + " file: " + current_filemarker[0] + " line: " + current_filemarker[1] + " lines: " + foldlines + "/" + lines_per_fold + " (" + (foldlines * 100) / lines_per_fold + "%)");
                        file_markers.add(current_filemarker);
                        current_fold++;
                    }

                }

                current_filemarker = null;
                current_filemarker = new String[2];
                current_filemarker[0] = tabarr[0];
                //System.err.println(linen);
                current_filemarker[1] = "" + (linen);
                int foldlines = (Integer.parseInt(current_filemarker[1]) - (Integer.parseInt((file_markers.get((current_fold - 2))[1]))));
                System.err.println("Fold: " + current_fold + "/" + folds + " file: " + current_filemarker[0] + " line: " + current_filemarker[1] + " lines: " + foldlines + "/" + lines_per_fold + " (" + (foldlines * 100) / lines_per_fold + "%)");
                file_markers.add(current_filemarker);


            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " - line:" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return file_markers;

    }

    public static void create_folded_data(String file, String parent_path, ArrayList<String[]> file_markers, int i, String separator) {
        boolean intrain = true;
        int linen=0;
        try {
                File dirtrain = new File(parent_path + File.separator + "train" + (i + 1));
                if (!dirtrain.exists() || !dirtrain.isDirectory()) {
                    dirtrain.mkdir();
                }
                File dirtest = new File(parent_path + File.separator + "test" + (i + 1));
                if (!dirtest.exists() || !dirtest.isDirectory()) {
                    dirtest.mkdir();
                }

                String firsttestfilemarker = "";
                String lasttestfilemarker = file_markers.get(i)[0];
                if (i != 0) {
                    firsttestfilemarker = file_markers.get(i - 1)[0];
                }

            BufferedReader reader = new BufferedReader(new FileReader(parent_path+File.separator+"data2fold"+File.separator+file));
            BufferedWriter outtrain = new BufferedWriter(new FileWriter(dirtrain + File.separator + file));
            BufferedWriter outtest = new BufferedWriter(new FileWriter(dirtest + File.separator + file));
            try {
                String line;
                String[] tabarr = null;
                boolean lastfile = false;
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(dirtrain + "   " + dirtest + "   " + (i + 1) + "    1-" + firsttestfilemarker + "   2-" + lasttestfilemarker);
                }
                while ((line = reader.readLine()) != null) {
                    linen++;
                    tabarr = line.split(separator);

                    if (firsttestfilemarker.equals("")) {
                        firsttestfilemarker = tabarr[0];
                    }

                    if (firsttestfilemarker.equals(tabarr[0])) {
                        intrain = false;
                    }
                    // check lastfile
                    if (lasttestfilemarker.equals(tabarr[0])) {
                        lastfile = true;
                    }
                    if (!lasttestfilemarker.equals(tabarr[0]) && lastfile) {
                        lastfile = false;
                        intrain = true;
                    }

                    if (intrain) {
                        outtrain.write(line+"\n");
                    } else {
                        outtest.write(line+"\n");
                    }

                }

            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outtrain != null) {
                    outtrain.close();
                }
                if (outtest != null) {
                    outtest.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }

    public static String merge_extents(String features, String extentstab, String elem) {
        String output;

        TabFile tf = new TabFile(extentstab);
        tf.isWellFormatted();
        output = ((TabFile) tf).getPipesFile();
        output = FileUtils.renameTo(output, "-extents\\.tab\\.pipes", "\\.TempEval-extents");
        PipesFile nlpfile = new PipesFile(features);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        String temp = output;
        output = merge_extents(((PipesFile) nlpfile), elem);
        (new File(temp)).delete();

        return output;
    }

    public static String merge_extents(PipesFile pipesfile, String elemext) {
        String outputfile = null;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-annotationKey-" + elemext;
            //String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemext + ".TempEvalFiles-extents";
            String extentsfile = pipesfile.getFile().getParent() + "/" + elemext + ".TempEval-extents";

            PipesFile keypipes = new PipesFile(extentsfile);
            keypipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String extentline;
                String[] extentarr = null;
                String pipesline;
                String[] pipesarr = null;
                String extentId = ""; // save id
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");
                    if (extentarr == null && (extentline = extentsreader.readLine()) != null) {
                        extentarr = extentline.split("\\|");
                        // to avoid joining t1 at the end of a file and t1 at the begining of the next
                        if (!curr_fileid.equals(extentarr[0])) {
                            extentId = "";
                            curr_fileid = extentarr[0];
                        }
                    }

                    if (extentarr != null) {
                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            if (!extentId.equals(extentarr[4])) {
                                outfile.write(pipesline + "|B-" + extentarr[3] + "\n");
                            } else {
                                outfile.write(pipesline + "|I-" + extentarr[3] + "\n");
                            }
                            extentId = extentarr[4];
                            extentarr = null;
                        } else {
                            outfile.write(pipesline + "|O\n");
                            extentId = "";
                        }
                    } else {
                        outfile.write(pipesline + "|O\n");
                        extentId = "";
                    }

                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_extents_class(String features, String extentstab, String elem) {
        String output;

        TabFile tf = new TabFile(extentstab);
        tf.isWellFormatted();
        output = ((TabFile) tf).getPipesFile();
        output = FileUtils.renameTo(output, "-extents\\.tab\\.pipes", "\\.TempEval-extents");
        PipesFile nlpfile = new PipesFile(features);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        String temp = output;
        output = merge_extents_class(((PipesFile) nlpfile), elem);
        (new File(temp)).delete();

        return output;
    }

    public static String merge_extents_class(PipesFile pipesfile, String elemext) {
        String outputfile = null;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-annotationKey-" + elemext + "-class";
            //String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemext + ".TempEvalFiles-extents";
            String extentsfile = pipesfile.getFile().getParent() + "/" + elemext + ".TempEval-extents";
            String attrfile = pipesfile.getFile().getParent() + "/" + elemext + "-attributes.tab";

            /*PipesFile attrpipes = new PipesFile();
            attrpipes.loadFile(new File(attrfile));
            attrpipes.isWellFormedOptimist();*/

            PipesFile keypipes = new PipesFile(extentsfile);
            keypipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader attrsreader = new BufferedReader(new FileReader(attrfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String extentline;
                String[] extentarr = null;
                String attrline;
                String[] attrarr = null;
                String attrclass = "";
                String pipesline;
                String[] pipesarr = null;
                String extentId = ""; // save id
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");
                    if (extentarr == null && (extentline = extentsreader.readLine()) != null) {
                        extentarr = extentline.split("\\|");
                        // to avoid joining t1 at the end of a file and t1 at the begining of the next
                        if (!curr_fileid.equals(extentarr[0])) {
                            extentId = "";
                            curr_fileid = extentarr[0];
                        }
                    }

                    if (extentarr != null) {
                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            if (!extentId.equals(extentarr[4])) {
                                while ((attrline = attrsreader.readLine()) != null) {
                                    //System.out.println(attrline);
                                    attrarr = attrline.split("\t");
                                    if (attrarr[6].matches("(?i)(class|type)")) {
                                        attrclass = attrarr[7];
                                        break;
                                    }
                                }
                                outfile.write(pipesline + "|B-" + extentarr[3] + "-" + attrclass + "\n");
                            } else {
                                outfile.write(pipesline + "|I-" + extentarr[3] + "-" + attrclass + "\n");
                            }
                            extentId = extentarr[4];
                            extentarr = null;
                        } else {
                            outfile.write(pipesline + "|O\n");
                            extentId = "";
                        }
                    } else {
                        outfile.write(pipesline + "|O\n");
                        extentId = "";
                    }

                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (attrsreader != null) {
                    attrsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String split_extents_attrib(PipesFile pipesfile, String attrib) {
        String outputfile = null;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-attribs-" + attrib;
            //String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemext + ".TempEvalFiles-extents";

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String pipesline;
                String[] pipesarr = null;
                int elembiocol = pipesfile.getColumn("element\\(IOB2\\)");


                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");

                    if (pipesarr[elembiocol].equalsIgnoreCase("O")) {
                        outfile.write(pipesline + "|-\n");
                    } else {
                        String attribval = "EMPTY";
                        String[] elemsplit = pipesarr[elembiocol].split("-");
                        if (elemsplit.length >= 3) {
                            attribval = elemsplit[2];
                        }
                        outfile.write(pipesline.substring(0, pipesline.lastIndexOf("-")) + "|");
                        if (elemsplit[0].equalsIgnoreCase("B")) {
                            outfile.write(attrib + "=" + attribval + "\n");
                        } else {
                            outfile.write("-\n");
                        }

                    }
                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_attribs(String features_and_extents, String attribstab, String elem) {
        String output;
        TabFile tf = new TabFile(attribstab);
        tf.isWellFormatted();
        output = ((TabFile) tf).getPipesFile();
        output = FileUtils.renameTo(output, "-attributes\\.tab\\.pipes", "\\.TempEval-attributes");
        PipesFile nlpfile = new PipesFile(features_and_extents);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        String temp = output;
        output = merge_attribs(((PipesFile) nlpfile), elem);
        (new File(temp)).delete();

        return output;
    }

    public static String merge_attribs(PipesFile pipesfile, String elemattr) {
        String outputfile = null;
        int linen = 0;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-attribs";
            //String attrfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemattr + ".TempEvalFiles-attributes";
            String attrfile = pipesfile.getFile().getParent() + "/" + elemattr + ".TempEval-attributes";

            PipesFile attrpipes = new PipesFile(attrfile);
            attrpipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(attrfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            int elemcol = pipesfile.getColumn("element\\(IOB2\\)");

            try {
                String attrline = "";
                String[] attrarr = null;
                String pipesline;
                String[] pipesarr = null;
                String attrId = ""; // save id
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    if (elemcol > pipesarr.length) {
                        elemcol = pipesarr.length - 1;
                    }
                    if (attrarr == null && (attrline = extentsreader.readLine()) != null) {
                        attrarr = attrline.split("\\|");
                        attrId = attrarr[4];
                        curr_fileid = attrarr[0];
                    }

                    if (attrarr != null) {
                        if (pipesarr[0].equals(attrarr[0]) && pipesarr[1].equals(attrarr[1]) && pipesarr[2].equals(attrarr[2])) {
                            if (!pipesarr[elemcol].equals("B-" + attrarr[3])) {
                                throw new Exception("Malformed TempEval attribs file (B-element not found for attribs)\n" + pipesline + "\n" + attrline);
                            }
                            outfile.write(pipesline + "|" + elemattr.substring(0, 1).toLowerCase() + "id=\"" + attrarr[4] + "\" " + attrarr[6] + "=\"" + attrarr[7] + "\"");

                            while ((attrline = extentsreader.readLine()) != null) {
                                attrarr = attrline.split("\\|");
                                if (attrId.equals(attrarr[4]) && curr_fileid.equals(attrarr[0])) {
                                    outfile.write(" " + attrarr[6] + "=\"" + attrarr[7] + "\"");
                                    attrarr = null;
                                } else {
                                    attrId = attrarr[4];
                                    curr_fileid = attrarr[0];
                                    break;
                                }
                            }
                            outfile.write("\n");
                        } else {
                            if (pipesarr[elemcol].equals("B-" + elemattr)) {
                                throw new Exception("Malformed TempEval attribs file (B-" + elemattr + " found with no attribs)\n" + pipesline + "\n" + attrline);
                            }
                            outfile.write(pipesline + "|-\n");
                        }
                    } else {
                        if (pipesarr[elemcol].equals("B-" + elemattr)) {
                            throw new Exception("Malformed TempEval attribs file (B-" + elemattr + " found with no attribs)\n" + pipesline + "\n" + attrline);
                        }
                        outfile.write(pipesline + "|-\n");
                    }
                }
            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " - line:" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_extents_and_attribs(PipesFile pipesfile, String elem) {
        String outputfile = null;
        int linen = 0;
        try {

            outputfile = pipesfile.getFile().getCanonicalPath() + "-annotationKey-" + elem + "-attribs";
            String extentsfile = pipesfile.getFile().getParent() + "/" + elem + "-extents.tab";
            String attrsfile = pipesfile.getFile().getParent() + "/" + elem + "-attributes.tab";



            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader attrsreader = new BufferedReader(new FileReader(attrsfile));

            try {
                String pipesline;
                String[] pipesarr = null;
                String extentline = "";
                String[] extentarr = null;
                String extentId = ""; // save id
                String attrline = "";
                String[] attrarr = null;
                String attrId = ""; // save id
                String curr_fileid = "";


                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    //System.out.println(pipesline);
                    pipesarr = pipesline.split("\\|");
                    if (extentarr == null && (extentline = extentsreader.readLine()) != null) {
                        extentarr = extentline.split("\t");
                        //System.out.println(pipesline+"   "+extentline);

                        // to avoid joining t1 at the end of a file and t1 at the begining of the next
                        if (!curr_fileid.equals(extentarr[0])) {
                            extentId = ""; // new id
                            curr_fileid = extentarr[0];
                        }
                        // only in B-elements
                        if (!extentId.equals(extentarr[4])) { // new id
                            if (attrarr != null) {
                                if (!extentarr[0].equals(attrarr[0]) || !extentarr[1].equals(attrarr[1]) || !extentarr[2].equals(attrarr[2]) || !extentarr[3].equals(attrarr[3]) || !extentarr[4].equals(attrarr[4])) {
                                    throw new Exception("Extents-Attributes incongruence:\n\t" + extentline + "\n\t" + attrline);
                                }
                            } else {
                                if ((attrline = attrsreader.readLine()) == null) {
                                    throw new Exception("Attributes for extents (" + extentline + ") missing");
                                } else {
                                    attrarr = attrline.split("\t");
                                    attrId = attrarr[4];
                                    // corss-check
                                    if (!extentarr[0].equals(attrarr[0]) || !extentarr[1].equals(attrarr[1]) || !extentarr[2].equals(attrarr[2]) || !extentarr[3].equals(attrarr[3]) || !extentarr[4].equals(attrarr[4])) {
                                        throw new Exception("Extents-Attributes incongruence:\n\t" + extentline + "\n\t" + attrline);

                                    }
                                }
                            }
                        }

                    }

                    if (extentarr != null) {

                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            //System.out.println(pipesline+"   "+extentline);
                            if (!extentId.equals(extentarr[4])) {
                                outfile.write(pipesline + "|B-" + extentarr[3] + "|" + elem.substring(0, 1).toLowerCase() + "id=\"" + attrarr[4] + "\" " + attrarr[6] + "=\"" + attrarr[7] + "\"");
                                attrarr = null; // nullify scheme
                                while ((attrline = attrsreader.readLine()) != null) {
                                    attrarr = attrline.split("\t");
                                    if (attrarr[7].trim().equals("")) {
                                        throw new Exception("Empty attribute: " + attrline);
                                    }
                                    //System.out.println(attrId+" "+attrarr[4]+" "+curr_fileid+" "+attrarr[0]);
                                    if (attrId.equals(attrarr[4]) && curr_fileid.equals(attrarr[0])) {
                                        outfile.write(" " + attrarr[6] + "=\"" + attrarr[7] + "\"");
                                        attrarr = null;
                                    } else {
                                        attrId = attrarr[4];
                                        break;
                                    }
                                }
                                outfile.write("\n");
                            } else {
                                outfile.write(pipesline + "|I-" + extentarr[3] + "|-\n");
                            }
                            extentId = extentarr[4];
                            extentarr = null;
                        } else {
                            outfile.write(pipesline + "|O|-\n");
                            extentId = "";
                        }
                    } else {
                        outfile.write(pipesline + "|O|-\n");
                        extentId = "";
                    }

                }


                if (extentarr != null) {
                    throw new Exception("Extents found without tokens correspondence: " + extentline + " " + elem);
                }

                if (attrarr != null) {
                    throw new Exception("Attributes found without tokens correspondence: " + extentline + " " + elem);
                }

                if ((extentline = extentsreader.readLine()) != null) {
                    throw new Exception("Some extents not assigned (" + extentline + ") " + elem);
                }

                if ((attrline = attrsreader.readLine()) != null) {
                    throw new Exception("Some attributes not assigned (" + attrline + ") " + elem);
                }




            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (attrsreader != null) {
                    attrsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " - line:" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String deletespecial_extents_and_attribs(PipesFile pipesfile, String elem) {
        String outputfile = null;
        int linen = 0;
        try {

            String extentsfile = pipesfile.getFile().getParent() + "/" + elem + "-extents.tab";
            String attrsfile = pipesfile.getFile().getParent() + "/" + elem + "-attributes.tab";


            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader attrsreader = new BufferedReader(new FileReader(attrsfile));

            BufferedWriter outextents = new BufferedWriter(new FileWriter(pipesfile.getFile().getParent() + "/" + elem + "-extents.tab2"));
            BufferedWriter outattribs = new BufferedWriter(new FileWriter(pipesfile.getFile().getParent() + "/" + elem + "-attributes.tab2"));


            try {
                String pipesline;
                String[] pipesarr = null;
                String extentline = "";
                String[] extentarr = null;
                String extentId = ""; // save id
                String attrline = "";
                String[] attrarr = null;
                String attrId = ""; // save id
                String curr_fileid = "";


                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    //System.out.println(pipesline);
                    pipesarr = pipesline.split("\\|");
                    if (extentarr == null && (extentline = extentsreader.readLine()) != null) {
                        extentarr = extentline.split("\t");

                        // to avoid joining t1 at the end of a file and t1 at the begining of the next
                        if (!curr_fileid.equals(extentarr[0])) {
                            extentId = ""; // new id
                            curr_fileid = extentarr[0];
                        }
                        // only in B-elements
                        if (!extentId.equals(extentarr[4])) { // new id
                            if (attrarr != null) {
                                if (!extentarr[0].equals(attrarr[0]) || !extentarr[1].equals(attrarr[1]) || !extentarr[2].equals(attrarr[2]) || !extentarr[3].equals(attrarr[3]) || !extentarr[4].equals(attrarr[4])) {
                                    throw new Exception("Extents-Attributes incongruence:\n\t" + extentline + "\n\t" + attrline);
                                }
                            } else {
                                if ((attrline = attrsreader.readLine()) == null) {
                                    throw new Exception("Attributes for extents (" + extentline + ") missing");
                                } else {
                                    attrarr = attrline.split("\t");
                                    attrId = attrarr[4];
                                    // corss-check
                                    if (!extentarr[0].equals(attrarr[0]) || !extentarr[1].equals(attrarr[1]) || !extentarr[2].equals(attrarr[2]) || !extentarr[3].equals(attrarr[3]) || !extentarr[4].equals(attrarr[4])) {
                                        throw new Exception("Extents-Attributes incongruence:\n\t" + extentline + "\n\t" + attrline);

                                    }
                                }
                            }
                        }

                    }

                    if (extentarr != null) {
                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            if (!extentId.equals(extentarr[4])) {
                                //if(pipesarr[4].matches("(?i)(fue|soy|es|eres|somos|sois|son|era|eras|éramos|erais|eran|SEGUIR POR AQUIIIIIIIIIIIIIes)"))
                                if (pipesarr[9].matches("(?i)(ser|estar|haber)")) {
                                    System.out.println(pipesline);
                                } else {
                                    outextents.write(extentline + "\n");
                                    outattribs.write(attrline + "\n");
                                }
                                attrarr = null; // nullify scheme
                                while ((attrline = attrsreader.readLine()) != null) {
                                    attrarr = attrline.split("\t");
                                    if (attrarr[7].trim().equals("")) {
                                        throw new Exception("Empty attribute: " + attrline);
                                    }
                                    //System.out.println(attrId+" "+attrarr[4]+" "+curr_fileid+" "+attrarr[0]);
                                    if (attrId.equals(attrarr[4]) && curr_fileid.equals(attrarr[0])) {
                                        if (!pipesarr[9].matches("(?i)(ser|estar|haber)")) {
                                            outattribs.write(attrline + "\n");
                                        }
                                        attrarr = null;
                                    } else {
                                        attrId = attrarr[4];
                                        break;
                                    }
                                }
                                //outfile.write("\n");
                            } else {
                                throw new Exception("Multi-token event");
                            }
                            extentId = extentarr[4];
                            extentarr = null;
                        } else {
                            extentId = "";
                        }
                    } else {
                        extentId = "";
                    }

                }


                if (extentarr != null) {
                    throw new Exception("Extents found without tokens correspondence: " + extentline + " " + elem);
                }

                if (attrarr != null) {
                    throw new Exception("Attributes found without tokens correspondence: " + extentline + " " + elem);
                }

                if ((extentline = extentsreader.readLine()) != null) {
                    throw new Exception("Some extents not assigned (" + extentline + ") " + elem);
                }

                if ((attrline = attrsreader.readLine()) != null) {
                    throw new Exception("Some attributes not assigned (" + attrline + ") " + elem);
                }




            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (attrsreader != null) {
                    attrsreader.close();
                }
                if (outextents != null) {
                    outextents.close();
                }
                if (outattribs != null) {
                    outattribs.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " - line:" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_attribs_specific(String features_and_extents, String attribstab, String elem, String attrib_re, String newattrname) {
        String output;
        TabFile tf = new TabFile(attribstab);
        tf.isWellFormatted();
        output = ((TabFile) tf).getPipesFile();
        output = FileUtils.renameTo(output, "-attributes\\.tab\\.pipes", "\\.TempEval-attributes");
        PipesFile nlpfile = new PipesFile(features_and_extents);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        String temp = output;
        output = merge_attribs_specific(((PipesFile) nlpfile), elem, attrib_re, newattrname);
        (new File(temp)).delete();

        return output;
    }

    public static String merge_attribs_specific(PipesFile pipesfile, String elemattr, String attr_re, String attr_newname) {
        String outputfile = null;
        int linen = 0;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-attribs";
            //String attrfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemattr + ".TempEvalFiles-attributes";
            String attrfile = pipesfile.getFile().getParent() + "/" + elemattr + ".TempEval-attributes";

            PipesFile attrpipes = new PipesFile(attrfile);
            attrpipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(attrfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            int elemcol = pipesfile.getColumn("element\\(IOB2\\)");

            try {
                String attrline = "";
                String[] attrarr = null;
                String pipesline;
                String[] pipesarr = null;
                String attrId = ""; // save id
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    if (elemcol > pipesarr.length) {
                        elemcol = pipesarr.length - 1;
                    }
                    if (attrarr == null && (attrline = extentsreader.readLine()) != null) {
                        attrarr = attrline.split("\\|");
                        attrId = attrarr[4];
                        curr_fileid = attrarr[0];
                    }

                    if (attrarr != null) {
                        if (pipesarr[0].equals(attrarr[0]) && pipesarr[1].equals(attrarr[1]) && pipesarr[2].equals(attrarr[2])) {
                            if (!pipesarr[elemcol].equals("B-" + attrarr[3])) {
                                throw new Exception("Malformed TempEval attribs file (attribs not in B- element)\n" + pipesline + "\n" + attrline);
                            }
                            outfile.write(pipesline + "|");

                            do {
                                attrarr = attrline.split("\\|");
                                if (attrId.equals(attrarr[4]) && curr_fileid.equals(attrarr[0])) {
                                    if (attrarr[6].matches(attr_re)) {
                                        outfile.write(";" + attr_newname + "=" + attrarr[7]);
                                        attrarr = null;
                                    }
                                } else {
                                    attrId = attrarr[4];
                                    curr_fileid = attrarr[0];
                                    break;
                                }
                            } while ((attrline = extentsreader.readLine()) != null);
                            outfile.write("\n");
                        } else {
                            outfile.write(pipesline + "|-\n");
                        }
                    } else {
                        outfile.write(pipesline + "|-\n");
                    }
                }
            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " - line:" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_classik(String extentsfile, String attribsfile, String attrib) {
        String outputfile = null;
        try {
            outputfile = extentsfile + ".TempEval2-features-annotatedWith-attribs";
            //String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemext + ".TempEvalFiles-extents";

            PipesFile keypipes = new PipesFile(extentsfile);
            keypipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(new File(attribsfile)));

            try {
                String extentline;
                String[] extentarr = null;
                String pipesline;
                String[] pipesarr = null;

                while ((extentline = extentsreader.readLine()) != null) {
                    extentarr = extentline.split("\\|");
                    if (pipesarr == null && (pipesline = pipesreader.readLine()) != null) {
                        pipesarr = pipesline.split("\\|");
                    }

                    if (pipesarr != null) {
                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            outfile.write(extentline + "|" + attrib + "=\"" + pipesarr[pipesarr.length - 1] + "\"\n");
                            pipesarr = null;
                        } else {
                            outfile.write(extentline + "|-\n");
                        }
                    } else {
                        outfile.write(extentline + "|-\n");
                    }

                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_classik(PipesFile pipesfile, String attrib) {
        String outputfile = null;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + ".TempEval-features-annotatedWith-attribs";
            //String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemext + ".TempEvalFiles-extents";
            String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().lastIndexOf('.'));

            PipesFile keypipes = new PipesFile(extentsfile);
            keypipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String extentline;
                String[] extentarr = null;
                String pipesline;
                String[] pipesarr = null;

                while ((extentline = extentsreader.readLine()) != null) {
                    extentarr = extentline.split("\\|");
                    if (pipesarr == null && (pipesline = pipesreader.readLine()) != null) {
                        pipesarr = pipesline.split("\\|");
                    }

                    if (pipesarr != null) {
                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            outfile.write(extentline + "|" + attrib + "=" + pipesarr[pipesarr.length - 1] + "\n");
                            pipesarr = null;
                        } else {
                            outfile.write(extentline + "|-\n");
                        }
                    } else {
                        outfile.write(extentline + "|-\n");
                    }

                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String merge_classik_append(String appendfile, PipesFile pipesfile, String attrib) {
        String outputfile = null;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + ".TempEval-features-annotatedWith-attribs-append";
            //String extentsfile = pipesfile.getFile().getCanonicalPath().substring(0, pipesfile.getFile().getCanonicalPath().indexOf(".")) + "." + elemext + ".TempEvalFiles-extents";
            String extentsfile = appendfile;

            PipesFile keypipes = new PipesFile(extentsfile);
            keypipes.isWellFormedOptimist();

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(extentsfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String extentline;
                String[] extentarr = null;
                String pipesline;
                String[] pipesarr = null;

                while ((extentline = extentsreader.readLine()) != null) {
                    extentarr = extentline.split("\\|");
                    if (pipesarr == null && (pipesline = pipesreader.readLine()) != null) {
                        pipesarr = pipesline.split("\\|");
                    }

                    if (pipesarr != null) {
                        if (pipesarr[0].equals(extentarr[0]) && pipesarr[1].equals(extentarr[1]) && pipesarr[2].equals(extentarr[2])) {
                            outfile.write(extentline + ";" + attrib + "=" + pipesarr[pipesarr.length - 1] + "\n");
                            pipesarr = null;
                        } else {
                            outfile.write(extentline + "\n");
                        }
                    } else {
                        outfile.write(extentline + "\n");
                    }

                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String generate_tab_extents(PipesFile pipesfile) {
        String outputfile = null;
        int linen = 0;
        try {
            int iob2col = pipesfile.getColumn("element\\(IOB2\\)");
            if (iob2col == -1) {
                iob2col = pipesfile.getLastDescColumn();
            }
            outputfile = pipesfile.getFile().getCanonicalPath() + "-extents.tab";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String pipesline;
                String[] pipesarr = null;
                int elemid = 0; // save id per file
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    if (!curr_fileid.equals(pipesarr[0])) {
                        elemid = 0;
                        curr_fileid = pipesarr[0];
                    }
                    if (!pipesarr[iob2col].equals("O")) {
                        String iob2 = pipesarr[iob2col].substring(0, 2);
                        String element = pipesarr[iob2col].substring(2);
                        if (iob2.equals("B-")) {
                            elemid++;
                        }
                        //outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2].substring(0, pipesarr[2].indexOf('-')) + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\n");
                        outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\n");
                    }
                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " (" + linen + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String generate_tab_attribs(PipesFile pipesfile) {
        String outputfile = null;
        int linen = 0;

        try {
            int iob2col = pipesfile.getColumn("element\\(IOB2\\)");
            if (iob2col == -1) {
                iob2col = pipesfile.getLastDescColumn() - 1;
            }
            int attrscol = iob2col + 1;
            outputfile = pipesfile.getFile().getCanonicalPath() + "-attributes.tab";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String pipesline;
                String[] pipesarr = null;
                int elemid = 0; // save id per file
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    if (!curr_fileid.equals(pipesarr[0])) {
                        elemid = 0;
                        curr_fileid = pipesarr[0];
                    }
                    //System.err.println(pipesline + " ----> "+pipesarr[iob2col]+" attrcol ("+attrscol+") l="+pipesarr.length);
                    if (!pipesarr[attrscol].equals("-") && !pipesarr[attrscol].equals("*")) {
                        //System.err.println(pipesline + " ----> "+pipesarr[iob2col]);
                        if (pipesarr[attrscol].matches(".*[^\"=]\" .*")) { // spaces
                            String attrs = pipesarr[attrscol].replaceAll("\\s+", " ").trim();
                            if (attrs.endsWith("\"")) {
                                attrs = attrs.substring(0, attrs.length() - 1);
                            }
                            String[] attrsarr = attrs.split("\" ");
                            String element = pipesarr[iob2col].substring(2);
                            elemid++;
                            for (int i = 0; i < attrsarr.length; i++) {
                                if (attrsarr[i].matches("[^=]+=\"[^=]+")) {
                                    String attrname = attrsarr[i].substring(0, attrsarr[i].indexOf("=\""));
                                    String attrvalue = attrsarr[i].substring(attrsarr[i].indexOf("=\"") + 2);
                                    if (attrvalue.matches("\".*\"")) {
                                        attrvalue = attrvalue.substring(1, attrvalue.length() - 1);
                                    }
                                    if (!attrname.matches("(t|e)id")) {
                                        //outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2].substring(0, pipesarr[2].indexOf('-')) + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\t" + attrname + "\t" + attrvalue + "\n");
                                        outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\t" + attrname + "\t" + attrvalue + "\n");
                                    }
                                }
                            }
                        } else { // semicolon
                            String[] attrsarr = pipesarr[attrscol].trim().split(";");
                            String element = pipesarr[iob2col].substring(2);
                            elemid++;
                            for (int i = 0; i < attrsarr.length; i++) {
                                String attrname = attrsarr[i].substring(0, attrsarr[i].indexOf('='));
                                String attrvalue = attrsarr[i].substring(attrsarr[i].indexOf("=") + 1);
                                if (attrvalue.matches("\".*\"")) {
                                    attrvalue = attrvalue.substring(1, attrvalue.length() - 1);
                                }
                                if (!attrname.matches("(t|e)id")) {
                                    //outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2].substring(0, pipesarr[2].indexOf('-')) + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\t" + attrname + "\t" + attrvalue + "\n");
                                    outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\t" + attrname + "\t" + attrvalue + "\n");
                                }
                            }
                        }
                    }
                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " (Reading line " + linen + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static Boolean generate_tab_extents_and_attribs_with_real_id(PipesFile pipesfile, String filenamebase, String id_re) {
        int linen = 0;
        try {
            int iob2col = pipesfile.getColumn("element\\(IOB2\\)");
            if (iob2col == -1) {
                iob2col = pipesfile.getLastDescColumn();
            }
            int attrscol = iob2col + 1;

            BufferedWriter extentsfile = new BufferedWriter(new FileWriter(pipesfile.getFile().getParent() + filenamebase + "-extents.tab"));
            BufferedWriter attribsfile = new BufferedWriter(new FileWriter(pipesfile.getFile().getParent() + filenamebase + "-attributes.tab"));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String pipesline;
                String[] pipesarr = null;
                String elemid = "unkonwn"; // save id per file
                String curr_fileid = "";

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    if (!curr_fileid.equals(pipesarr[0])) {
                        elemid = "unknown";
                        curr_fileid = pipesarr[0];
                    }




                    //System.err.println(pipesline + " ----> "+pipesarr[iob2col]+" attrcol ("+attrscol+") l="+pipesarr.length);

                    // Include extents for B- and I- elements
                    if (!pipesarr[iob2col].equals("O")) {
                        String element = pipesarr[iob2col].substring(2);

                        // Include only attribs for B-
                        if (pipesarr[iob2col].substring(0, 2).equalsIgnoreCase("B-")) {
                            // check there are attribs (at least id)
                            if (pipesarr[attrscol].equals("-") || pipesarr[attrscol].equals("*")) {
                                throw new Exception("Found B-element without attribs");
                            }

                            HashMap<String, String> attribs = XmlAttribs.parseAttrs(pipesarr[attrscol]);

                            Boolean id_found = false;
                            for (String current_attrib : attribs.keySet()) {
                                if (current_attrib.matches(id_re)) {
                                    elemid = attribs.get(current_attrib);
                                    id_found = true;
                                    break;
                                }
                            }
                            if (!id_found) {
                                throw new Exception("All the elements must have an ID (" + id_re + "). Line: " + pipesline);
                            }
                            for (String current_attrib : attribs.keySet()) {
                                if (!current_attrib.matches(id_re)) {
                                    attribsfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + element.toLowerCase() + "\t" + elemid + "\t1\t" + current_attrib + "\t" + attribs.get(current_attrib) + "\n");
                                }
                            }

                        }
                        //outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2].substring(0, pipesarr[2].indexOf('-')) + "\t" + element.toLowerCase() + "\t" + element.substring(0, 1).toLowerCase() + elemid + "\t1\n");
                        extentsfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + element.toLowerCase() + "\t" + elemid + "\t1\n");
                    }




                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (extentsfile != null) {
                    extentsfile.close();
                }
                if (attribsfile != null) {
                    attribsfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " (Reading line " + linen + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return false;
        }
        return true;
    }

    // generate tab links: cut -f 1-3,last -d "|" --> save in tab format
    public static String generate_tab_links(PipesFile pipesfile) {
        String outputfile = null;


        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-links.tab";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));
            try {
                String pipesline;
                String[] pipesarr = null;
                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");
                    outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + pipesarr[pipesarr.length - 1] + "\n");
                }
            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String generate_base_segmentation(String file) {
        String outputfile = null;
        int linen = 0;
        try {
            outputfile = file.substring(0, file.indexOf(".plain")) + ".plain.tab";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(file));

            try {
                String pipesline;
                String[] pipesarr = null;
                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    outfile.write(pipesarr[0] + "\t" + pipesarr[1] + "\t" + pipesarr[2] + "\t" + pipesarr[3] + "\n");
                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " (" + linen + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * Saves the dct in a standard dct.tab file
     *
     * @param tmlfile
     * @return the path to the file
     */
    public static String tml2dct_tab(String tmlfile) {
        String outputfile = null;
        try {
            outputfile = tmlfile.substring(0, tmlfile.lastIndexOf('/') + 1) + "dct.tab";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile, true));
            BufferedReader tmlreader = new BufferedReader(new FileReader(tmlfile.substring(0, tmlfile.indexOf(".tml", tmlfile.lastIndexOf('/'))) + ".tml"));

            try {
                String[] dct = new String[3]; // tid, value
                String line;
                while ((line = tmlreader.readLine()) != null) {
                    if (line.matches(".*tid=.*")
                         && line.matches(".*value.*")
                         && line.matches(".*functionInDocument=\"(CREATION|PUBLICATION)_TIME\".*")
                         ) {
                        dct[0] = tmlfile.substring(tmlfile.lastIndexOf('/') + 1, tmlfile.indexOf(".tml", tmlfile.lastIndexOf('/')));
                        dct[1] = line.substring(line.indexOf("value=\"") + 7, line.indexOf("\"", line.indexOf("value=\"") + 7));
                        dct[2] = line.substring(line.indexOf("tid=\"") + 5, line.indexOf("\"", line.indexOf("tid=\"") + 5));
                        break;
                    }
                }

                if (dct[0] == null) {
                    throw new Exception("Reference date (dct) not found as CREATION_TIME/PUBLICATION_TIME: "+tmlfile);
                }

                outfile.write(dct[0] + ".tml.plain\t" + dct[1] + "\t" + dct[2] + "\n");
            } finally {
                if (tmlreader != null) {
                    tmlreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (TempEvalFiles):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                //System.exit(1);
            }
            return outputfile;
        }
        return outputfile;
    }


    /**
     * Returns and array with [value,tid] pairs from a dct.tab
     * 
     * @param dctsTabFile
     * @return
     */
    public static HashMap<String, String[]> getDCTsFromTab(String dctsTabFile) {
        HashMap<String, String[]> DCTs = null;
        try {
            if (!(new File(dctsTabFile)).exists()) {
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(dctsTabFile + " does not exist.");
                }
                return null;
            }
            BufferedReader dctreader = new BufferedReader(new FileReader(dctsTabFile));
            try {
                String line;
                DCTs = new HashMap<String, String[]>();
                while ((line = dctreader.readLine()) != null) {
                    String[] linearr = line.split("\t");
                    if (linearr[1].matches("[0-9]{8}")) {
                        linearr[1] = linearr[1].substring(0, 4) + "-" + linearr[1].substring(4, 6) + "-" + linearr[1].substring(6, 8);
                    }
                    if (linearr.length == 2) {
                        DCTs.put(linearr[0], new String[]{linearr[1], "t0"});
                    }
                    if (linearr.length == 3) {
                        DCTs.put(linearr[0], new String[]{linearr[1], linearr[2]});
                    }
                }
            } finally {
                if (dctreader != null) {
                    dctreader.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEvalFiles):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return DCTs;
    }

}
