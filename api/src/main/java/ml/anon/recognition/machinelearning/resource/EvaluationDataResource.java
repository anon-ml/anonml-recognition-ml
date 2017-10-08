package ml.anon.recognition.machinelearning.resource;

import ml.anon.exception.BadRequestException;
import ml.anon.recognition.machinelearning.model.DocEvaluation;
import ml.anon.recognition.machinelearning.model.EvaluationData;
import ml.anon.resource.Read;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

@Component
public class EvaluationDataResource implements Read<EvaluationData> {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${machinelearning.service.url}")
    private String mlUrl;

    @Override
    public EvaluationData findById(String id) throws BadRequestException {
        try {
            return restTemplate.getForEntity(mlUrl + "/ml/get/evaluation/data/", EvaluationData.class).getBody();
        } catch (Exception e) {
            throw new BadRequestException();
        }
    }

    public DocEvaluation findByDocumentId(String documentId) throws BadRequestException {
        try {
            return restTemplate.getForEntity(mlUrl + "/ml/get/doc/evaluation/" + documentId, DocEvaluation.class).getBody();
        } catch (Exception e) {
            throw new BadRequestException();
        }
    }


}
