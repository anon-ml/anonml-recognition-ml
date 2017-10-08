package ml.anon.recognition.machinelearning.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import ml.anon.model.BaseEntity;

/**
 * Is basically a object to save the trainings.txt in the database to dynamically extend it and easily export it.
 */
@Data
@Builder
@org.springframework.data.mongodb.core.mapping.Document(collection = "TrainingData")
public class TrainingData extends BaseEntity {

    private String trainingTxt;

}
