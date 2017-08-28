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
  private String trainingTxt;

}
