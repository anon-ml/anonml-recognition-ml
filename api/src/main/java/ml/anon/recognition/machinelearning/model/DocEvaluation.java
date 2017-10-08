package ml.anon.recognition.machinelearning.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DocEvaluation extends EvaluationData{

    private String documentId;


}
