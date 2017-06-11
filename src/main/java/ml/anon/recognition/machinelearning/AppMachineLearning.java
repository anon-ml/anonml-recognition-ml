package ml.anon.recognition.machinelearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ml.anon.recognition.machinelearning.service.AnnotationService;

@SpringBootApplication
public class AppMachineLearning {

    public static void main(String[] args){
//        AnnotationService.initGermaNER();
        SpringApplication.run(AppMachineLearning.class, args);
        
        

    }
}
