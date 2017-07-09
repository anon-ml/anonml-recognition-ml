package ml.anon.recognition.machinelearning;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;

/**
 * Created by mirco on 09.07.17.
 */
public class ResourceUtil {
    /**
     * access the file from the jar
     * @param path
     * @return
     */
    public static String getPath(String path) {
        Resource resource = new ClassPathResource(path);
        BufferedReader reader = null;
        try {
            File tmp = File.createTempFile(RandomStringUtils.randomAlphanumeric(20), "");

            IOUtils.copy(resource.getInputStream(), new FileOutputStream(tmp));
            return tmp.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "NAY";
        }
    }

    public static InputStream getStream(String path) throws IOException {
        return new ClassPathResource(path).getInputStream();
    }

}
