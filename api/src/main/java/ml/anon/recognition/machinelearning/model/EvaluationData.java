package ml.anon.recognition.machinelearning.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import ml.anon.model.BaseEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Data object to store evaluation data of the ml part of the application.
 */
@Data
@NoArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "EvaluationData")
public class EvaluationData extends BaseEntity {

    private Date lastReset;
    private double generated;
    private double corrected;
    private double correctFound;

    private double precision;
    private double recall;
    private double fOne;

}
