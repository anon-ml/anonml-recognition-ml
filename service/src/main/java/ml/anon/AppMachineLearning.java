package ml.anon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ml.anon.recognition.machinelearning.service.AnnotationService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Main class of the ML Module. Initializes the GermaNER functionality on startup and keeps it running to save time later
 * on.
 */
@SpringBootApplication
@EnableMongoAuditing
public class AppMachineLearning {

    public static void main(String[] args) {
        AnnotationService.initGermaNER();
        SpringApplication.run(AppMachineLearning.class, args);


    }
}
