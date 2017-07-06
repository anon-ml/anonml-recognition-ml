package ml.anon.recognition.machinelearning.service;

import java.util.List;

import ml.anon.model.anonymization.Anonymization;
import ml.anon.model.docmgmt.Document;
import ml.anon.recognition.machinelearning.model.TrainingData;

public interface IAnnotationService {

  public List<Anonymization> annotate(Document document);

  public boolean retrain();
}
