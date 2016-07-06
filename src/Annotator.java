import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Annotator {

    public void LoadDirectory(String rawfolder, String annotatingFolder, String suffix)
            throws IOException {
        int annotatedDocs = 0;
        System.out.println("Reading FOMC files from " + rawfolder + "...");

        File dir = new File(rawfolder);
        System.out.println(dir.getCanonicalPath());
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(suffix)) {
                AnnotateDocument(file, annotatingFolder);
                annotatedDocs++;
            } else if (file.isDirectory())
                LoadDirectory(file.getAbsolutePath(), annotatingFolder, suffix);
        }

        System.out.println("Annotated " + annotatedDocs + " review documents from " + rawfolder);
        System.out.println();
    }

    public void AnnotateDocument(File file, String folder) throws IOException {
        System.out.println("Annotating " + file.getName());
        System.out.println("=====================================================");

        Scanner scanner = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        File annotatedFile = new File(folder + file.getName());
        BufferedWriter writer = new BufferedWriter(new FileWriter(annotatedFile));
        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
                iterator.setText(line);
                int start = iterator.first();
                for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                    String sentence = line.substring(start,end);
                    System.out.println(sentence);
                    boolean valid = false;
                    double ranking = -10;
                    while (!valid) {
                        System.out.print("Ranking [-1, 1]: ");
                        String tmp = scanner.next();
                        try {
                            ranking = Double.parseDouble(tmp);
                            if (ranking >= -1 && ranking <= 1) valid = true;
                            else System.out.println("ERROR: Ranking must be within [-1, 1]!");
                        } catch (NumberFormatException e) {
                            System.out.println("ERROR: \'" + tmp + "\' is not a valid ranking!");
                        }
                    }
                    System.out.println();
                    writer.append(ranking + "\t" + sentence + "\n");
                }
            }
        }
        writer.flush();
        writer.close();
        reader.close();
    }

    public static void main(String[] args) throws IOException {
        Annotator annotator = new Annotator();
        annotator.LoadDirectory("data/FOMC_minutes/", "data/annotated_FOMC_minutes/", ".txt");

    }

}
