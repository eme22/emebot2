package com.eme22.bolo.ai;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
@Slf4j
public class ExaMcpClient {

    private static final String PROTOCOL_VERSION = "2025-06-18";
    private static final String CLIENT_NAME = "embot";
    private static final String CLIENT_VERSION = "0.1.2";
    private static final String SEARCH_TOOL = "web_search_exa";

    @ConfigProperty(name = "exa.mcp-url", defaultValue = "https://mcp.exa.ai/mcp?tools=web_search_exa")
    String mcpUrl;

    @ConfigProperty(name = "exa.api-key", defaultValue = "")
    String apiKey;

    @ConfigProperty(name = "exa.timeout-seconds", defaultValue = "30")
    int timeoutSeconds;

    private HttpClient httpClient;
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final AtomicReference<String> negotiatedVersion = new AtomicReference<>(PROTOCOL_VERSION);
    private final AtomicLong requestId = new AtomicLong(1);
    private final Object sessionLock = new Object();

    @PostConstruct
    void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Realiza una búsqueda web a través del servidor MCP hospedado de Exa y devuelve
     * los resultados formateados como texto listo para el LLM.
     */
    public String search(String query) throws Exception {
        try {
            return callTool(SEARCH_TOOL, new JSONObject().put("query", query));
        } catch (McpSessionExpiredException e) {
            log.info("[Exa MCP] Sesión expirada o inválida, reinicializando y reintentando búsqueda.");
            synchronized (sessionLock) {
                initializeSession();
            }
            return callTool(SEARCH_TOOL, new JSONObject().put("query", query));
        }
    }

    private void initializeSession() throws Exception {
        long id = requestId.getAndIncrement();
        JSONObject initRequest = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", id)
                .put("method", "initialize")
                .put("params", new JSONObject()
                        .put("protocolVersion", PROTOCOL_VERSION)
                        .put("capabilities", new JSONObject())
                        .put("clientInfo", new JSONObject()
                                .put("name", CLIENT_NAME)
                                .put("version", CLIENT_VERSION)));

        HttpResponse<String> response = sendRequest(initRequest, null);
        if (response.statusCode() != 200) {
            throw new Exception("Inicialización MCP falló con HTTP " + response.statusCode());
        }

        JSONObject json = parseJsonRpcResponse(response, id);
        if (json.has("error")) {
            throw new Exception("Error MCP en initialize: " + extractErrorMessage(json));
        }

        JSONObject result = json.optJSONObject("result");
        if (result != null && !result.optString("protocolVersion").isEmpty()) {
            negotiatedVersion.set(result.getString("protocolVersion"));
        }

        String newSessionId = response.headers().firstValue("Mcp-Session-Id").orElse(null);
        sessionId.set(newSessionId);

        // Spec MCP: notificar 'initialized' tras el handshake exitoso
        JSONObject notification = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("method", "notifications/initialized");
        HttpResponse<String> notifResponse = sendRequest(notification, newSessionId);
        if (notifResponse.statusCode() >= 400) {
            throw new Exception("Notificación MCP 'initialized' rechazada con HTTP " + notifResponse.statusCode());
        }

        log.info("[Exa MCP] Sesión inicializada correctamente (protocolo {}).", negotiatedVersion.get());
    }

    private String callTool(String toolName, JSONObject arguments) throws Exception {
        String session = sessionId.get();
        if (session == null) {
            synchronized (sessionLock) {
                if (sessionId.get() == null) {
                    initializeSession();
                }
            }
            session = sessionId.get();
        }

        long id = requestId.getAndIncrement();
        JSONObject call = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", id)
                .put("method", "tools/call")
                .put("params", new JSONObject()
                        .put("name", toolName)
                        .put("arguments", arguments));

        HttpResponse<String> response = sendRequest(call, session);

        // 404/400 suelen indicar sesión inválida o expirada según el spec MCP
        if (response.statusCode() == 404 || response.statusCode() == 400) {
            throw new McpSessionExpiredException();
        }
        if (response.statusCode() == 429) {
            throw new ExaRateLimitException();
        }
        if (response.statusCode() != 200) {
            throw new Exception("Exa MCP devolvió HTTP " + response.statusCode());
        }

        JSONObject json = parseJsonRpcResponse(response, id);
        if (json.has("error")) {
            String message = extractErrorMessage(json);
            if (isRateLimit(message)) throw new ExaRateLimitException();
            throw new Exception(message);
        }

        JSONObject result = json.optJSONObject("result");
        if (result == null) {
            throw new Exception("Respuesta MCP sin 'result'.");
        }

        StringBuilder text = new StringBuilder();
        if (result.has("content") && !result.isNull("content")) {
            JSONArray content = result.getJSONArray("content");
            for (int i = 0; i < content.length(); i++) {
                JSONObject part = content.getJSONObject(i);
                if ("text".equals(part.optString("type")) && part.has("text")) {
                    if (text.length() > 0) text.append("\n\n");
                    text.append(part.getString("text"));
                }
            }
        }

        String output = text.toString();
        if (result.optBoolean("isError", false)) {
            if (isRateLimit(output)) throw new ExaRateLimitException();
            throw new Exception(output.isEmpty() ? "El servidor de búsqueda devolvió un error." : output);
        }
        if (output.isEmpty()) {
            throw new Exception("El servidor de búsqueda no devolvió contenido.");
        }
        return output;
    }

