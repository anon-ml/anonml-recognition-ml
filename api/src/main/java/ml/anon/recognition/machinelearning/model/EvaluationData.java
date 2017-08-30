package ml.anon.recognition.machinelearning.model;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "EvaluationData")
public class EvaluationData {

    private double totalGenerated;
    private double totalCorrected;
    private double totalNumberOfCorrectFound;

    private double overallPrecision;
    private double overallRecall;
    private double overallFOne;

    private List<Double> lastTenPrecision;
    private List<Double> lastTenRecall;
    private List<Double> lastTenFOne;

}
