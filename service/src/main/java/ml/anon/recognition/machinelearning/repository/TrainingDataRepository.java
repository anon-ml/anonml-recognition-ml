package ml.anon.recognition.machinelearning.repository;



import ml.anon.recognition.machinelearning.model.TrainingData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainingDataRepository extends MongoRepository<TrainingData, String> {

  
}
