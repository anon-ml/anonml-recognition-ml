package ml.anon.recognition.machinelearning.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ml.anon.recognition.machinelearning.model.AnonPlusTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import ml.anon.anonymization.model.Anonymization;
import ml.anon.anonymization.model.Label;
import ml.anon.documentmanagement.model.Document;
import ml.anon.documentmanagement.resource.DocumentResource;
import ml.anon.recognition.machinelearning.model.TrainingData;
import ml.anon.recognition.machinelearning.repository.TrainingDataRepository;

/**
 * Service to set up the new trainings file to later on retrain the model and improve the results.
 * @author Matthias
 */
@Service
public class TrainingDataService implements ITrainingDataService {

  private RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private TrainingDataRepository trainingDataRepository;

  public boolean updateTrainingData(String id) {

    DocumentResource access = new DocumentResource(new RestTemplate());
    Document document = access.findById(id);

    List<TrainingData> trainingDataList = trainingDataRepository.findAll();
    TrainingData trainingData = null;
    if(trainingDataList.size() == 0){
      trainingData = TrainingData.builder().tokens(new ArrayList<String>()).annotations(new ArrayList<String>()).trainingTxt("").build();
    }else if(trainingDataList.size() == 1) {
      trainingData = trainingDataList.get(0);
    }else{

      System.out.print("Error: more then one trainingData file found!");
    }

    List<String> annotations = new ArrayList<String>();

    List<Integer> indexesOfToken = this.indexOfAll("", document.getChunks());

    for (int i = 0; i < document.getChunks().size(); ++i) {
      if (!indexesOfToken.contains(new Integer(i))) {
        annotations.add("O");
      }
      else{
        annotations.add(""); // for the spaces between sentences
      }
    }

    List<AnonPlusTokens> anonsWithTokens = this.getAnonPlusTokens(document);


    for (AnonPlusTokens anons : anonsWithTokens) {
      System.out.println("Anons: "+ anons.getAnonymization().getData().getOriginal());

      indexesOfToken = this.getOccurrencesOfOriginal(document, anons.getTokens());

      for (Integer occurence : indexesOfToken) {
        // -1 because of the empty token produced by the tokenizer
        for (int i = 0; i < anons.getTokens().size() - 1; ++i) {
          if (i == 0) {
            annotations.set(occurence + i, "B-" + anons.getAnonymization().getData().getLabel());
          } else {
            annotations.set(occurence + i, "I-" + anons.getAnonymization().getData().getLabel());
          }
        }
      }
    }

    trainingData.addTokens(document.getChunks());
    trainingData.addAnnotations(annotations);
    this.appendToExisting(trainingData);
       // TrainingData.builder().annotations(annotations).tokens(document.getChunks()).build());
    trainingDataRepository.save(trainingData);

    System.out.println("############################################");
    System.out.println(trainingDataRepository.findAll().get(0).getTrainingTxt());
    System.out.println("############################################");
    return true;
  }

  private List<AnonPlusTokens> getAnonPlusTokens(Document document) {
    List<AnonPlusTokens> anonsWithTokens = new ArrayList<AnonPlusTokens>();

    for (Anonymization anonymization : document.getAnonymizations()) {

      if (anonymization.getData().getLabel().equals(Label.PERSON)
          || anonymization.getData().getLabel().equals(Label.MISC)
          || anonymization.getData().getLabel().equals(Label.ORGANIZATION)
          || anonymization.getData().getLabel().equals(Label.LOCATION)) {

        List<String> tokensOfOriginal = this.tokenize(anonymization.getData().getOriginal());

        anonsWithTokens.add(AnonPlusTokens.builder().anonymization(anonymization).tokens(tokensOfOriginal).build());

      }
    }

    Collections.sort(anonsWithTokens, new Comparator<AnonPlusTokens>() {
      @Override
      public int compare(AnonPlusTokens o1, AnonPlusTokens o2) {
        if(o1.getTokens().size() == o2.getTokens().size()){
          return 0;
        }
        else if(o1.getTokens().size() > o2.getTokens().size()){
          return 1;
        }
        else{
          return -1;
        }
      }
    });
    return anonsWithTokens;
  }

  private List<Integer> getOccurrencesOfOriginal(Document document, List<String> tokensOfOriginal) {

    List<Integer> indexesOfToken = this.indexOfAll(tokensOfOriginal.get(0), document.getChunks());
    List<Integer> occurrencesOfSequence = new ArrayList<Integer>(indexesOfToken);

    System.out.println("Size: " + occurrencesOfSequence.size());

    // -1 because of the blank line token
    for (int i = 1; i < tokensOfOriginal.size()-1; ++i) {
      List<Integer> indexes = this.indexOfAll(tokensOfOriginal.get(i), document.getChunks());

      for (Integer integer : indexesOfToken) {
        if (!indexes.contains(integer + i)) {
          occurrencesOfSequence.remove(integer);
        }
      }
    }
    return occurrencesOfSequence;
  }


  private boolean appendToExisting(TrainingData trainingData) {
    // output the actual trainings file

    for (int i = 0; i < trainingData.getAnnotations().size(); ++i) {
      if(trainingData.getTokens().get(i).equals("")){
        trainingData.appendToTrainingTxt("");

      } else {
        trainingData.appendToTrainingTxt(trainingData.getTokens().get(i) + "  " + trainingData.getAnnotations().get(i));
      }
    }
    System.out.println("File: " + trainingFile.getAbsolutePath());
    return true;
  }

  private List<Integer> indexOfAll(String token, List<String> tokens) {
    ArrayList<Integer> indexList = new ArrayList<Integer>();
    for (int i = 0; i < tokens.size(); i++) {
      if (token.equals(tokens.get(i))) {
        indexList.add(i);
      }
    }
    return indexList;
  }

  private ArrayList<String> tokenize(String original) {

   // System.out.println("original: " + original);
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("text", original);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
        map, headers);
    return restTemplate
        .postForObject(URI.create("http://127.0.0.1:9001/document/tokenize/text"), request,
            ArrayList.class);
  }

}
