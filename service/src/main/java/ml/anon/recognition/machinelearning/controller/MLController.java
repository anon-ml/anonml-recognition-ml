package ml.anon.recognition.machinelearning.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import ml.anon.recognition.machinelearning.service.IAnnotationService;
import ml.anon.recognition.machinelearning.service.ITrainingDataService;

/**
 * @author Matthias
 */
@RestController
public class MLController {

  @Resource
  private IAnnotationService annotationService;
  @Resource
  private DocumentResource documentResource;

  @Resource
  private ITrainingDataService trainingDataAccess;


  @RequestMapping(value = "/ml/annotate/{id}", method = RequestMethod.POST)
  public List<Anonymization> annotate(@PathVariable String id) {
    Document doc = documentResource.findById(id);

    return annotationService.annotate(doc);

  }

  @RequestMapping(value = "/ml/update/training/data/{id}", method = RequestMethod.POST)
  public boolean updateTrainingData(@PathVariable String id) {

    return trainingDataAccess.updateTrainingData(id);

  }


  @RequestMapping(value = "/ml/retrain/", method = RequestMethod.GET)
  public boolean retrain() {

    return annotationService.retrain();

  }


}
