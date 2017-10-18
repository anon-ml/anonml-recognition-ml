package ml.anon.recognition.machinelearning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ml.anon.model.BaseEntity;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "Configuration")
public class Configuration extends BaseEntity {

    private Date resetDate;
}
