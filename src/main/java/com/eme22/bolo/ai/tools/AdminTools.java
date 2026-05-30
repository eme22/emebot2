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
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
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

    private Map<String, Object> createIntegerProp(String desc) {
        Map<String, Object> p = new HashMap<>();
        p.put("type", "integer");
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
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String catId = (String) arguments.get("categoryId");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";

                var action = guild.createTextChannel(name);
                if (catId != null && !catId.trim().isEmpty()) {
                    Category category = getCategoryByIdOrName(guild, catId);
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
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String catId = (String) arguments.get("categoryId");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";

                var action = guild.createVoiceChannel(name);
                if (catId != null && !catId.trim().isEmpty()) {
                    Category category = getCategoryByIdOrName(guild, catId);
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
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String chId = (String) arguments.get("channelId");
                String newName = (String) arguments.get("newName");
                Guild guild = event.getGuild();
                GuildChannel existing = getChannelByIdOrName(guild, chId);
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
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String chId = (String) arguments.get("channelId");
                Guild guild = event.getGuild();
                GuildChannel channel = getChannelByIdOrName(guild, chId);
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
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_ROLES); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String uId = (String) arguments.get("userId");
                String rId = (String) arguments.get("roleId");
                Guild guild = event.getGuild();
                var member = getMemberByIdOrName(guild, uId);
                if (member == null) return "Error: No se encontró el miembro del servidor con ID " + uId;
                Role role = getRoleByIdOrName(guild, rId);
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
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_ROLES); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String uId = (String) arguments.get("userId");
                String rId = (String) arguments.get("roleId");
                Guild guild = event.getGuild();
                var member = getMemberByIdOrName(guild, uId);
                if (member == null) return "Error: No se encontró el miembro del servidor con ID " + uId;
                Role role = getRoleByIdOrName(guild, rId);
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
                GuildChannel gc = getChannelByIdOrName(guild, channelIdStr);
                if (!(gc instanceof TextChannel textChannel)) return "Error: No se encontró el canal de texto con ID/nombre " + channelIdStr;
                
                List<Role> roles = new ArrayList<>();
                for (String rId : roleIds) {
                    Role role = getRoleByIdOrName(guild, rId);
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

    @Produces
    @ApplicationScoped
    public AITool getEditChannelTool() {
        return new AITool() {
            @Override public String getName() { return "edit_channel"; }
            @Override public String getDescription() { return "Edita un canal del servidor: renombrar, cambiar tema, modo NSFW, slowmode."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("channelId", createStringProp("ID del canal a editar."));
                props.put("name", createStringProp("Nuevo nombre para el canal (opcional)."));
                props.put("topic", createStringProp("Nuevo tema/descripción del canal (solo canales de texto/anuncios, opcional)."));
                props.put("nsfw", createBooleanProp("Marcar como NSFW (solo canales de texto/anuncios, opcional)."));
                props.put("slowmode", createIntegerProp("Slowmode en segundos (0-21600, solo canales de texto/anuncios, opcional)."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("channelId")).build()).build()).build();
            }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String channelId = (String) arguments.get("channelId");
                String name = (String) arguments.get("name");
                String topic = (String) arguments.get("topic");
                Boolean nsfw = (Boolean) arguments.get("nsfw");
                Integer slowmode = arguments.get("slowmode") != null ? ((Number) arguments.get("slowmode")).intValue() : null;
                Guild guild = event.getGuild();
                GuildChannel channel = getChannelByIdOrName(guild, channelId);
                if (channel == null) return "Error: No se encontró el canal con ID " + channelId + ".";
                List<String> changes = new ArrayList<>();
                if (name != null && !name.trim().isEmpty()) {
                    channel.getManager().setName(name).complete();
                    changes.add("nombre cambiado a '" + name + "'");
                }
                boolean isTextLike = channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.NEWS;
                if (isTextLike) {
                    if (channel instanceof TextChannel tc) {
                        var m = tc.getManager();
                        if (topic != null) { m.setTopic(topic); changes.add("tema actualizado"); }
                        if (nsfw != null) { m.setNSFW(nsfw); changes.add("NSFW " + (nsfw ? "activado" : "desactivado")); }
                        if (slowmode != null) {
                            if (slowmode < 0 || slowmode > 21600) return "Error: El slowmode debe estar entre 0 y 21600 segundos.";
                            m.setSlowmode(slowmode); changes.add("slowmode cambiado a " + slowmode + "s");
                        }
                        if (topic != null || nsfw != null || slowmode != null) m.complete();
                    } else if (channel instanceof NewsChannel nc) {
                        if (slowmode != null) return "Error: Los canales de anuncios no soportan slowmode.";
                        var m = nc.getManager();
                        if (topic != null) { m.setTopic(topic); changes.add("tema actualizado"); }
                        if (nsfw != null) { m.setNSFW(nsfw); changes.add("NSFW " + (nsfw ? "activado" : "desactivado")); }
                        if (topic != null || nsfw != null) m.complete();
                    }
                } else {
                    if (topic != null) return "Error: Este tipo de canal no soporta tema/descripción.";
                    if (nsfw != null) return "Error: Este tipo de canal no soporta modo NSFW.";
                    if (slowmode != null) return "Error: Este tipo de canal no soporta slowmode.";
                }
                if (changes.isEmpty()) return "No se realizó ningún cambio. Proporciona al menos un parámetro para modificar.";
                return "Canal '" + channel.getName() + "' editado correctamente. Cambios: " + String.join(", ", changes) + ".";
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCreateCategoryTool() {
        return new AITool() {
            @Override public String getName() { return "create_category"; }
            @Override public String getDescription() { return "Crea una nueva categoría en el servidor."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("name", createStringProp("Nombre de la nueva categoría."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("name")).build()).build()).build();
            }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";
                Category created = guild.createCategory(name).complete();
                return String.format("Se ha creado exitosamente la categoría '%s' (ID: %s).", created.getName(), created.getId());
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCreateAnnouncementChannelTool() {
        return new AITool() {
            @Override public String getName() { return "create_announcement_channel"; }
            @Override public String getDescription() { return "Crea un nuevo canal de anuncios en el servidor dentro de una categoría opcional."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("name", createStringProp("Nombre del nuevo canal de anuncios."));
                props.put("categoryId", createStringProp("ID opcional de la categoría donde colocar el canal."));
                props.put("topic", createStringProp("Tema/descripción opcional del canal."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("name")).build()).build()).build();
            }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String catId = (String) arguments.get("categoryId");
                String topic = (String) arguments.get("topic");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";
                var action = guild.createNewsChannel(name);
                if (catId != null && !catId.trim().isEmpty()) {
                    Category category = getCategoryByIdOrName(guild, catId);
                    if (category != null) action = action.setParent(category);
                }
                if (topic != null && !topic.trim().isEmpty()) {
                    action = action.setTopic(topic);
                }
                NewsChannel created = action.complete();
                return String.format("Se ha creado exitosamente el canal de anuncios `#%s` (ID: %s) en la categoría '%s'.",
                        created.getName(), created.getId(), created.getParentCategory() != null ? created.getParentCategory().getName() : "Ninguna");
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getCreateStageChannelTool() {
        return new AITool() {
            @Override public String getName() { return "create_stage_channel"; }
            @Override public String getDescription() { return "Crea un nuevo canal de escenario en el servidor dentro de una categoría opcional."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("name", createStringProp("Nombre del nuevo canal de escenario."));
                props.put("categoryId", createStringProp("ID opcional de la categoría donde colocar el canal."));
                props.put("topic", createStringProp("Tema/descripción opcional del canal."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("name")).build()).build()).build();
            }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String name = (String) arguments.get("name");
                String catId = (String) arguments.get("categoryId");
                String topic = (String) arguments.get("topic");
                Guild guild = event.getGuild();
                if (name == null || name.trim().isEmpty()) return "Error: Falta el parámetro 'name'.";
                var action = guild.createStageChannel(name);
                if (catId != null && !catId.trim().isEmpty()) {
                    Category category = getCategoryByIdOrName(guild, catId);
                    if (category != null) action = action.setParent(category);
                }
                if (topic != null && !topic.trim().isEmpty()) {
                    action = action.setTopic(topic);
                }
                StageChannel created = action.complete();
                return String.format("Se ha creado exitosamente el canal de escenario `%s` (ID: %s) en la categoría '%s'.",
                        created.getName(), created.getId(), created.getParentCategory() != null ? created.getParentCategory().getName() : "Ninguna");
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getMoveChannelTool() {
        return new AITool() {
            @Override public String getName() { return "move_channel"; }
            @Override public String getDescription() { return "Mueve un canal a una categoría específica o lo saca de su categoría actual."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("channelId", createStringProp("ID del canal a mover."));
                props.put("categoryId", createStringProp("ID de la categoría destino. Si está vacío, el canal se saca de su categoría actual."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Collections.singletonList("channelId")).build()).build()).build();
            }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String channelId = (String) arguments.get("channelId");
                String catId = (String) arguments.get("categoryId");
                Guild guild = event.getGuild();
                GuildChannel channel = getChannelByIdOrName(guild, channelId);
                if (channel == null) return "Error: No se encontró el canal con ID " + channelId + ".";
                if (!(channel instanceof StandardGuildChannel sgc)) {
                    return "Error: Este tipo de canal no se puede mover a una categoría.";
                }
                Category category = null;
                if (catId != null && !catId.trim().isEmpty()) {
                    category = getCategoryByIdOrName(guild, catId);
                    if (category == null) return "Error: No se encontró la categoría con ID " + catId + ".";
                }
                sgc.getManager().setParent(category).complete();
                String parentName = category != null ? "'" + category.getName() + "'" : "Ninguna (sin categoría)";
                return String.format("El canal '%s' (ID: %s) se ha movido exitosamente a la categoría %s.",
                        channel.getName(), channel.getId(), parentName);
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getSetChannelPermissionsTool() {
        return new AITool() {
            @Override public String getName() { return "set_channel_permissions"; }
            @Override public String getDescription() { return "Configura permisos de un rol o usuario en un canal específico del servidor."; }
            @Override public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> props = new HashMap<>();
                props.put("channelId", createStringProp("ID del canal donde se aplicarán los permisos."));
                Map<String, Object> targetTypeProp = new HashMap<>();
                targetTypeProp.put("type", "string");
                targetTypeProp.put("enum", Arrays.asList("role", "user"));
                targetTypeProp.put("description", "Tipo de destino: 'role' para un rol, 'user' para un miembro.");
                props.put("targetType", targetTypeProp);
                props.put("targetId", createStringProp("ID del rol o usuario (según targetType)."));
                props.put("allowPermissions", createStringProp("Permisos a conceder, separados por coma (ej: VIEW_CHANNEL, MESSAGE_SEND). Opcional."));
                props.put("denyPermissions", createStringProp("Permisos a denegar, separados por coma (ej: MESSAGE_WRITE). Opcional."));
                return OpenAIDTO.Tool.builder().type("function").function(OpenAIDTO.FunctionDefinition.builder().name(getName()).description(getDescription()).parameters(OpenAIDTO.ParametersDefinition.builder().type("object").properties(props).required(Arrays.asList("channelId", "targetType", "targetId")).build()).build()).build();
            }
            @Override public List<Permission> getRequiredUserPermissions() { return Collections.singletonList(Permission.MANAGE_CHANNEL); }
            @Override public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                String channelId = (String) arguments.get("channelId");
                String targetType = (String) arguments.get("targetType");
                String targetId = (String) arguments.get("targetId");
                String allowStr = (String) arguments.get("allowPermissions");
                String denyStr = (String) arguments.get("denyPermissions");
                Guild guild = event.getGuild();
                GuildChannel channel = getChannelByIdOrName(guild, channelId);
                if (channel == null) return "Error: No se encontró el canal con ID " + channelId + ".";
                IPermissionHolder holder;
                if ("role".equalsIgnoreCase(targetType)) {
                    Role role = getRoleByIdOrName(guild, targetId);
                    if (role == null) return "Error: No se encontró el rol con ID " + targetId + ".";
                    holder = role;
                } else if ("user".equalsIgnoreCase(targetType)) {
                    Member member = getMemberByIdOrName(guild, targetId);
                    if (member == null) return "Error: No se encontró el miembro con ID " + targetId + " en este servidor.";
                    holder = member;
                } else {
                    return "Error: targetType debe ser 'role' o 'user'.";
                }
                long allowRaw = parsePermissionString(allowStr);
                long denyRaw = parsePermissionString(denyStr);
                PermissionOverrideAction action = ((IPermissionContainer) channel).upsertPermissionOverride(holder);
                action.setAllowed(allowRaw).setDenied(denyRaw).complete();
                StringBuilder result = new StringBuilder();
                result.append("Permisos actualizados en el canal '").append(channel.getName()).append("' para ");
                result.append(holder instanceof Role ? "el rol" : "el miembro").append(" '").append(
                        holder instanceof Role r ? r.getName() : ((Member) holder).getEffectiveName()
                ).append("'.");
                if (allowRaw != 0) result.append("\nPermisos concedidos: ").append(allowStr);
                if (denyRaw != 0) result.append("\nPermisos denegados: ").append(denyStr);
                return result.toString();
            }

            private long parsePermissionString(String perms) {
                long bits = 0;
                if (perms == null || perms.trim().isEmpty()) return bits;
                for (String p : perms.split(",")) {
                    try {
                        Permission perm = Permission.valueOf(p.trim().toUpperCase());
                        bits |= perm.getRawValue();
                    } catch (IllegalArgumentException ignored) {}
                }
                return bits;
            }
        };
    }

    private GuildChannel getChannelByIdOrName(Guild guild, String idOrName) {
        if (idOrName == null || idOrName.trim().isEmpty()) {
            return null;
        }
        try {
            String sanitized = idOrName.replaceAll("\\D", "");
            if (!sanitized.isEmpty()) {
                GuildChannel channel = guild.getGuildChannelById(sanitized);
                if (channel != null) return channel;
            }
        } catch (Exception ignored) {}

        String cleanName = idOrName.replace("#", "").trim();
        for (GuildChannel channel : guild.getChannels()) {
            if (channel.getName().equalsIgnoreCase(idOrName) || channel.getName().equalsIgnoreCase(cleanName)) {
                return channel;
            }
        }
        return null;
    }

    private Category getCategoryByIdOrName(Guild guild, String idOrName) {
        if (idOrName == null || idOrName.trim().isEmpty()) {
            return null;
        }
        try {
            String sanitized = idOrName.replaceAll("\\D", "");
            if (!sanitized.isEmpty()) {
                Category category = guild.getCategoryById(sanitized);
                if (category != null) return category;
            }
        } catch (Exception ignored) {}

        for (Category category : guild.getCategories()) {
            if (category.getName().equalsIgnoreCase(idOrName)) {
                return category;
            }
        }
        return null;
    }

    private Role getRoleByIdOrName(Guild guild, String idOrName) {
        if (idOrName == null || idOrName.trim().isEmpty()) {
            return null;
        }
        try {
            String sanitized = idOrName.replaceAll("\\D", "");
            if (!sanitized.isEmpty()) {
                Role role = guild.getRoleById(sanitized);
                if (role != null) return role;
            }
        } catch (Exception ignored) {}

        String cleanName = idOrName.replace("@", "").trim();
        for (Role role : guild.getRoles()) {
            if (role.getName().equalsIgnoreCase(idOrName) || role.getName().equalsIgnoreCase(cleanName)) {
                return role;
            }
        }
        return null;
    }

    private Member getMemberByIdOrName(Guild guild, String idOrName) {
        if (idOrName == null || idOrName.trim().isEmpty()) {
            return null;
        }
        try {
            String sanitized = idOrName.replaceAll("\\D", "");
            if (!sanitized.isEmpty()) {
                Member member = guild.getMemberById(sanitized);
                if (member != null) return member;
            }
        } catch (Exception ignored) {}

        String cleanName = idOrName.replace("@", "").trim();
        for (Member member : guild.getMembers()) {
            if (member.getUser().getName().equalsIgnoreCase(idOrName) || 
                member.getEffectiveName().equalsIgnoreCase(idOrName) ||
                member.getUser().getAsMention().equals(idOrName) ||
                member.getEffectiveName().equalsIgnoreCase(cleanName) ||
                member.getUser().getName().equalsIgnoreCase(cleanName)) {
                return member;
            }
        }
        return null;
    }
}
