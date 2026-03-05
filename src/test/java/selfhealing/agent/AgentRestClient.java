package selfhealing.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;
import selfhealing.context.SelfHealingContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AgentRestClient {

    private static final String DEFAULT_AGENT_URL = "http://localhost:8081/agent/fix-xpath";
    private static final int REQUEST_LOG_MAX = 4000;
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AgentRestClient() {
    }

    public static List<String> requestHealing(Object payload, String agentUrl) {
        List<String> endpoints = candidateEndpoints(agentUrl);
        Map<String, String> errors = new LinkedHashMap<>();

        for (String endpoint : endpoints) {
            try {
                List<String> suggestions = callEndpoint(endpoint, payload);
                if (!suggestions.isEmpty()) {
                    if (!endpoint.equals(agentUrl)) {
                        System.out.println("[SELF-HEALING] Agent fallback endpoint used: " + endpoint);
                    }
                    return suggestions;
                }
            } catch (HttpStatusCodeException e) {
                String msg = e.getStatusCode() + " : " + trimBody(e.getResponseBodyAsString());
                errors.put(endpoint, msg);
                if (!is404(e)) {
                    break;
                }
            } catch (Exception e) {
                errors.put(endpoint, e.getMessage());
            }
        }

        for (Map.Entry<String, String> error : errors.entrySet()) {
            System.err.println("[SELF-HEALING] Agent call failed @ " + error.getKey() + ": " + error.getValue());
        }
        return new ArrayList<>();
    }

    // Backward compatible signature used by old V1 code paths.
    public static String requestHealing(SelfHealingContext context) {
        List<String> suggestions = requestHealing(context, DEFAULT_AGENT_URL);
        return suggestions.isEmpty() ? null : suggestions.get(0);
    }

    private static List<String> parseSuggestions(String rawBody) {
        if (rawBody == null || rawBody.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String body = rawBody.trim();
        if (looksLikeXpath(body)) {
            return List.of(body);
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            Set<String> out = new LinkedHashSet<>();
            collect(root, out);
            return new ArrayList<>(out);
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    private static List<String> callEndpoint(String endpoint, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        logOutgoingPayload(endpoint, payload);
        HttpEntity<Object> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = REST_TEMPLATE.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
        );
        return parseSuggestions(response.getBody());
    }

    private static List<String> candidateEndpoints(String configuredUrl) {
        Set<String> urls = new LinkedHashSet<>();
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            urls.add(configuredUrl.trim());
        } else {
            urls.add(DEFAULT_AGENT_URL);
        }

        String base = baseUrl(urls.iterator().next());
        if (base != null) {
            urls.add(base + "/api/heal");
            urls.add(base + "/agent/fix-xpath");
            urls.add(base + "/heal");
        }
        return new ArrayList<>(urls);
    }

    private static String baseUrl(String url) {
        try {
            URI uri = URI.create(url);
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .scheme(uri.getScheme())
                    .host(uri.getHost());
            if (uri.getPort() != -1) {
                builder.port(uri.getPort());
            }
            return builder.build().toUriString();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean is404(HttpStatusCodeException e) {
        return e.getStatusCode().value() == 404;
    }

    private static String trimBody(String body) {
        if (body == null) {
            return "";
        }
        String trimmed = body.trim();
        return trimmed.length() > 300 ? trimmed.substring(0, 300) : trimmed;
    }

    private static void logOutgoingPayload(String endpoint, Object payload) {
        try {
            String asJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            System.out.println("[SELF-HEALING] Agent request payload @ " + endpoint + ":\n"
                    + clip(asJson, REQUEST_LOG_MAX));
        } catch (Exception e) {
            System.out.println("[SELF-HEALING] Agent request payload @ " + endpoint
                    + " could not be serialized. payloadType="
                    + (payload == null ? "null" : payload.getClass().getName())
                    + ", error=" + e.getMessage());
        }
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "null";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= max) {
            return trimmed;
        }
        return trimmed.substring(0, max) + "...[truncated]";
    }

    private static void collect(JsonNode node, Set<String> out) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isTextual()) {
            String value = node.asText();
            if (looksLikeXpath(value)) {
                out.add(value.trim());
            }
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                collect(item, out);
            }
            return;
        }

        if (node.isObject()) {
            addIfXpath(node.get("healedXpath"), out);
            addIfXpath(node.get("xpath"), out);
            addIfXpath(node.get("suggestedXpath"), out);
            collect(node.get("suggestions"), out);
            collect(node.get("candidates"), out);
            collect(node.get("results"), out);
            return;
        }
    }

    private static void addIfXpath(JsonNode node, Set<String> out) {
        if (node == null || !node.isTextual()) {
            return;
        }
        String value = node.asText();
        if (looksLikeXpath(value)) {
            out.add(value.trim());
        }
    }

    private static boolean looksLikeXpath(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        return v.startsWith("/") || v.startsWith("(");
    }
}
