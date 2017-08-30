package ml.anon.recognition.machinelearning.service;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.recognition.machinelearning.model.EvaluationData;

import java.util.List;

public interface IScoreService {

    public boolean calculateScores(String documentId, List<Anonymization> correctAnonymizations);

    public EvaluationData getEvaluationData();
}
