package ml.anon.recognition.machinelearning.service;

import java.util.List;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.documentmanagement.model.Document;

public interface IAnnotationService {

  public List<Anonymization> annotate(Document document);

  public boolean retrain();
}
