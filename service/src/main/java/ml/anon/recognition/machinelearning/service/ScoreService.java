package ml.anon.recognition.machinelearning.service;


import ml.anon.anonymization.model.Anonymization;
import ml.anon.anonymization.model.Label;
import ml.anon.anonymization.model.Producer;
import ml.anon.anonymization.model.Status;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import ml.anon.recognition.machinelearning.model.EvaluationData;
import ml.anon.recognition.machinelearning.repository.EvaluationDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 *
 */
@Service
public class ScoreService implements IScoreService {

    @Resource
    private DocumentResource documentResource;

    @Autowired
    private EvaluationDataRepository evaluationDataRepository;

    private double threshold = 10;

    @Override
    public boolean calculateScores(String documentId, List<Anonymization> correctAnonymizations) {

        Document document = documentResource.findById(documentId);

        List<Anonymization> filteredGenerated = this.filterByProducer(document.getAnonymizations());
        List<Anonymization> filteredCorrected = this.filterByProducer(correctAnonymizations);

        double numberOfGenerated = filteredGenerated.size();
        double numberOfCorrected = filteredCorrected.size();
        double numberOfCorrectFound = this.countCorrectFound(filteredGenerated, filteredCorrected);

        double precision = numberOfCorrectFound / numberOfGenerated;
        double recall = numberOfCorrectFound / numberOfCorrected;
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

        evaluationData.setOverallPrecision(evaluationData.getTotalNumberOfCorrectFound() / evaluationData.getTotalGenerated());
        evaluationData.setOverallRecall(evaluationData.getTotalNumberOfCorrectFound() / evaluationData.getTotalCorrected());
        evaluationData.setOverallFOne(this.calculatetFOne(evaluationData.getOverallPrecision(), evaluationData.getOverallRecall()));

        evaluationDataRepository.save(evaluationData);

        return true;
    }

    private double calculatetFOne(double precision, double recall) {
        return (2 * precision * recall) / (precision + recall);
    }

    /**
     * Sets the given evaluation score to the given list, if the given list exceeds a size of the threshold the oldest
     * element is removed to keep the maximum size of the threshold
     *
     * @param score  evaluation score to add
     * @param scores list of last evaluation scores
     * @return the modified list
     */
    private List<Double> updateScore(double score, List<Double> scores) {

        scores.add(score);
        if (scores.size() > threshold) {
            scores.remove(0);
        }
        return scores;
    }

    @Override
    public EvaluationData getEvaluationData() {
        List<EvaluationData> evaluationDatas = evaluationDataRepository.findAll();
        if (evaluationDatas.size() == 0) {
            EvaluationData evaluationData = new EvaluationData();
            evaluationData.setLastTenRecall(new ArrayList<Double>());
            evaluationData.setLastTenPrecision(new ArrayList<Double>());
            evaluationData.setLastTenFOne(new ArrayList<Double>());
            return evaluationData;
        } else {
            Collections.sort(evaluationDatas, Comparator.comparing(EvaluationData::getCreated));
            return evaluationDatas.get(0);
        }
    }

    /**
     * Counts the correct found {@link Anonymization} objects. Basically all {@link Anonymization} added by human were
     * not correctly found, but all {@link Anonymization} which are still contained in the corrected data are correct
     * (it is ignored if the {@link Label} was changed)
     *
     * @param filteredGenerated the from the ml module produced {@link Anonymization}s
     * @param filteredCorrected the {@link Anonymization}s produced by the ml module and from the human interacting
     * @return the number of found correct {@link Anonymization}s
     */
    private double countCorrectFound(List<Anonymization> filteredGenerated, List<Anonymization> filteredCorrected) {
        double numberOfCorrectFound = 0;
        for (Anonymization generated : filteredGenerated) {

            for (Anonymization corrected : filteredCorrected) {
                if (generated.getData().getOriginal().equals(corrected.getData().getOriginal())
                        && corrected.getProducer().equals(Producer.ML)) {
                    numberOfCorrectFound++;
                    break;
                }
            }
        }

        return numberOfCorrectFound;
    }

    /**
     * Filters the list by {@link Producer} and {@link Label} so that only labels are regarded if they are produced by
     * the ml module to keep the evaluation scores representative
     *
     * @param anonymizations list of {@link Anonymization} objects which should be filtered
     * @return the filtered list
     */
    private List<Anonymization> filterByProducer(List<Anonymization> anonymizations) {
        List<Anonymization> filtered = new ArrayList<Anonymization>();
        for (Anonymization anonymization : anonymizations) {

            if (!anonymization.getStatus().equals(Status.DECLINED)
                    && (anonymization.getProducer().equals(Producer.ML)
                    || anonymization.getProducer().equals(Producer.HUMAN))
                    && (anonymization.getData().getLabel().equals(Label.MISC)
                    || anonymization.getData().getLabel().equals(Label.LOCATION)
                    || anonymization.getData().getLabel().equals(Label.ORGANIZATION)
                    || anonymization.getData().getLabel().equals(Label.PERSON))) {
                filtered.add(anonymization);
            }
        }
        return filtered;
    }
}
