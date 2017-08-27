package ml.anon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ml.anon.recognition.machinelearning.service.AnnotationService;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class AppMachineLearning {

  public static void main(String[] args) {
    AnnotationService.initGermaNER();
    SpringApplication.run(AppMachineLearning.class, args);


  }
}
