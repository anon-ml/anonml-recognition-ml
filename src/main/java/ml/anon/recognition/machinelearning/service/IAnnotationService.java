package ml.anon.recognition.machinelearning.service;

import java.util.List;

import ml.anon.model.anonymization.Anonymization;
import ml.anon.model.docmgmt.Document;

public interface IAnnotationService {

  public List<Anonymization> annotate(Document document);
}
