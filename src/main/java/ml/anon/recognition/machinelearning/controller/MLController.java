package ml.anon.recognition.machinelearning.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ml.anon.recognition.machinelearning.service.IAnnotationService;

/**
 * 
 * @author Matthias
 *
 */
@RestController
public class MLController {

  @Autowired
  IAnnotationService annotationService;

  @RequestMapping(value = "/ml/annotate", method = RequestMethod.POST)
  public ResponseEntity<?> bulkUpload(@RequestParam("doc") String tokenizedFile)
      throws IOException {
    
    String annotatedFile = annotationService.annotateTokenizedFiletest(tokenizedFile);;
    return ResponseEntity.ok().body(annotatedFile);
  }



}
