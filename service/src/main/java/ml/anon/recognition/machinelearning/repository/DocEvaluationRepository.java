package ml.anon.recognition.machinelearning.repository;

import ml.anon.recognition.machinelearning.model.DocEvaluation;
import ml.anon.recognition.machinelearning.model.EvaluationData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocEvaluationRepository extends MongoRepository<DocEvaluation, String>{

    DocEvaluation findByDocumentId(String documentId);
  
}
