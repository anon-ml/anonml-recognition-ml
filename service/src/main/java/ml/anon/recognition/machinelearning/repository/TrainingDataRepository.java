package ml.anon.recognition.machinelearning.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ml.anon.recognition.machinelearning.model.TrainingData;

public interface TrainingDataRepository extends MongoRepository<TrainingData, String>{

  
}
