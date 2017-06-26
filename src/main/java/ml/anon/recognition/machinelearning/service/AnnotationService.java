package ml.anon.recognition.machinelearning.service;

import de.tu.darmstadt.lt.ner.annotator.NERAnnotator;
import de.tu.darmstadt.lt.ner.preprocessing.ChangeColon;
import de.tu.darmstadt.lt.ner.preprocessing.Configuration;
import de.tu.darmstadt.lt.ner.preprocessing.GermaNERMain;
import de.tu.darmstadt.lt.ner.reader.NERReader;
import de.tu.darmstadt.lt.ner.writer.EvaluatedNERWriter;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.cr.FilesCollectionReader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

@Service
public class AnnotationService implements IAnnotationService {

    private final static String basePath = "./src/main/resources/GermaNER/";
    private final static String pathToTokenizedFile = basePath + "temp-file-to-annotate.txt";
    private final static String pathToConfig = basePath + "config.properties";
    private final static String pathToOuputFile = basePath + "taggedFile.txt";
    private final static String pathToModel = basePath + "model";
    static Properties prop;
    static InputStream configFile = null;
    static File modelDirectory;
    static ChangeColon c;

    private static void initNERModel() {
        System.out.println("initNerModel Accessed");

        File dataZip = new File(pathToConfig);
        try {
            InputStream is = new FileInputStream(dataZip);
            configFile = is;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GermaNERMain.configFile = configFile;
        prop = new Properties();
        GermaNERMain.prop = prop;
        loadConfig();
    }

    private static void loadConfig() {

        System.out.println("LoadConfig Accessed");

        try {
            prop.load(configFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (Configuration.testFileName != null) {
            if (Configuration.trainFileName != null) {
                Configuration.mode = "ft";
            } else {
                Configuration.mode = "t";
            }
        }
        if (Configuration.trainFileName != null) {
            if (Configuration.testFileName != null) {
                Configuration.mode = "ft";
            } else {
                Configuration.mode = "f";
            }
        }
        Configuration.useClarkPosInduction =
                prop.getProperty("useClarkPosInduction").equals("1") ? true : false;
        Configuration.usePosition = prop.getProperty("usePosition").equals("1") ? true : false;
        Configuration.useFreeBase = prop.getProperty("useFreeBase").equals("1") ? true : false;
        Configuration.modelDir = prop.getProperty("modelDir");
    }

    private static void setModelDir() throws IOException, FileNotFoundException {

        System.out.println("setModelDir Accessed");

        // TODO: changed new File("output")
        modelDirectory = (Configuration.modelDir == null || Configuration.modelDir.isEmpty())
                ? new File(pathToModel) : new File(Configuration.modelDir);
        modelDirectory.mkdirs();

        System.out.println("ModelDir: " + modelDirectory.getAbsolutePath());
        System.out.println("ModelDir: " + Configuration.modelDir);

        if (!new File(modelDirectory, "model.jar").exists()) {
            IOUtils.copyLarge(ClassLoader.getSystemResourceAsStream("model/model.jar"),
                    new FileOutputStream(new File(modelDirectory, "model.jar")));
        }
        if (!new File(modelDirectory, "MANIFEST.MF").exists()) {
            IOUtils.copyLarge(ClassLoader.getSystemResourceAsStream("model/MANIFEST.MF"),
                    new FileOutputStream(new File(modelDirectory, "MANIFEST.MF")));
        }
        if (!new File(modelDirectory, "feature.xml").exists()) {
            IOUtils.copyLarge(ClassLoader.getSystemResourceAsStream("feature/feature.xml"),
                    new FileOutputStream(new File(modelDirectory, "feature.xml")));
        }
    }

    private static void classifyTestFile(File aClassifierJarPath, File testPosFile, File outputFile,
                                         File aNodeResultFile, List<Integer> aSentencesIds) throws UIMAException, IOException {

        System.out.println("classifyTestFile Accessed");

        runPipeline(FilesCollectionReader.getCollectionReaderWithSuffixes(testPosFile.getAbsolutePath(),
                NERReader.CONLL_VIEW, testPosFile.getName()),

                createEngine(NERReader.class),
                createEngine(NERAnnotator.class, NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE,
                        aClassifierJarPath.getAbsolutePath() + "/feature.xml", NERAnnotator.FEATURE_FILE,
                        aClassifierJarPath.getAbsolutePath(),
                        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                        aClassifierJarPath.getAbsolutePath() + "/model.jar"),
                createEngine(EvaluatedNERWriter.class, EvaluatedNERWriter.OUTPUT_FILE, outputFile,
                        EvaluatedNERWriter.IS_GOLD, false, EvaluatedNERWriter.NOD_OUTPUT_FILE, aNodeResultFile,
                        EvaluatedNERWriter.SENTENCES_ID, aSentencesIds));
    }

    public static void initGermaNER() {

        System.out.println("initGermaNER Accessed");
        long startTime = System.currentTimeMillis();

        c = new ChangeColon();

        PrintWriter out;

        try {

            out = new PrintWriter(pathToTokenizedFile);
            out.println("init");
            out.close();

            Configuration.testFileName = new File(pathToTokenizedFile).getAbsolutePath();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (prop == null) {
            // load a properties file
            long initNerModelB = System.currentTimeMillis();
            initNERModel();
            long initNerModelA = System.currentTimeMillis();
            System.out.println("Time to init: " + (initNerModelA - initNerModelB) + "ms");

        }

        try {

            setModelDir();

            File outputtmpFile = new File(modelDirectory, "result.tmp");
            File outputFile = new File(pathToOuputFile);

            long initNerModelB = System.currentTimeMillis();

            // one classifyTestFile run to preload the data.zip
            c.normalize(Configuration.testFileName, Configuration.testFileName + ".normalized");
            classifyTestFile(modelDirectory, new File(Configuration.testFileName + ".normalized"),
                    outputtmpFile, null, null);
            c.deNormalize(outputtmpFile.getAbsolutePath(), outputFile.getAbsolutePath());

            long initNerModelA = System.currentTimeMillis();
            System.out.println("Time to preload features: " + (initNerModelA - initNerModelB) / 1000 + "s");


        } catch (Exception e) {
            System.out.println("error in initGermaNER second try-catch");
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Done in " + totalTime / 1000 + " seconds");

    }


    // TODO: to pre load all it is necessary to start "initGermaNER" by the main and maybe make one
    // run of classifyTestFile to load all features..
    public String annotateTokenizedFile(String tokenizedFile) {

    initGermaNER();

        String content = "";
        File outputtmpFile = new File(modelDirectory, "result.tmp");
        File outputFile = new File(pathToOuputFile);

        System.out.println("Start tagging");

        PrintWriter out;
        try (InputStream inputStream = new FileInputStream(outputFile.getAbsolutePath())) {

            out = new PrintWriter(pathToTokenizedFile);
            out.println(tokenizedFile);
            out.close();

            c.normalize(Configuration.testFileName, Configuration.testFileName + ".normalized");

            classifyTestFile(modelDirectory, new File(Configuration.testFileName + ".normalized"),
                    outputtmpFile, null, null);
            // re-normalized the colon changed text
            c.deNormalize(outputtmpFile.getAbsolutePath(), outputFile.getAbsolutePath());


            Reader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(inputStreamReader);

            String line;
            while ((line = in.readLine()) != null) {
                String[] splitted = line.split("  ");

                if (splitted.length != 2)
                    continue;
                if (splitted[1].equals("O"))
                    continue;
                if (splitted[1].startsWith("B-")) {
                    if (content.length() != 0)
                        content += "\n";
                    content += splitted[1].substring(2);
                    content += " ";
                    content += " " + splitted[0];
                } else if (splitted[1].startsWith("I-")) {
                    content += " " + splitted[0];
                } else {
                    System.out.println("Unknown type: " + splitted[1]);
                }
            }
            inputStreamReader.close();


        } catch (UIMAException e) {
            // TODO Auto-generated catch block
            System.out.println("error in annotateTokenizedFile first catch");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("error in annotateTokenizedFile second catch");
            e.printStackTrace();
        }
        System.out.println("Content: \n\n" + content);
        return content;
    }
}
