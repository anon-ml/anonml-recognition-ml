package ml.anon.recognition.machinelearning.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;
import ml.anon.recognition.machinelearning.model.AnonPlusTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 *
 * @author Matthias
 */
@Service
public class TrainingDataService implements ITrainingDataService {

  private RestTemplate restTemplate = new RestTemplate();

  @Value("${documentmanagement.service.url}")
  private String documentManagementUrl;

  @Resource
  private DocumentResource documentResource;

  @Autowired
  private TrainingDataRepository trainingDataRepository;

  public boolean updateTrainingData(String documentId) {

    Document document = documentResource.findById(documentId);

    List<String> annotations = new ArrayList<String>();
    List<Integer> indexesOfToken = this.indexOfAll("", document.getChunks());

    for (int i = 0; i < document.getChunks().size(); ++i) {
      if (!indexesOfToken.contains(new Integer(i))) {
        annotations.add("O");
      } else {
        annotations.add(""); // for the spaces between sentences
      }
    }

    List<AnonPlusTokens> anonsWithTokens = this.getAnonPlusTokens(document.getAnonymizations());

    for (AnonPlusTokens anons : anonsWithTokens) {

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

    this.appendToExisting(document.getChunks(), annotations);

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

  private List<AnonPlusTokens> getAnonPlusTokens(List<Anonymization> anonymizations) {
    List<AnonPlusTokens> anonsWithTokens = new ArrayList<AnonPlusTokens>();

    for (Anonymization anonymization : anonymizations) {

      if (anonymization.getData().getLabel().equals(Label.PERSON)
          || anonymization.getData().getLabel().equals(Label.MISC)
          || anonymization.getData().getLabel().equals(Label.ORGANIZATION)
          || anonymization.getData().getLabel().equals(Label.LOCATION)) {

        List<String> tokensOfOriginal = this.tokenize(anonymization.getData().getOriginal());

        anonsWithTokens.add(
            AnonPlusTokens.builder().anonymization(anonymization).tokens(tokensOfOriginal).build());

      }
    }

    Collections.sort(anonsWithTokens, new Comparator<AnonPlusTokens>() {
      @Override
      public int compare(AnonPlusTokens o1, AnonPlusTokens o2) {
        if (o1.getTokens().size() == o2.getTokens().size()) {
          return 0;
        } else if (o1.getTokens().size() > o2.getTokens().size()) {
          return 1;
        } else {
          return -1;
        }
      }
    });
    return anonsWithTokens;
  }

  private List<Integer> getOccurrencesOfOriginal(Document document, List<String> tokensOfOriginal) {

    List<Integer> indexesOfToken = this.indexOfAll(tokensOfOriginal.get(0), document.getChunks());
    List<Integer> occurrencesOfSequence = new ArrayList<Integer>(indexesOfToken);

    // -1 because of the blank line token
    for (int i = 1; i < tokensOfOriginal.size() - 1; ++i) {
      List<Integer> indexes = this.indexOfAll(tokensOfOriginal.get(i), document.getChunks());

      for (Integer integer : indexesOfToken) {
        if (!indexes.contains(integer + i)) {
          occurrencesOfSequence.remove(integer);
        }
      }
    }
    return occurrencesOfSequence;
  }


  private boolean appendToExisting(List<String> tokens, List<String> annotations) {
    // output the actual trainings file

    StringBuilder stringBuilder = new StringBuilder();

    for (int i = 0; i < annotations.size(); ++i) {
      if (tokens.get(i).equals("")) {
        stringBuilder.append("");
      } else {
        stringBuilder.append(tokens.get(i) + "  " + annotations.get(i));
      }
      if(i < annotations.size()-1){
        stringBuilder.append("\r\n");
      }
    }

    this.appendToTrainingTxt(stringBuilder.toString());

    return true;
  }

  @Override
  public TrainingData getTrainingData() {
    List<TrainingData> trainingDataList = trainingDataRepository.findAll();

    TrainingData trainingData = null;
    if (trainingDataList.size() == 0) {
      trainingData = TrainingData.builder().trainingTxt("").build();
    } else if (trainingDataList.size() == 1) {
      trainingData = trainingDataList.get(0);
    } else {

      System.err.print("Error: more then one trainingData file found!");
    }
    return trainingData;
  }

  @Override
  public boolean appendToTrainingTxt(String trainingDataToAdd) {

    TrainingData trainingData = this.getTrainingData();

    String trainingTxt = trainingData.getTrainingTxt();

    StringBuilder stringBuilder = new StringBuilder(trainingTxt);
    if(!trainingTxt.equals("")){
      stringBuilder.append("\r\n");
    }
    stringBuilder.append(trainingDataToAdd);
    trainingData.setTrainingTxt(stringBuilder.toString());

    trainingDataRepository.save(trainingData);

    return true;
  }

  private ArrayList<String> tokenize(String original) {

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("text", original);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
        map, headers);
    return restTemplate
        .postForObject(URI.create(documentManagementUrl + "/document/tokenize/text"), request,
            ArrayList.class);
  }



}
