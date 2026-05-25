package com.eme22.bolo.ai;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.List;
import java.util.Map;

public interface AITool {
    String getName();
    String getDescription();
    OpenAIDTO.Tool getDefinition();
    String getRequiredMode(); // "NORMAL" or "ADMIN"
    List<Permission> getRequiredUserPermissions();
    String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception;
}
