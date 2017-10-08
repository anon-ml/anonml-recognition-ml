package ml.anon.recognition.machinelearning.service;

import java.util.List;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.documentmanagement.model.Document;

/**
 * Holds the whole functionality to annotate a tokenized file to receive the annotations and convert them into
 * useable {@link Anonymization}. In addition it handles the retraining of the model by using the stored
 * training data.
 */
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
