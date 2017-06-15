package ml.anon.recognition.machinelearning.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ml.anon.recognition.machinelearning.service.AnnotationService;
import ml.anon.recognition.machinelearning.service.IAnnotationService;

/**
 * 
 * @author Matthias
 *
 */
@Controller
public class MLController {

  @Autowired
  IAnnotationService annotationService;
  
  @Autowired
  AnnotationService annotationServiceImpl;

  @RequestMapping(value = "/ml/annotate", method = RequestMethod.POST)
  public ResponseEntity<?> annotate(@RequestParam("tokenizedFile") String tokenizedFile)
      throws IOException {
    
    String annotatedFile = annotationService.annotateTokenizedFile(tokenizedFile);;
    return ResponseEntity.ok().body(annotatedFile);
  }

  /**
   * For test purpose only!
   * @return
   */
  @RequestMapping(value = "/testAnnotate", method = RequestMethod.GET)
  public void init() {
    System.out.println("testAnnotate accessed!");
    
    annotationServiceImpl.initGermaNER();
    
  }
  


}
