package ml.anon.recognition.machinelearning.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Is basically a object to save the trainings.txt in the database to dynamically extend it and easily export it.
 */
@Data
@Builder
@org.springframework.data.mongodb.core.mapping.Document(collection = "TrainingData")
public class TrainingData {

  private String id;
  private String trainingTxt;

}
