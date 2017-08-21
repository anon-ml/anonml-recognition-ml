package ml.anon.recognition.machinelearning.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

  private TrainingDataRepository repo;

  public boolean updateTrainingData(String id) {

    DocumentResource access = new DocumentResource(new RestTemplate());
    Document document = access.findById(id);

//    TrainingData trainingData = repo.findAll().get(0);

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

    for (Anonymization anonymization : document.getAnonymizations()) {

      if (anonymization.getData().getLabel().equals(Label.PERSON)
          || anonymization.getData().getLabel().equals(Label.MISC)
          || anonymization.getData().getLabel().equals(Label.ORGANIZATION)
          || anonymization.getData().getLabel().equals(Label.LOCATION)) {

        List<String> tokensOfOriginal = this.tokenize(anonymization.getData().getOriginal());
        indexesOfToken = this.getOccurrencesOfOriginal(document, tokensOfOriginal);

        for (Integer occurence : indexesOfToken) {
          // -1 because of the empty token produced by the tokenizer
          for (int i = 0; i < tokensOfOriginal.size() - 1; ++i) {
            if (i == 0) {
              annotations.set(occurence + i, "B-" + anonymization.getData().getLabel());
            } else {
              annotations.set(occurence + i, "I-" + anonymization.getData().getLabel());
            }
          }
        }
      }
    }

    // trainingData.addTokens(document.getChunks());
    // trainingData.addAnnotations(annotations);
    // restTemplate.put(IP + "/training/" + id, trainingData);
    this.appendToExisting(
        TrainingData.builder().annotations(annotations).tokens(document.getChunks()).build());

    return true;
  }

  private List<Integer> getOccurrencesOfOriginal(Document document, List<String> tokensOfOriginal) {
    List<Integer> indexesOfToken;
    indexesOfToken = this.indexOfAll(tokensOfOriginal.get(0), document.getChunks());

    for (int i = 1; i < tokensOfOriginal.size(); ++i) {

      List<Integer> indexes = this.indexOfAll(tokensOfOriginal.get(i), document.getChunks());
      for (Integer integer : indexes) {
        if (!indexesOfToken.contains(integer - i)) {
          indexesOfToken.remove(new Integer(integer - i));
        }
      }
    }
    return indexesOfToken;
  }


  private boolean appendToExisting(TrainingData trainingData) {
    // output the actual trainings file
    PrintWriter out;
    try {
      File trainingFile = new File(AnnotationService.pathToTrainingFile);

      out = new PrintWriter(new FileOutputStream(trainingFile, true));

      for (int i = 0; i < trainingData.getAnnotations().size(); ++i) {
        //System.out
          //  .println(trainingData.getTokens().get(i) + "  " + trainingData.getAnnotations().get(i));
        if(trainingData.getTokens().get(i).equals("")){
          out.println("");
        }
        else {
          out.println(trainingData.getTokens().get(i) + "  " + trainingData.getAnnotations().get(i));
        }
      }
      System.out.println("File: " + trainingFile.getAbsolutePath());
      out.close();

    } catch (FileNotFoundException e2) {
      e2.printStackTrace();
      return false;
    }

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
