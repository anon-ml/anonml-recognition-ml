package ml.anon.recognition.machinelearning;

import ml.anon.AppMachineLearning;
import ml.anon.recognition.machinelearning.model.TrainingData;
import ml.anon.recognition.machinelearning.repository.TrainingDataRepository;
import ml.anon.recognition.machinelearning.service.ITrainingDataService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        AppMachineLearning.class,
        TestMongoConfig.class
})
public class TrainingTest {

    @Autowired
    private ITrainingDataService trainingDataService;

    @Autowired
    private TrainingDataRepository trainingDataRepository;


    private String testString = "Test O";

    private String trainingTxt = "Schartau\tB-PERSON\n" +
            "sagte\tO\n" +
            "dem\tO\n" +
            "\"\tO\n" +
            "Tagesspiegel\tB-ORGANIZATION\n" +
            "\"\tO\n" +
            "vom\tO\n" +
            "Freitag\tO\n" +
            ",\tO\n" +
            "Fischer\tB-PERSON\n" +
            "sei\tO\n" +
            "\"\tO\n" +
            "in\tO\n" +
            "einer\tO\n" +
            "Weise\tO\n" +
            "aufgetreten\tO\n" +
            ",\tO\n" +
            "die\tO\n" +
            "alles\tO\n" +
            "andere\tO\n" +
            "als\tO\n" +
            "überzeugend\tO\n" +
            "war\tO\n" +
            "\"\tO\n" +
            ".\tO";

    @Before
    public void setUp(){
        trainingDataService.appendToTrainingTxt(testString, false);
    }

    @After
    public void pullDown(){
        trainingDataRepository.deleteAll();
    }


    @Test
    public void appendTrainingData(){

        List<TrainingData> trainingData = trainingDataRepository.findAll();

        assertThat(trainingDataService.appendToTrainingTxt(trainingTxt, false), is(true));
        List<TrainingData> trainingDataAfter = trainingDataRepository.findAll();

        assertThat(trainingData.size(), is(trainingDataAfter.size()));
        assertThat(trainingData.get(0).getTrainingTxt(), not(containsString(trainingTxt)));
        assertThat(trainingDataAfter.get(0).getTrainingTxt(), is(containsString(trainingTxt)));
        assertThat(trainingData.get(0).getTrainingTxt(), is(containsString(testString)));
        assertThat(trainingDataAfter.get(0).getTrainingTxt(), is(containsString(testString)));
        assertThat(trainingDataAfter.get(0).getTrainingTxt(), is(testString
                + System.lineSeparator() + System.lineSeparator() + trainingTxt + System.lineSeparator()));
    }

    @Test
    public void resetTrainingData(){

        List<TrainingData> trainingData = trainingDataRepository.findAll();
        assertThat(trainingData.get(0).getTrainingTxt(), is(testString + System.lineSeparator()));

        assertThat(trainingDataService.appendToTrainingTxt(trainingTxt, true), is(true));
        List<TrainingData> trainingDataAfter = trainingDataRepository.findAll();

        assertThat(trainingDataAfter.size(), is(trainingData.size()));
        assertThat(trainingDataAfter.get(0).getTrainingTxt(), is(trainingTxt + System.lineSeparator()));
    }


}
