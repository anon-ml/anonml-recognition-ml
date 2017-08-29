package ml.anon.recognition.machinelearning.service;

import ml.anon.anonymization.model.Anonymization;

import java.util.List;

public interface IScoreService {

    public boolean calculateFOneScore(String documentId, List<Anonymization> correctAnonymizations);
}
