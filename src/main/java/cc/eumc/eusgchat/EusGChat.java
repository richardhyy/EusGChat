package cc.eumc.eusgchat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class EusGChat extends Plugin implements Listener {
    String CHAT_FORMAT_SERVER_INDICATOR;
    String CHAT_FORMAT_MESSAGE;
    List<String> BYPASS_SERVERS = new ArrayList<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");


        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            CHAT_FORMAT_SERVER_INDICATOR = config.getString("Settings.ChatFormat.ServerIndicator", "&7{server} ").replace("&", "§");
            CHAT_FORMAT_MESSAGE = "§r" + config.getString("Settings.ChatFormat.Message", "{player} &f>&7>&8> &r{message}").replace("&", "§");

            BYPASS_SERVERS = config.getStringList("Settings.BypassServer");

            getLogger().info("Bypass servers: " + BYPASS_SERVERS);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (e.isCancelled() || e.isCommand() || e.isProxyCommand()) return;

        ProxiedPlayer player = (ProxiedPlayer) e.getSender();

        if (BYPASS_SERVERS.contains(player.getServer().getInfo().getName())) {
            return;
        }

        TextComponent base = new TextComponent();

        TextComponent prefix = new TextComponent(CHAT_FORMAT_SERVER_INDICATOR.replace("{server}", String.valueOf(player.getServer().getInfo().getName().charAt(0)).toUpperCase()));
        prefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(player.getServer().getInfo().getName()).create()));
        base.addExtra(prefix);

        base.addExtra(new TextComponent(CHAT_FORMAT_MESSAGE.replace("{player}", player.getDisplayName()).replace("{message}", e.getMessage())));

        getProxy().broadcast(base);
        e.setCancelled(true);
    }
}
