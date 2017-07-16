package ml.anon.recognition.machinelearning.service;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import ml.anon.io.ResourceUtil;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.crfsuite.CrfSuiteStringOutcomeDataWriter;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.cr.FilesCollectionReader;
import org.springframework.stereotype.Service;

import de.tu.darmstadt.lt.ner.annotator.NERAnnotator;
import de.tu.darmstadt.lt.ner.preprocessing.ChangeColon;
import de.tu.darmstadt.lt.ner.preprocessing.Configuration;
import de.tu.darmstadt.lt.ner.preprocessing.GermaNERMain;
import de.tu.darmstadt.lt.ner.reader.NERReader;
import de.tu.darmstadt.lt.ner.writer.EvaluatedNERWriter;
import ml.anon.documentmanagement.resource.ReplacementResource;
import ml.anon.anonymization.model.Anonymization;
import ml.anon.anonymization.model.Anonymization.AnonymizationBuilder;
import ml.anon.anonymization.model.Label;
import ml.anon.anonymization.model.Producer;
import ml.anon.documentmanagement.model.Document;

@Service
@Slf4j
public class AnnotationService implements IAnnotationService {
    private final static String basePath = AnnotationService.class.getResource(File.separator + "GermaNER").getPath() + File.separator;


    private final static String pathToTokenizedFile = ResourceUtil.getPath("GermaNER" + File.separator + "temp-file-to-annotate.txt");
    private final static String pathToConfig = ResourceUtil.getPath("GermaNER" + File.separator + "config.properties");
    private final static String pathToOuputFile = ResourceUtil.getPath("GermaNER" + File.separator + "taggedFile.txt");
    private final static String pathToModel = basePath + "model";
    public final static String pathToTrainingFile = ResourceUtil.getPath("GermaNER" + File.separator + "trainingsFile.txt");
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
            IOUtils.copyLarge(ResourceUtil.getStream("model/model.jar"),
                    new FileOutputStream(new File(modelDirectory, "model.jar")));
        }
        if (!new File(modelDirectory, "MANIFEST.MF").exists()) {
            IOUtils.copyLarge(ResourceUtil.getStream("model/MANIFEST.MF"),
                    new FileOutputStream(new File(modelDirectory, "MANIFEST.MF")));
        }
        if (!new File(modelDirectory, "feature.xml").exists()) {
            IOUtils.copyLarge(ResourceUtil.getStream("feature/feature.xml"),
                    new FileOutputStream(new File(modelDirectory, "feature.xml")));
        }
    }

    public static void writeModel(File NER_TagFile, File modelDirectory, String dataZipFile)
            throws UIMAException, IOException {
        runPipeline(
                FilesCollectionReader.getCollectionReaderWithSuffixes(NER_TagFile.getAbsolutePath(),
                        NERReader.CONLL_VIEW, NER_TagFile.getName()),
                createEngine(NERReader.class, NERReader.DATA_ZIP_FILE, dataZipFile),
                createEngine(NERAnnotator.class, NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE,
                        modelDirectory.getAbsolutePath() + "/feature.xml",
                        CleartkSequenceAnnotator.PARAM_IS_TRAINING, true,
                        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, modelDirectory.getAbsolutePath(),
                        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
                        CrfSuiteStringOutcomeDataWriter.class));

    }

    public static void trainModel(File modelDirectory) throws Exception {
        org.cleartk.ml.jar.Train.main(modelDirectory.getAbsolutePath());
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

            File outputtmpFile = File.createTempFile("result", ".tmp");
            File outputFile = new File(pathToOuputFile);

            long initNerModelB = System.currentTimeMillis();

            // one classifyTestFile run to preload the data.zip
            c.normalize(Configuration.testFileName, Configuration.testFileName + ".normalized");
            classifyTestFile(modelDirectory, new File(Configuration.testFileName + ".normalized"),
                    outputtmpFile, null, null);
            c.deNormalize(outputtmpFile.getAbsolutePath(), outputFile.getAbsolutePath());

            long initNerModelA = System.currentTimeMillis();
            System.out
                    .println("Time to preload features: " + (initNerModelA - initNerModelB) / 1000 + "s");


        } catch (Exception e) {
            System.out.println("error in initGermaNER second try-catch");
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Done in " + totalTime / 1000 + " seconds");

    }

    public List<Anonymization> annotate(Document document) {

        // initGermaNER();


        String tokenizedFile = "";
        for (String token : document.getChunks()) {
            if (tokenizedFile.length() != 0) {
                tokenizedFile += "\n";
            }
            tokenizedFile += token;
        }


        File outputFile = new File(pathToOuputFile);

        System.out.println("Start tagging");

        PrintWriter out;
        ArrayList<Anonymization> anonymizations = null;
        try (InputStream inputStream = new FileInputStream(outputFile.getAbsolutePath())) {
            File outputtmpFile = File.createTempFile("result", ".tmp");
            out = new PrintWriter(pathToTokenizedFile);
            out.println(tokenizedFile);
            out.close();

            c.normalize(Configuration.testFileName, Configuration.testFileName + ".normalized");

            classifyTestFile(modelDirectory, new File(Configuration.testFileName + ".normalized"),
                    outputtmpFile, null, null);
            // re-normalized the colon changed text
            c.deNormalize(outputtmpFile.getAbsolutePath(), outputFile.getAbsolutePath());


            anonymizations = receiveAnonymizations(inputStream);

        } catch (UIMAException e) {
            // TODO Auto-generated catch block
            System.out.println("error in annotateTokenizedFile first catch");
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("error in annotateTokenizedFile second catch");
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }

        return anonymizations;
    }

    private ArrayList<Anonymization> receiveAnonymizations(InputStream inputStream)
            throws IOException {
        ArrayList<Anonymization> anonymizations;
        ReplacementResource replacementGenerator = new ReplacementResource();

        Reader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader in = new BufferedReader(inputStreamReader);

        anonymizations = new ArrayList<Anonymization>();

        // id, original, replacement, label, producer

        AnonymizationBuilder anonymization = Anonymization.builder().producer(Producer.ML);

        String line;
        String original = "";
        Label label = Label.MISC;
        int counter = 0;
        int temp = 1;
        while ((line = in.readLine()) != null) {
            String[] splitted = line.split("  ");

            if (splitted.length != 2)
                continue;
            if (splitted[1].equals("O"))
                continue;
            if (splitted[1].startsWith("B-")) {
                counter++;
                if (temp == (counter - 1)) {
                    original = original.trim();
                    temp = counter;
                    anonymization.original(original);

                    anonymization.replacement(replacementGenerator.generateReplacement(original, label));

                    anonymization.label(label);

                    anonymizations.add(anonymization.build());
                    original = "";

                }
                String substring = splitted[1].substring(2);
                label = Label.getOrDefault(substring, Label.UNKNOWN); // Label - must exactly match!

                original += " " + splitted[0]; // 1. Teil des Tags
                log.debug(line);
            } else if (splitted[1].startsWith("I-")) {
                original += " " + splitted[0]; // 2. - n. Teil des tags
            } else {
                System.out.println("Unknown type: " + splitted[1]);
            }
        }

        original = original.trim();
        anonymization.original(original);
        anonymization.replacement(replacementGenerator.generateReplacement(original, label));
        anonymization.label(label);

        anonymizations.add(anonymization.build());

        inputStreamReader.close();
        return anonymizations;
    }


    public boolean retrain() {

        System.out.println("Now retrain!");

        Configuration.trainFileName = new File(pathToTrainingFile).getAbsolutePath();
        initNERModel();

        try {
            setModelDir();

        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }

        try {

            c.normalize(Configuration.trainFileName, Configuration.trainFileName + ".normalized");

            System.out.println("Start model generation");
            String dataZipeFile = null;
            writeModel(new File(Configuration.trainFileName + ".normalized"), modelDirectory,
                    dataZipeFile);
            System.out.println("Start model generation -- done");

            System.out.println("Start training");
            trainModel(modelDirectory);
            System.out.println("Start training ---done");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

}
