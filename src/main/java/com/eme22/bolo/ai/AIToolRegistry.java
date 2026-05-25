package com.eme22.bolo.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AIToolRegistry {

    @Inject
    Instance<AITool> tools;

    public List<AITool> getAvailableTools(String serverMode, Member member) {
        List<AITool> filtered = new ArrayList<>();
        boolean isAdminUser = member != null && (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR));

        for (AITool tool : tools) {
            // 1. Check Chatbot Mode
            if ("NORMAL".equals(serverMode) && "ADMIN".equals(tool.getRequiredMode())) {
                continue; // Normal mode cannot use Admin tools
            }

            // 2. Check Member Discord Permissions
            List<Permission> requiredPerms = tool.getRequiredUserPermissions();
            if (requiredPerms != null && !requiredPerms.isEmpty()) {
                if (member == null) {
                    continue; // Requires a member with permissions
                }
                if (!isAdminUser && !member.hasPermission(requiredPerms)) {
                    continue; // User lacks required Discord permissions
                }
            }

            filtered.add(tool);
        }
        return filtered;
    }

    public Optional<AITool> getTool(String name) {
        for (AITool tool : tools) {
            if (tool.getName().equalsIgnoreCase(name)) {
                return Optional.of(tool);
            }
        }
        return Optional.empty();
    }
}
