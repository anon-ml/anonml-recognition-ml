package ml.anon.recognition.machinelearning.repository;

import ml.anon.recognition.machinelearning.model.EvaluationData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EvaluationDataRepository extends MongoRepository<EvaluationData, String>{

  
}
