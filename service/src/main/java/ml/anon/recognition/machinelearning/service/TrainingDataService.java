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

      indexesOfToken = this.getOccurrencesOfOriginal(document.getChunks(), anons.getTokens());

      for (Integer occurrence : indexesOfToken) {
        // -1 because of the empty token produced by the tokenizer
        for (int i = 0; i < anons.getTokens().size() - 1; ++i) {
          if (i == 0) {
            annotations.set(occurrence + i, "B-" + anons.getAnonymization().getData().getLabel());
          } else {
            annotations.set(occurrence + i, "I-" + anons.getAnonymization().getData().getLabel());
          }
        }
      }
    }

    this.appendToExisting(document.getChunks(), annotations);

    return true;
  }

  /**
   * Searches all occurrences of the given token in the given list of tokens and saves those in a list of indexes.
   *
   * @param token which occurrences should be found
   * @param tokens list of tokens from the document which should be used to update the training data
   * @return list of indexes where the given token occurres in the given token list
   */
  private List<Integer> indexOfAll(String token, List<String> tokens) {
    ArrayList<Integer> indexList = new ArrayList<Integer>();
    for (int i = 0; i < tokens.size(); i++) {
      if (token.equals(tokens.get(i))) {
        indexList.add(i);
      }
    }
    return indexList;
  }

  /**
   * Iterates over the given list of anonymizations of the actual document to tokenize the originals of all
   * anonymization with labels which are set by the ml module (misc, person, location and organization). The tokens
   * and the original anonymization are set in the {@link AnonPlusTokens} object to use the corresponding label.
   * After tokenizing the list of {@link AnonPlusTokens} is sorted by number of tokens of the original to start with
   * the shortest (to not have encapsulated annotations)
   *
   * @param anonymizations list of anonymizations of the actual document. Produces by Ml, Regex and Human interaction
   * @return a sorted list of {@link AnonPlusTokens} objects which hold the tokenized original and the anonymization
   * itself
   */
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

    /**
     * Searches for occurrences of all tokens of an original in a row to get the whole sequence occurrence. The index
     * of the start positions of the sequence are saved in a list and returned to set the annotations.
     *
     * @param tokens list of all tokens of the acutal document
     * @param tokensOfOriginal list of the tokens of the original of the anonymizaion which is looked at
     * @return a list of the sequence start positions
     */
  private List<Integer> getOccurrencesOfOriginal(List<String> tokens, List<String> tokensOfOriginal) {

    List<Integer> indexesOfToken = this.indexOfAll(tokensOfOriginal.get(0), tokens);
    List<Integer> occurrencesOfSequence = new ArrayList<Integer>(indexesOfToken);

    // -1 because of the blank line token
    for (int i = 1; i < tokensOfOriginal.size() - 1; ++i) {
      List<Integer> indexes = this.indexOfAll(tokensOfOriginal.get(i), tokens);

      for (Integer integer : indexesOfToken) {
        if (!indexes.contains(integer + i)) {
          occurrencesOfSequence.remove(integer);
        }
      }
    }
    return occurrencesOfSequence;
  }

    /**
     * Builds the training String from the tokens of the document and the before set annotations.
     *
     * @param tokens list of all tokens of the actual document
     * @param annotations list of annotations corresponding to the tokens (e.g.: B-Person, I-Person, ...)
     * @return true if everything worked
     */
  private boolean appendToExisting(List<String> tokens, List<String> annotations) {

    StringBuilder stringBuilder = new StringBuilder();

    for (int i = 0; i < annotations.size(); ++i) {
      if (tokens.get(i).equals("")) {
        stringBuilder.append("");
      } else {
        stringBuilder.append(tokens.get(i) + "  " + annotations.get(i));
      }
      if(i < annotations.size()-1){
        stringBuilder.append(System.lineSeparator());
      }
    }

    if(!this.appendToTrainingTxt(stringBuilder.toString(), false)){
        return false;
    }

    return true;
  }

  @Override
  public boolean appendToTrainingTxt(String trainingDataToAdd, boolean resetOld) {
      TrainingData trainingData = this.getTrainingData();

      if(resetOld){
        trainingData.setTrainingTxt(trainingDataToAdd);

      } else {

        String trainingTxt = trainingData.getTrainingTxt().trim();
        StringBuilder stringBuilder = new StringBuilder(trainingTxt);
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(trainingDataToAdd);
        trainingData.setTrainingTxt(stringBuilder.toString());
      }

      trainingDataRepository.save(trainingData);

      return true;
  }

  @Override
  public TrainingData getTrainingData() {

    List<TrainingData> trainingDataList = trainingDataRepository.findAll();

    TrainingData trainingData = null;
    if (trainingDataList.size() == 0) {

      trainingData = TrainingData.builder().trainingTxt(AnnotationService.buildInTrainingFile).build();
    } else if (trainingDataList.size() == 1) {
      trainingData = trainingDataList.get(0);
    } else {

      System.err.print("Error: more then one trainingData file found!");
    }
    return trainingData;
  }


    /**
     * Tokenizes the given String with the help of the functionality of the documentmanagement module
     * @param original String to tokenize
     * @return a list of tokens of the given original
     */
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