    private HttpResponse<String> sendRequest(JSONObject body, String session) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Protocol-Version", negotiatedVersion.get())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));

        if (session != null) {
            builder.header("Mcp-Session-Id", session);
        }
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("x-api-key", apiKey.trim());
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Extrae la respuesta JSON-RPC de una respuesta HTTP que puede venir como
     * JSON plano o como stream SSE (text/event-stream).
     */
    private JSONObject parseJsonRpcResponse(HttpResponse<String> response, long id) throws Exception {
        String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase();
        String body = response.body();

        if (contentType.contains("text/event-stream")) {
            String candidate = extractSseJson(body, id);
            if (candidate != null) {
                return new JSONObject(candidate);
            }
            throw new Exception("No se encontró respuesta JSON-RPC en el stream SSE de Exa: " + truncate(body));
        }

        JSONObject json = new JSONObject(body);
        if (json.optLong("id", Long.MIN_VALUE) == id || json.has("result") || json.has("error")) {
            return json;
        }
        throw new Exception("Respuesta JSON-RPC inesperada de Exa: " + truncate(body));
    }

    /**
     * Recorre los eventos SSE y devuelve el payload JSON-RPC correspondiente al id solicitado.
     */
    private String extractSseJson(String sseBody, long id) {
        StringBuilder currentData = new StringBuilder();
        String fallback = null;

        for (String rawLine : sseBody.split("\\R")) {
            String line = rawLine.stripTrailing();
            if (line.startsWith("data:")) {
                String payload = line.length() > 5 ? line.substring(5).trim() : "";
                if (!payload.isEmpty()) {
                    if (currentData.length() > 0) currentData.append("\n");
                    currentData.append(payload);
                }
            } else if (line.isEmpty() && currentData.length() > 0) {
                String event = currentData.toString();
                currentData.setLength(0);
                if (matchesRpcId(event, id)) return event;
                if (fallback == null && looksLikeRpcResponse(event)) fallback = event;
            }
        }

        if (currentData.length() > 0) {
            String event = currentData.toString();
            if (matchesRpcId(event, id)) return event;
            if (fallback == null && looksLikeRpcResponse(event)) fallback = event;
        }
        return fallback;
    }

    private boolean matchesRpcId(String json, long id) {
        try {
            return new JSONObject(json).optLong("id", Long.MIN_VALUE) == id;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean looksLikeRpcResponse(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.has("result") || obj.has("error");
        } catch (Exception e) {
            return false;
        }
    }

    private String extractErrorMessage(JSONObject json) {
        JSONObject error = json.optJSONObject("error");
        if (error == null) return "Error desconocido de Exa MCP.";
        return error.optString("message", "Error desconocido de Exa MCP.");
    }

    private boolean isRateLimit(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("rate limit") || lower.contains("rate-limit") || lower.contains("free mcp");
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() <= 500 ? s : s.substring(0, 500) + "...";
    }

    /** El límite gratuito diario de Exa MCP fue alcanzado. */
    public static class ExaRateLimitException extends Exception {
        public ExaRateLimitException() {
            super("Límite diario de consultas gratuitas de Exa alcanzado.");
        }
    }

    /** Excepción interna: la sesión MCP ya no es válida y debe reinicializarse. */
    private static class McpSessionExpiredException extends Exception {
    }
}
