package ml.anon.recognition.machinelearning.service;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.anonymization.model.Label;
import ml.anon.anonymization.model.Producer;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreService implements IScoreService{

    @Resource
    private DocumentResource documentResource;

    @Override
    public boolean calculateFOneScore(String documentId, List<Anonymization> correctAnonymizations) {

        Document document = documentResource.findById(documentId);

        List<Anonymization> filteredGenerated = this.filterByProducer(document.getAnonymizations());
        List<Anonymization> filteredCorrected = this.filterByProducer(correctAnonymizations);

        double numberOfGenerated = filteredGenerated.size();
        double numberOfCorrected = filteredCorrected.size();
        double numberOfCorrectFound = this.countCorrectFound(filteredGenerated, filteredCorrected);

        double precision = numberOfCorrectFound/numberOfGenerated;
        double recall = numberOfCorrectFound/numberOfCorrected;
        double fOneScore = (2*precision*recall) / (precision + recall);

        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1: " + fOneScore);

        return true;
    }

    private double countCorrectFound(List<Anonymization> filteredGenerated, List<Anonymization> filteredCorrected) {
        double numberOfCorrectFound = 0;
        for (Anonymization generated: filteredGenerated) {

            for (Anonymization corrected: filteredCorrected) {
                if(generated.getData().getOriginal().equals(corrected.getData().getOriginal())
                        && corrected.getProducer().equals(Producer.ML)){
                    numberOfCorrectFound++;
                    break;
                }
            }
        }

        return numberOfCorrectFound;
    }

    private List<Anonymization> filterByProducer(List<Anonymization> anonymizations) {
        List<Anonymization> filtered = new ArrayList<Anonymization>();
        for (Anonymization anonymization: anonymizations) {

            if(anonymization.getProducer().equals(Producer.ML) || anonymization.getProducer().equals(Producer.HUMAN)
                    && (anonymization.getData().getLabel().equals(Label.MISC)
                    || anonymization.getData().getLabel().equals(Label.LOCATION)
                    || anonymization.getData().getLabel().equals(Label.ORGANIZATION)
                    || anonymization.getData().getLabel().equals(Label.PERSON))){
               filtered.add(anonymization);
            }
        }
        return filtered;
    }
}
