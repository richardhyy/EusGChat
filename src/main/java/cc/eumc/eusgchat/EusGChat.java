package cc.eumc.eusgchat;

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

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class EusGChat extends Plugin implements Listener {
    Configuration config;

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

            try (InputStream in = getResourceAsStream("webhook_example.php")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            new PluginConfig(this);

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

        if (PluginConfig.BYPASS_SERVERS.contains(player.getServer().getInfo().getName())) {
            return;
        }

        TextComponent base = new TextComponent();

        TextComponent prefix = new TextComponent(PluginConfig.CHAT_FORMAT_SERVER_INDICATOR.replace("{server}", String.valueOf(player.getServer().getInfo().getName().charAt(0)).toUpperCase()));
        prefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(player.getServer().getInfo().getName()).create()));
        base.addExtra(prefix);

        base.addExtra(new TextComponent(PluginConfig.CHAT_FORMAT_MESSAGE.replace("{player}", player.getDisplayName()).replace("{message}", e.getMessage())));

        if (PluginConfig.GlobalGhostMode) {
            for (ProxiedPlayer p : getProxy().getPlayers()) {
                p.sendMessage(base);
            }
        }
        else {
            getProxy().broadcast(base);
        }

        if (PluginConfig.Sending_Enabled) {
            getProxy().getScheduler().schedule(this, () -> {
                for (String webhookURL : PluginConfig.Sending_Webhooks) {
                    sendMessageToWebhook(webhookURL, player.getName(), e.getMessage());
                }
            }, 1, TimeUnit.MILLISECONDS);
        }

        e.setCancelled(true);
    }

    public Configuration getConfig() {
        return config;
    }

    void sendMessageToWebhook(String webhookURL, String playerName, String message) {
        try {
            String params = "";
            params = "password=" + URLEncoder.encode(PluginConfig.Sending_ConnectionPassword, StandardCharsets.UTF_8.toString());
            params += "&playername=" + URLEncoder.encode(playerName, StandardCharsets.UTF_8.toString());
            params += "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
            httpPost(webhookURL, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String httpPost(String url, String urlParameters) throws Exception {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
