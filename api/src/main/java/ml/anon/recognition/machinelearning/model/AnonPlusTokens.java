package ml.anon.recognition.machinelearning.model;

import lombok.Builder;
import lombok.Data;
import ml.anon.anonymization.model.Anonymization;

import java.util.List;

@Data
@Builder
public class AnonPlusTokens {

    private Anonymization anonymization;
    private List<String> tokens;

}
