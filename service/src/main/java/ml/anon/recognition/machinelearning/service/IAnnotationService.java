package ml.anon.recognition.machinelearning.service;

import java.util.List;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.documentmanagement.model.Document;

public interface IAnnotationService {

  /**
   * Outputs all tokens of the given document (one token per line) to a temp file to run the NER on it and to get the
   * annotations.
   *
   * @param document the document object of the actual uploaded document
   * @return a list of anonymizations
   */
  public List<Anonymization> annotate(Document document);

  /**
   * Calls outputTrainingData to have a training file to work with. Afterwards initializes the GermaNER component and
   * retrains the model
   *
   * @return true if everything worked
   */
  public boolean retrain();
}
