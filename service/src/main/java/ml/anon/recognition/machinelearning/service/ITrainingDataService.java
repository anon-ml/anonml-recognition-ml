package ml.anon.recognition.machinelearning.service;

import ml.anon.recognition.machinelearning.model.TrainingData;

public interface ITrainingDataService {

  /**
   * Sets up the training data from the manually controlled and edited document.
   * Only places the annotations (misc, person, location and organization) which are produces by the ml module to the
   * corresponding tokens. All other tokens receive the "O" (other) annotation.
   *
   * @param documentId id of the edited document
   * @return true if everything worked
   */
  public boolean updateTrainingData(String documentId);


  /**
   * Loads the saved training data to update it or to export it. If no training is saved yet, a new {@link TrainingData}
   * object is returned.
   *
   * @return the found or initialized {@link TrainingData} object
   */
  public TrainingData getTrainingData();


  /**
   * Loads the actual training data from the database, appends the given training data and saves it.
   *
   * @param importedTrainingData a String of the training data format which can be appended to the saved training data
   * @param resetOld a boolean if the existing training data should be kept or overwritten
   * @return true if everything worked
   */
  public boolean appendToTrainingTxt(String importedTrainingData, boolean resetOld);
}
