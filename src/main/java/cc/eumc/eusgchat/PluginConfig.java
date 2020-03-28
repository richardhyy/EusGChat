package cc.eumc.eusgchat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PluginConfig {
    public static boolean Sending_Enabled;
    public static String Sending_ConnectionPassword;
    public static List<String> Sending_Webhooks;

    public static boolean GlobalGhostMode = false;
    public static String CHAT_FORMAT_SERVER_INDICATOR;
    public static String CHAT_FORMAT_MESSAGE;
    public static Pattern BYPASS_MSG_PATTERN;
    public static List<String> BYPASS_SERVERS = new ArrayList<>();

    public PluginConfig(EusGChat instance) {
        Sending_Enabled = instance.getConfig().getBoolean("Settings.ChatBridge.Enabled", false);
        if (Sending_Enabled) {
            Sending_ConnectionPassword = instance.getConfig().getString("Settings.ChatBridge.ConnectionPassword", "");
            Sending_Webhooks = instance.getConfig().getStringList("Settings.ChatBridge.Webhooks");
        }
        instance.getLogger().info("ChatBridge: " + (Sending_Enabled?"§aenabled":"disabled"));

        GlobalGhostMode = instance.getConfig().getBoolean("Settings.GlobalGhostMode", false);
        instance.getLogger().info("GlobalGhostMode: " + (GlobalGhostMode?"§aenabled":"disabled"));

        CHAT_FORMAT_SERVER_INDICATOR = instance.getConfig().getString("Settings.ChatFormat.ServerIndicator", "&7{server} ").replace("&", "§");
        CHAT_FORMAT_MESSAGE = "§r" + instance.getConfig().getString("Settings.ChatFormat.Message", "{player} &f>&7>&8> &r{message}").replace("&", "§");

        String regex = instance.getConfig().getString("Settings.BypassByRegex", "");
        if (!regex.equals("")) {
            BYPASS_MSG_PATTERN = Pattern.compile(regex);
            instance.getLogger().info("BypassByRegex: " + regex);
        }

        BYPASS_SERVERS = instance.getConfig().getStringList("Settings.BypassServer");
        instance.getLogger().info("Bypass servers: " + BYPASS_SERVERS);
    }
}
