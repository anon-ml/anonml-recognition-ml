package ml.anon.recognition.machinelearning.controller;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.Objects;
import javax.annotation.Resource;

import lombok.extern.java.Log;
import ml.anon.exception.BadRequestException;
import ml.anon.recognition.machinelearning.service.IScoreService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
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
  private ITrainingDataService trainingDataAccess;

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

    return trainingDataAccess.updateTrainingData(id);

  }

  @RequestMapping(value = "/ml/calculate/f/one/{id}", method = RequestMethod.POST)
  public boolean postCalculateFOne(@RequestBody List<Anonymization> correctAnonymizations,
      @PathVariable String id) {

    return scoreService.calculateFOneScore(id, correctAnonymizations);

  }

  @RequestMapping(value = "/ml/post/training/data/", method = RequestMethod.POST)
  public boolean postTrainingData(@RequestBody String importedTrainingData) {

    return trainingDataAccess.appendToTrainingTxt(importedTrainingData);

  }

  @RequestMapping(value = "/ml/get/training/data/", method = RequestMethod.GET)
  public String getTrainingDataAsString() {

    return trainingDataAccess.getTrainingData().getTrainingTxt();

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
