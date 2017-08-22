package ml.anon.recognition.machinelearning.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@org.springframework.data.mongodb.core.mapping.Document(collection = "TrainingData")
public class TrainingData {

  private String id;
  private List<String> tokens;
  private List<String> annotations;
  private String trainingTxt;

  public boolean addTokens(List<String> tokens) {

    return this.tokens.addAll(tokens);
  }

  public boolean addAnnotations(List<String> annotations) {

    return this.annotations.addAll(annotations);
  }

  public void appendToTrainingTxt(String line){
    StringBuilder stringBuilder = new StringBuilder(this.trainingTxt);
    if(!this.trainingTxt.equals("")){
      stringBuilder.append("\r\n");
    }
    stringBuilder.append(line);
    this.trainingTxt = stringBuilder.toString();
  }

}
