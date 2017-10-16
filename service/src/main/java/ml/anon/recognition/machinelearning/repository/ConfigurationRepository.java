package ml.anon.recognition.machinelearning.repository;

import ml.anon.recognition.machinelearning.model.Configuration;
import ml.anon.recognition.machinelearning.model.DocEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigurationRepository extends MongoRepository<Configuration, String> {


    Configuration findLastByResetDate();
}
