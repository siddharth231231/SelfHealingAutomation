package selfhealing.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import selfhealing.dom.DomContext;

public class AgentRestClient {

    private static final String AGENT_URL =
            "http://localhost:8081/agent/fix-xpath";

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String requestHealing(DomContext domContext) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DomContext> request =
                    new HttpEntity<>(domContext, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            AGENT_URL,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            return response.getBody();

        } catch (Exception e) {
            System.err.println("[SELF-HEALING] Agent call failed: " + e.getMessage());
            return null;
        }
    }
}
