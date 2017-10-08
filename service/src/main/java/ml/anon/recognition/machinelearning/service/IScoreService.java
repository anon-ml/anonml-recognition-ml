package ml.anon.recognition.machinelearning.service;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.recognition.machinelearning.model.EvaluationData;

import java.util.List;

/**
 * Calculates evaluation scores as F_one, Precision and Recall out of the generated {@link Anonymization}s and the
 * by human interaction corrected {@link Anonymization}s as gold set.
 */
public interface IScoreService {

    /**
     * Calculates evaluation scores of the actual anonymized document and in addition builds overall evaluation
     * scores to see the quality of the generated {@link Anonymization}s
     *
     * @param documentId the id of the actual worked document which should be saved now
     * @return true if everything went fine
     */
    public boolean prepareNumbers(String documentId);

    /**
     * Loads the saved {@link EvaluationData} from the database if there is already one saved or generates one initial
     * one.
     *
     * @return the found or generated {@link EvaluationData}
     */
    public EvaluationData getOverallEvaluationData();
}
