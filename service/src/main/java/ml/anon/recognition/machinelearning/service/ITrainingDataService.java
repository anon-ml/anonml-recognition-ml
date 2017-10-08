package ml.anon.recognition.machinelearning.service;

import ml.anon.recognition.machinelearning.model.TrainingData;

public interface ITrainingDataService {


  /**
   * Loads the actual training data from the database, appends the given training data and saves it.
   *
   * @param importedTrainingData a String of the training data format which can be appended to the saved training data
   * @param resetOld a boolean if the existing training data should be kept or overwritten
   * @return true if everything worked
   */
  public TrainingData appendToTrainingTxt(String importedTrainingData, boolean resetOld);

  /**
   * Loads the saved training data and appends the generated training data from the saved
   * {@link ml.anon.documentmanagement.model.Document}s
   * @return the found or initialized {@link TrainingData} object
   */
  public TrainingData getBuildTrainingData();
}
