package ml.anon.recognition.machinelearning.service;

import edu.stanford.nlp.parser.metrics.Eval;
import ml.anon.anonymization.model.Anonymization;
import ml.anon.anonymization.model.Label;
import ml.anon.anonymization.model.Producer;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import ml.anon.recognition.machinelearning.model.EvaluationData;
import ml.anon.recognition.machinelearning.repository.EvaluationDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreService implements IScoreService{

    @Resource
    private DocumentResource documentResource;

    @Autowired
    private EvaluationDataRepository evaluationDataRepository;

    private double threshhold = 10;

    @Override
    public boolean calculateScores(String documentId, List<Anonymization> correctAnonymizations) {

        Document document = documentResource.findById(documentId);

        List<Anonymization> filteredGenerated = this.filterByProducer(document.getAnonymizations());
        List<Anonymization> filteredCorrected = this.filterByProducer(correctAnonymizations);

        double numberOfGenerated = filteredGenerated.size();
        double numberOfCorrected = filteredCorrected.size();
        double numberOfCorrectFound = this.countCorrectFound(filteredGenerated, filteredCorrected);

        double precision = numberOfCorrectFound/numberOfGenerated;
        double recall = numberOfCorrectFound/numberOfCorrected;
        double fOneScore = this.calculatetFOne(precision, recall);

        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1: " + fOneScore);


        EvaluationData evaluationData = this.getEvaluationData();

        evaluationData.setTotalCorrected(evaluationData.getTotalCorrected() + numberOfCorrected);
        evaluationData.setTotalGenerated(evaluationData.getTotalGenerated() + numberOfGenerated);
        evaluationData.setTotalNumberOfCorrectFound(evaluationData.getTotalNumberOfCorrectFound()
                + numberOfCorrectFound);

        evaluationData.setLastTenFOne(this.updateScore(fOneScore, evaluationData.getLastTenFOne()));
        evaluationData.setLastTenPrecision(this.updateScore(precision, evaluationData.getLastTenPrecision()));
        evaluationData.setLastTenRecall(this.updateScore(recall, evaluationData.getLastTenRecall()));

        evaluationData.setOverallPrecision(evaluationData.getTotalNumberOfCorrectFound()/evaluationData.getTotalGenerated());
        evaluationData.setOverallRecall(evaluationData.getTotalNumberOfCorrectFound()/evaluationData.getTotalCorrected());
        evaluationData.setOverallFOne(this.calculatetFOne(evaluationData.getOverallPrecision(), evaluationData.getOverallRecall()));

        evaluationDataRepository.save(evaluationData);

        return true;
    }

    private double calculatetFOne(double precision, double recall) {
        return (2*precision*recall) / (precision + recall);
    }

    private List<Double> updateScore(double score, List<Double> scores) {

        scores.add(score);
        if(scores.size() > threshhold){
            scores.remove(0);
        }
        return scores;
    }

    @Override
    public EvaluationData getEvaluationData() {
        List<EvaluationData> evaluationDatas = evaluationDataRepository.findAll();
        if(evaluationDatas.size() == 0) {
            EvaluationData evaluationData = new EvaluationData();
            evaluationData.setLastTenRecall(new ArrayList<Double>());
            evaluationData.setLastTenPrecision(new ArrayList<Double>());
            evaluationData.setLastTenFOne(new ArrayList<Double>());
            return evaluationData;
        }else{
            return evaluationDatas.get(0);
        }
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
