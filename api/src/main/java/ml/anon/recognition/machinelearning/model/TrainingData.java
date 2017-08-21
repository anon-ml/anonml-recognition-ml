package ml.anon.recognition.machinelearning.model;

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

  public boolean addTokens(List<String> tokens) {
    return this.tokens.addAll(tokens);
  }

  public boolean addAnnotations(List<String> annotation) {
    return this.annotations.addAll(annotation);
  }

}
