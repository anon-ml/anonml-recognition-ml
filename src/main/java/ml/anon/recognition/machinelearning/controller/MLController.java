package ml.anon.recognition.machinelearning.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ml.anon.model.anonymization.Anonymization;
import ml.anon.model.docmgmt.Document;
import ml.anon.model.docmgmt.DocumentAccess;
import ml.anon.recognition.machinelearning.service.IAnnotationService;

/**
 * 
 * @author Matthias
 *
 */
@RestController
public class MLController {

  @Resource
  private IAnnotationService annotationService;
  private DocumentAccess documentAccess = new DocumentAccess(new RestTemplate());



  @RequestMapping(value = "/ml/annotate/{id}", method = RequestMethod.POST)
  public List<Anonymization> annotate(@PathVariable String id) {
    
    ResponseEntity<Document> resp = documentAccess.getDocument(id);
    Document doc = resp.getBody();

    return annotationService.annotate(doc);
    
  }


}
