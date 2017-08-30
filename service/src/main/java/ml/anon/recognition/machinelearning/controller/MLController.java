package ml.anon.recognition.machinelearning.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.java.Log;
import ml.anon.exception.BadRequestException;
import ml.anon.recognition.machinelearning.model.EvaluationData;
import ml.anon.recognition.machinelearning.service.IScoreService;
import org.springframework.web.bind.annotation.*;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import ml.anon.recognition.machinelearning.service.IAnnotationService;
import ml.anon.recognition.machinelearning.service.ITrainingDataService;

/**
 * Handles the http request to the ML module.
 *
 * @author Matthias
 */
@Log
@RestController
public class MLController {

  @Resource
  private IAnnotationService annotationService;

  @Resource
  private DocumentResource documentResource;

  @Resource
  private ITrainingDataService trainingDataService;

  @Resource
  private IScoreService scoreService;

  private boolean retrains = false;
  private LocalDateTime retrainStarted;


  @RequestMapping(value = "/ml/annotate/{id}", method = RequestMethod.POST)
  public List<Anonymization> annotate(@PathVariable String id) {
    Document doc = documentResource.findById(id);

    return annotationService.annotate(doc);

  }

  @RequestMapping(value = "/ml/update/training/data/{id}", method = RequestMethod.POST)
  public boolean updateTrainingData(@PathVariable String id) {

    return trainingDataService.updateTrainingData(id);

  }

  @RequestMapping(value = "/ml/calculate/f/one/{id}", method = RequestMethod.POST)
  public boolean postCalculateFOne(@RequestBody List<Anonymization> correctAnonymizations,
      @PathVariable String id) {

    return scoreService.calculateScores(id, correctAnonymizations);

  }

  @RequestMapping(value = "/ml/get/evaluation/data/", method = RequestMethod.GET)
  public EvaluationData getEvaluationData() {

    return scoreService.getEvaluationData();

  }

  @RequestMapping(value = "/ml/post/training/data/", method = RequestMethod.POST)
  public boolean postTrainingData(@RequestBody String importedTrainingData) {

    return trainingDataService.appendToTrainingTxt(importedTrainingData);

  }

  @RequestMapping(value = "/ml/get/training/data/", method = RequestMethod.GET)
  public String getTrainingDataAsString() {

    return trainingDataService.getTrainingData().getTrainingTxt();

  }

  @RequestMapping(value = "/ml/retrain/", method = RequestMethod.GET)
  public boolean retrain() {

    if (retrains) {
      throw new BadRequestException("Already retraining");
    } else {
      log.info("Started retrain");
      retrains = true;
      retrainStarted = LocalDateTime.now();
      boolean retrain = annotationService.retrain();
      retrains = false;
      log.info("Finished retrain, result: " + retrain);
      return retrain;
    }
  }

  @RequestMapping(value = "/ml/retrain/status", method = RequestMethod.GET)
  String getStatus() {
    return retrains ? retrainStarted.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
  }


}
