package com.eme22.bolo.ai.tools;

import com.eme22.bolo.ai.AITool;
import com.eme22.bolo.ai.ExaMcpClient;
import com.eme22.bolo.ai.OpenAIDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WebSearchTools {

    @Inject
    ExaMcpClient exaMcpClient;

    @Produces
    @ApplicationScoped
    public AITool webSearchTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "web_search";
            }

            @Override
            public String getDescription() {
                return "Realiza búsquedas en internet en tiempo real con el motor de búsqueda Exa y devuelve los resultados más relevantes (títulos, URLs y extractos). Úsala para noticias, eventos actuales, datos recientes o cualquier información que requiera datos actualizados que no conozcas con certeza.";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();

                Map<String, Object> queryProp = new HashMap<>();
                queryProp.put("type", "string");
                queryProp.put("description", "La consulta de búsqueda en lenguaje natural o palabras clave. Sé específico para obtener mejores resultados.");
                props.put("query", queryProp);

                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(props)
                                        .required(Collections.singletonList("query"))
                                        .build())
                                .build())
                        .build();
            }

            @Override
            public List<Permission> getRequiredUserPermissions() {
                return Collections.emptyList();
            }

            @Override
            public String execute(MessageReceivedEvent event, Map<String, Object> arguments) {
                Object query = arguments.get("query");
                if (query == null || query.toString().trim().isEmpty()) {
                    return "Error: Falta el argumento requerido 'query'.";
                }

                try {
                    return exaMcpClient.search(query.toString().trim());
                } catch (ExaMcpClient.ExaRateLimitException e) {
                    return "El servicio de búsqueda en internet (Exa) alcanzó su límite diario de consultas gratuitas. No hay resultados disponibles por ahora; inténtalo de nuevo más tarde.";
                } catch (Exception e) {
                    return "Error al realizar la búsqueda en internet: " + e.getMessage();
                }
            }
        };
    }
}
