package com.eme22.bolo.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AIToolRegistry {

    @Inject
    Instance<AITool> tools;

    public List<AITool> getAvailableTools(Member member, long adminRoleId) {
        List<AITool> filtered = new ArrayList<>();
        boolean isElevated = member != null && (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR) || hasAdminRole(member, adminRoleId));

        for (AITool tool : tools) {
            List<Permission> requiredPerms = tool.getRequiredUserPermissions();
            if (requiredPerms == null || requiredPerms.isEmpty()) {
                filtered.add(tool);
            } else if (member == null) {
                continue;
            } else if (isElevated) {
                filtered.add(tool);
            } else if (member.hasPermission(requiredPerms)) {
                filtered.add(tool);
            }
        }
        return filtered;
    }

    private boolean hasAdminRole(Member member, long adminRoleId) {
        if (adminRoleId == 0L) return false;
        for (Role role : member.getRoles()) {
            if (role.getIdLong() == adminRoleId) return true;
        }
        return false;
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
