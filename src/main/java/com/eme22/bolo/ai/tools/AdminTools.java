package com.eme22.bolo.ai.tools;

import com.eme22.bolo.Bot;
import com.eme22.bolo.ai.AITool;
import com.eme22.bolo.ai.OpenAIDTO;
import com.eme22.bolo.model.Server;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.awt.Color;
import java.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import com.eme22.bolo.model.RoleManager;

@ApplicationScoped
public class AdminTools {

    @Inject
    Bot bot;

    private Map<String, Object> createStringProp(String desc) {
        Map<String, Object> p = new HashMap<>();
        p.put("type", "string");
        p.put("description", desc);
        return p;
    }

    private Map<String, Object> createBooleanProp(String desc) {
        Map<String, Object> p = new HashMap<>();
        p.put("type", "boolean");
        p.put("description", desc);
        return p;
    }

    @Produces
    @ApplicationScoped
    public AITool getGetServerStructureTool() {
        return new AITool() {
            @Override public String getName() { return "get_server_structure"; }
            @Override public String getDescription() { return "Obtiene la lista completa de canales, categorías y roles del servidor para que la IA los analice y sugiera mejoras."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(new HashMap<>()).required(new ArrayList<>()).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_SERVER); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                Guild guild = event.getGuild();
                StringBuilder sb = new StringBuilder("Estructura del Servidor:\n\nCategorías y Canales:\n");
                for (Category category : guild.getCategories()) {
                    sb.append("- Categoría: ").append(category.getName()).append(" (ID: ").append(category.getId()).append(")\n");
                    for (GuildChannel channel : category.getChannels()) {
                        sb.append("  * Canal: ").append(channel.getName()).append(" (Tipo: ").append(channel.getType()).append(", ID: ").append(channel.getId()).append(")\n");
                    }
                }
                sb.append("\nCanales sin Categoría:\n");
                for (GuildChannel channel : guild.getChannels()) {
                    if (channel instanceof net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel catChan) {
                        if (catChan.getParentCategory() == null) {
                            sb.append("- ").append(channel.getName()).append(" (Tipo: ").append(channel.getType()).append(", ID: ").append(channel.getId()).append(")\n");
                        }
                    } else {
                        sb.append("- ").append(channel.getName()).append(" (Tipo: ").append(channel.getType()).append(", ID: ").append(channel.getId()).append(")\n");
                    }
                }
                sb.append("\nRoles del Servidor:\n");
                for (Role role : guild.getRoles()) {
                    if (!role.isPublicRole()) {
                        sb.append("- Rol: ").append(role.getName()).append(" (ID: ").append(role.getId()).append(", Color: #").append(Integer.toHexString(role.getColorRaw())).append(")\n");
                    }
                }
                return sb.toString();
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCreateTextChannelTool() {
        return new AITool() {
            @Override public String getName() { return "create_text_channel"; }
            @Override public String getDescription() { return "Crea un nuevo canal de texto en el servidor en una categoría opcional."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("name", createStringProp("Nombre del nuevo canal de texto."));
                props.put("categoryId", createStringProp("ID opcional de la categoría donde colocar el canal."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("name")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String catId = (String) arguments.get("categoryId");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";

                var action = guild.createTextChannel(name);
                if (catId != null && !catId.trim().isEmpty()) {
                    Category category = guild.getCategoryById(catId);
                    if (category != null) {
                        action = action.setParent(category);
                    }
                }
                TextChannel created = action.complete();
                return String.format("Se ha creado exitosamente el canal de texto `#%s` (ID: %s) en la categoría '%s'.",
                        created.getName(), created.getId(), created.getParentCategory() != null ? created.getParentCategory().getName() : "Ninguna");
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCreateVoiceChannelTool() {
        return new AITool() {
            @Override public String getName() { return "create_voice_channel"; }
            @Override public String getDescription() { return "Crea un nuevo canal de voz en el servidor."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("name", createStringProp("Nombre del nuevo canal de voz."));
                props.put("categoryId", createStringProp("ID opcional de la categoría donde colocar el canal."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("name")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String catId = (String) arguments.get("categoryId");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";

                var action = guild.createVoiceChannel(name);
                if (catId != null && !catId.trim().isEmpty()) {
                    Category category = guild.getCategoryById(catId);
                    if (category != null) {
                        action = action.setParent(category);
                    }
                }
                VoiceChannel created = action.complete();
                return String.format("Se ha creado exitosamente el canal de voz `%s` (ID: %s) en la categoría '%s'.",
                        created.getName(), created.getId(), created.getParentCategory() != null ? created.getParentCategory().getName() : "Ninguna");
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCloneChannelTool() {
        return new AITool() {
            @Override public String getName() { return "clone_channel"; }
            @Override public String getDescription() { return "Clona la estructura y permisos de un canal existente para crear uno nuevo."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("channelId", createStringProp("ID del canal existente que se va a clonar."));
                props.put("newName", createStringProp("Nombre opcional para el nuevo canal clonado."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("channelId")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String chId = (String) arguments.get("channelId");
                String newName = (String) arguments.get("newName");
                Guild guild = event.getGuild();
                GuildChannel existing = guild.getGuildChannelById(chId);
                if (existing == null) return "Error: No se encontró el canal con ID " + chId;

                if (existing instanceof net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel copyable) {
                    var action = copyable.createCopy();
                    if (newName != null && !newName.trim().isEmpty()) {
                        action = action.setName(newName);
                    }
                    GuildChannel cloned = (GuildChannel) action.complete();
                    return String.format("Se ha clonado exitosamente el canal '%s' como uno nuevo llamado '%s' (ID: %s).",
                            existing.getName(), cloned.getName(), cloned.getId());
                } else {
                    return "Error: Este tipo de canal no es compatible para clonación.";
                }
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getDeleteChannelTool() {
        return new AITool() {
            @Override public String getName() { return "delete_channel"; }
            @Override public String getDescription() { return "Elimina permanentemente un canal del servidor de Discord."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("channelId", createStringProp("ID del canal de texto o voz a eliminar."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("channelId")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String chId = (String) arguments.get("channelId");
                Guild guild = event.getGuild();
                GuildChannel channel = guild.getGuildChannelById(chId);
                if (channel == null) return "Error: No se encontró el canal con ID " + chId;

                String name = channel.getName();
                channel.delete().complete();
                return String.format("El canal '%s' (ID: %s) ha sido eliminado permanentemente del servidor.", name, chId);
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getAddRoleToUserTool() {
        return new AITool() {
            @Override public String getName() { return "add_role_to_user"; }
            @Override public String getDescription() { return "Asigna un rol de Discord existente a un miembro del servidor."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("userId", createStringProp("ID de Discord del usuario."));
                props.put("roleId", createStringProp("ID de Discord del rol a asignar."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Arrays.asList("userId", "roleId")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_ROLES); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String uId = (String) arguments.get("userId");
                String rId = (String) arguments.get("roleId");
                Guild guild = event.getGuild();
                var member = guild.getMemberById(uId);
                if (member == null) return "Error: No se encontró el miembro del servidor con ID " + uId;
                Role role = guild.getRoleById(rId);
                if (role == null) return "Error: No se encontró el rol con ID " + rId;

                guild.addRoleToMember(member, role).complete();
                return String.format("Se ha asignado correctamente el rol '%s' al miembro '%s'.", role.getName(), member.getEffectiveName());
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getRemoveRoleFromUserTool() {
        return new AITool() {
            @Override public String getName() { return "remove_role_from_user"; }
            @Override public String getDescription() { return "Quita un rol de Discord a un miembro del servidor."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("userId", createStringProp("ID de Discord del usuario."));
                props.put("roleId", createStringProp("ID de Discord del rol a remover."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Arrays.asList("userId", "roleId")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_ROLES); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String uId = (String) arguments.get("userId");
                String rId = (String) arguments.get("roleId");
                Guild guild = event.getGuild();
                var member = guild.getMemberById(uId);
                if (member == null) return "Error: No se encontró el miembro del servidor con ID " + uId;
                Role role = guild.getRoleById(rId);
                if (role == null) return "Error: No se encontró el rol con ID " + rId;

                guild.removeRoleFromMember(member, role).complete();
                return String.format("Se ha removido correctamente el rol '%s' al miembro '%s'.", role.getName(), member.getEffectiveName());
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCreateRoleTool() {
        return new AITool() {
            @Override public String getName() { return "create_role"; }
            @Override public String getDescription() { return "Crea un nuevo rol en el servidor con un nombre y color hexadecimal opcional."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("name", createStringProp("Nombre del nuevo rol."));
                props.put("colorHex", createStringProp("Color hexadecimal del rol (ej: #FF5733)."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("name")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_ROLES); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String colorHex = (String) arguments.get("colorHex");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";

                var action = guild.createRole().setName(name);
                if (colorHex != null && !colorHex.trim().isEmpty()) {
                    try {
                        Color color = Color.decode(colorHex);
                        action = action.setColor(color);
                    } catch (NumberFormatException e) {
                        return "Error: Formato de color hexadecimal inválido (ejemplo correcto: #FF0000).";
                    }
                }
                Role created = action.complete();
                return String.format("Se ha creado exitosamente el rol '%s' (ID: %s) con el color '%s'.",
                        created.getName(), created.getId(), colorHex != null ? colorHex : "Por defecto");
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getSetServerPrefixTool() {
        return new AITool() {
            @Override public String getName() { return "set_server_prefix"; }
            @Override public String getDescription() { return "Configura el prefijo de comandos del bot para este servidor de Discord."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("prefix", createStringProp("El nuevo prefijo de texto (máximo 10 caracteres, o '@mention')."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("prefix")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.ADMINISTRATOR); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String newPrefix = (String) arguments.get("prefix");
                if (newPrefix == null || newPrefix.trim().isEmpty()) return "Error: Falta el parámetro 'prefix'.";
                if (newPrefix.length() > 10) return "Error: El prefijo no puede tener más de 10 caracteres.";

                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                server.setPrefix(newPrefix);
                server.save();

                return String.format("El prefijo de comandos para este servidor se ha cambiado a: `%s`.", newPrefix);
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getToggleAntiRaidTool() {
        return new AITool() {
            @Override public String getName() { return "toggle_anti_raid"; }
            @Override public String getDescription() { return "Activa o desactiva el modo anti-raid del bot para proteger el servidor de intrusos."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("enable", createBooleanProp("Establecer a true para activar el modo anti-raid, o false para desactivarlo."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("enable")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.ADMINISTRATOR); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                Boolean enable = (Boolean) arguments.get("enable");
                if (enable == null) return "Error: Falta el parámetro obligatorio 'enable'.";

                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                server.setAntiRaidMode(enable);
                server.save();

                return String.format("El modo Anti-Raid se ha %s correctamente para este servidor.", enable ? "ACTIVADO (Seguridad Máxima)" : "DESACTIVADO");
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getConfigureWelcomeGoodbyeTool() {
        return new AITool() {
            @Override public String getName() { return "configure_welcome_goodbye"; }
            @Override public String getDescription() { return "Configura el canal y mensaje de bienvenida o despedida del servidor."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                Map<String, Object> typeProp = new HashMap<>();
                typeProp.put("type", "string");
                typeProp.put("enum", Arrays.asList("welcome", "goodbye"));
                typeProp.put("description", "Determina si configuras las bienvenidas o despedidas.");
                props.put("type", typeProp);
                props.put("channelId", createStringProp("ID del canal de Discord para enviar el mensaje."));
                props.put("message", createStringProp("Mensaje personalizado (puedes usar @username y @servername)."));
                props.put("enabled", createBooleanProp("Habilitar o deshabilitar este tipo de mensaje."));

                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Arrays.asList("type", "channelId", "message", "enabled")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.ADMINISTRATOR); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String type = (String) arguments.get("type");
                String channelIdStr = (String) arguments.get("channelId");
                String msg = (String) arguments.get("message");
                Boolean enabled = (Boolean) arguments.get("enabled");

                long channelId;
                try {
                    channelId = Long.parseLong(channelIdStr);
                } catch (NumberFormatException e) {
                    return "Error: Formato de ID de canal inválido.";
                }

                TextChannel channel = event.getGuild().getTextChannelById(channelId);
                if (channel == null) return "Error: No se encontró el canal de texto con ID " + channelIdStr;

                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                if ("welcome".equalsIgnoreCase(type)) {
                    server.setBienvenidasChannelEnabled(enabled);
                    server.setBienvenidasChannelId(channelId);
                    server.setBienvenidasChannelMessage(msg);
                } else if ("goodbye".equalsIgnoreCase(type)) {
                    server.setDespedidasChannelEnabled(enabled);
                    server.setDespedidasChannelId(channelId);
                    server.setDespedidasChannelMessage(msg);
                } else {
                    return "Error: El tipo debe ser 'welcome' o 'goodbye'.";
                }

                server.save();
                return String.format("Se ha configurado correctamente el sistema de %s:\n- Estado: %s\n- Canal: #%s\n- Mensaje: '%s'",
                        "welcome".equalsIgnoreCase(type) ? "Bienvenidas" : "Despedidas",
                        enabled ? "Habilitado" : "Deshabilitado",
                        channel.getName(), msg);
            }
        };
    }
    @Produces
    @ApplicationScoped
    public AITool getCreateRoleSelectorTool() {
        return new AITool() {
            @Override public String getName() { return "create_role_selector"; }
            @Override public String getDescription() { return "Crea un panel/mensaje con un menú desplegable (select menu) en un canal para que los usuarios puedan seleccionarse sus propios roles de forma interactiva."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("channelId", createStringProp("El ID del canal de texto de Discord donde se enviará el selector de roles."));
                props.put("message", createStringProp("El mensaje o descripción del panel que se enviará en el embed."));
                
                Map<String, Object> roleIdsProp = new HashMap<>();
                roleIdsProp.put("type", "array");
                Map<String, Object> itemsProp = new HashMap<>();
                itemsProp.put("type", "string");
                itemsProp.put("description", "El ID de Discord de un rol.");
                roleIdsProp.put("items", itemsProp);
                roleIdsProp.put("description", "Lista de IDs de roles que se incluirán en el menú selector.");
                props.put("roleIds", roleIdsProp);
                
                props.put("exclusive", createBooleanProp("Opcional: Si es true, el usuario solo podrá elegir un rol a la vez. Si es false (por defecto), podrá elegir múltiples."));

                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Arrays.asList("channelId", "message", "roleIds")).build()).build()).build();
            }
            @Override public String getRequiredMode() { return "ADMIN"; }
            @Override public List<Permission> getRequiredUserPermissions() { return Arrays.asList(Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String channelIdStr = (String) arguments.get("channelId");
                String message = (String) arguments.get("message");
                Boolean exclusive = (Boolean) arguments.get("exclusive");
                if (exclusive == null) exclusive = false;
                
                Object roleIdsObj = arguments.get("roleIds");
                List<String> roleIds = new ArrayList<>();
                if (roleIdsObj instanceof List<?> list) {
                    for (Object o : list) {
                        if (o != null) {
                            roleIds.add(o.toString());
                        }
                    }
                }
                
                if (channelIdStr == null || channelIdStr.trim().isEmpty()) return "Error: Falta el parámetro 'channelId'.";
                if (message == null || message.trim().isEmpty()) return "Error: Falta el parámetro 'message'.";
                if (roleIds.isEmpty()) return "Error: Debe proporcionar al menos un ID de rol en 'roleIds'.";
                
                Guild guild = event.getGuild();
                TextChannel textChannel = guild.getTextChannelById(channelIdStr);
                if (textChannel == null) return "Error: No se encontró el canal de texto con ID " + channelIdStr;
                
                List<Role> roles = new ArrayList<>();
                for (String rId : roleIds) {
                    Role role = guild.getRoleById(rId);
                    if (role == null) {
                        return "Error: No se encontró el rol con ID " + rId + " en este servidor.";
                    }
                    roles.add(role);
                }
                
                RoleManager manager = new RoleManager();
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription(message);
                eb.setColor(guild.getSelfMember().getColor());
                
                StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("roleselect")
                    .setPlaceholder("Selecciona tus roles...")
                    .setMinValues(0)
                    .setMaxValues(exclusive ? 1 : roles.size());
                    
                HashMap<String, String> map = new HashMap<>();
                for (Role role : roles) {
                    if (!guild.getSelfMember().canInteract(role)) {
                        return "Error: El rol " + role.getName() + " (ID: " + role.getId() + ") tiene mayor jerarquía que el rol del bot. Coloca el rol del bot más arriba en la lista de roles del servidor para que pueda asignarlo.";
                    }
                    menuBuilder.addOption(role.getName(), role.getId());
                    map.put(role.getId(), role.getAsMention());
                }
                
                StringSelectMenu menu = menuBuilder.build();
                
                try {
                    var successMessage = textChannel.sendMessageEmbeds(eb.build()).setComponents(ActionRow.of(menu)).complete();
                    manager.setId(successMessage.getIdLong());
                    manager.setEmoji(map);
                    manager.setToggled(exclusive);
                    
                    bot.getSettingsManager().addRoleManagerToServer(guild.getIdLong(), manager);
                    
                    return String.format("Se ha creado exitosamente el selector de roles en el canal #%s (Mensaje ID: %s).\nRoles incluidos: %s\nModo exclusivo: %s",
                        textChannel.getName(), successMessage.getId(), 
                        String.join(", ", roles.stream().map(Role::getName).toList()), 
                        exclusive ? "Sí" : "No");
                } catch (Exception e) {
                    return "Error al enviar el mensaje o guardar el selector: " + e.getMessage();
                }
            }
        };
    }
}
