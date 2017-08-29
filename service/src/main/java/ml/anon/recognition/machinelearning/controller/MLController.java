package ml.anon.recognition.machinelearning.controller;

import java.util.List;

import javax.annotation.Resource;

import ml.anon.recognition.machinelearning.service.IScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import ml.anon.recognition.machinelearning.service.IAnnotationService;
import ml.anon.recognition.machinelearning.service.ITrainingDataService;

/**
 * Handles the http request to the ML module.
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

  @Resource
  private IScoreService scoreService;


  @RequestMapping(value = "/ml/annotate/{id}", method = RequestMethod.POST)
  public List<Anonymization> annotate(@PathVariable String id) {
    Document doc = documentResource.findById(id);

    return annotationService.annotate(doc);

  }

  @RequestMapping(value = "/ml/update/training/data/{id}", method = RequestMethod.POST)
  public boolean updateTrainingData(@PathVariable String id) {

    return trainingDataAccess.updateTrainingData(id);

  }

  @RequestMapping(value = "/ml/calculate/f/one/{id}", method = RequestMethod.POST)
  public boolean postCalculateFOne(@RequestBody List<Anonymization> correctAnonymizations, @PathVariable String id) {

    return scoreService.calculateFOneScore(id, correctAnonymizations);

  }

  @RequestMapping(value = "/ml/post/training/data/", method = RequestMethod.POST)
  public boolean postTrainingData(@RequestParam String importedTrainingData) {

    return trainingDataAccess.appendToTrainingTxt(importedTrainingData);

  }

  @RequestMapping(value = "/ml/get/training/data/", method = RequestMethod.GET)
  public String getTrainingDataAsString() {

    return trainingDataAccess.getTrainingData().getTrainingTxt();

  }

  @RequestMapping(value = "/ml/retrain/", method = RequestMethod.GET)
  public boolean retrain() {

    return annotationService.retrain();

  }




}
