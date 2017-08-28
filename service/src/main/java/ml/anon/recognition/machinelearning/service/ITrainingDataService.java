package ml.anon.recognition.machinelearning.service;

import ml.anon.recognition.machinelearning.model.TrainingData;

public interface ITrainingDataService {
  public boolean updateTrainingData(String id);
  public TrainingData getTrainingData();
  public boolean appendToTrainingTxt(String importedTrainingData);
}
