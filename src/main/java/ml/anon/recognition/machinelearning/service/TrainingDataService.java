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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import ml.anon.model.anonymization.Anonymization;
import ml.anon.model.anonymization.Label;
import ml.anon.model.docmgmt.Document;
import ml.anon.model.docmgmt.DocumentAccess;
import ml.anon.recognition.machinelearning.model.TrainingData;
import ml.anon.recognition.machinelearning.repository.TrainingDataRepository;

/**
 * @author Matthias
 */
@Service
public class TrainingDataService implements ITrainingDataService{

  private RestTemplate restTemplate = new RestTemplate();

  private TrainingDataRepository repo;

  public boolean updateTrainingData(String id) {

    DocumentAccess access = new DocumentAccess(new RestTemplate());
    Document document = access.getDocument(id).getBody();

//    TrainingData trainingData = repo.findAll().get(0);

    List<String> annotations = new ArrayList<String>();
    for (int i = 0; i < document.getChunks().size(); ++i) {
      annotations.add("O");
    }


    for (Anonymization anonymization : document.getAnonymizations()) {

      if (anonymization.getLabel().equals(Label.PERSON)
          || anonymization.getLabel().equals(Label.MISC)
          || anonymization.getLabel().equals(Label.ORGANIZATION)
          || anonymization.getLabel().equals(Label.LOCATION)) {


        List<String> tokensOfOriginal = this.tokenize(anonymization.getOriginal());
        List<Integer> indexesOf = this.indexOfAll(tokensOfOriginal.get(0), document.getChunks());

        for (int i = 1; i < tokensOfOriginal.size(); ++i) {

          List<Integer> indexes = this.indexOfAll(tokensOfOriginal.get(i), document.getChunks());
          for (Integer integer : indexes) {
            if (!indexesOf.contains(integer - i)) {
//              indexesOf.remove(integer - i);
              indexesOf.remove(new Integer(integer-i));
            }
          }
        }

        for (Integer occurence : indexesOf) {
          for (int i = 0; i < tokensOfOriginal.size(); ++i) {
            if (i == 0) {
              annotations.set(occurence + i, "B-" + anonymization.getLabel());
            } else {
              annotations.set(occurence + i, "I-" + anonymization.getLabel());
            }
          }
        }
      }
    }

    // trainingData.addTokens(document.getChunks());
    // trainingData.addAnnotaions(annotations);
    // restTemplate.put(IP + "/training/" + id, trainingData);
    
    
 
    this.appendToExisting(TrainingData.builder().annotations(annotations).tokens(document.getChunks()).build());



    return true;
  }



  private boolean appendToExisting(TrainingData trainingData) {
    // output the actual trainings file
    PrintWriter out;
    try {
      File trainingFile = new File(AnnotationService.pathToTrainingFile);
      
      out = new PrintWriter(new FileOutputStream(trainingFile, true));

      for (int i = 0; i < trainingData.getAnnotations().size(); ++i) {
        System.out.println(trainingData.getTokens().get(i) + " " + trainingData.getAnnotations().get(i));
//        out.println(trainingData.getTokens().get(i) + " " + trainingData.getAnnotations().get(i));
      }

      out.close();

    } catch (FileNotFoundException e2) {
      e2.printStackTrace();
      return false;
    }

    return true;

  }

  private List<Integer> indexOfAll(String token, List<String> list) {
    ArrayList<Integer> indexList = new ArrayList<Integer>();
    for (int i = 0; i < list.size(); i++)
      if (token.equals(list.get(i)))
        indexList.add(i);
    return indexList;
  }

  private ArrayList<String> tokenize(String original) {
    
    System.out.println("original: " + original);
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("text", original);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
    return restTemplate
        .postForObject(URI.create("http://127.0.0.1:9001/document/tokenize/text"), request, ArrayList.class);
  }

}
