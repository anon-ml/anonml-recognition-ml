package ml.anon.recognition.machinelearning.model;

import lombok.Builder;
import lombok.Data;
import ml.anon.anonymization.model.Anonymization;

import java.util.List;


/**
 * Holds the anonymization itself and a list of tokens of the original. Helps to sort the anonymizations by number
 * of tokens.
 */
@Data
@Builder
public class AnonPlusTokens {

    private Anonymization anonymization;
    private List<String> tokens;

}
