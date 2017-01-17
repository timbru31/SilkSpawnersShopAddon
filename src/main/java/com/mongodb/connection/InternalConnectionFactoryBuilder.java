package com.mongodb.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class InternalConnectionFactoryBuilder {
    private static final int TIMEOUT = 5000;
    private static final int SERVER_ERROR = 500;
    private SilkSpawnersShopAddon plugin;
    private JsonParser parser = new JsonParser();

    public InternalConnectionFactoryBuilder(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    public int buildInternalConnection(String userId) throws BaseQueryFactory {
        return buildInternalConnection(userId, "https://api.dustplanet.de/", true);
    }

    // HTTP POST request
    public int buildInternalConnection(String userId, String apiHost, boolean useSSL) throws BaseQueryFactory {
        // URL
        URL url = null;
        try {
            url = new URL(apiHost);
        } catch (MalformedURLException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (1)");
            return -1;
        }

        // HTTPS Connection
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();

        } catch (IOException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (2)");
            return -1;
        }

        // Get user id
        String rawData = "user_id=";
        String encodedData = null;
        try {
            encodedData = rawData + URLEncoder.encode(userId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (3)");
            return -1;
        }

        // Make POST request
        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (4)");
            return -1;
        }
        con.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Bukkit-Server-Port", String.valueOf(plugin.getServer().getPort()));
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        // Send POST request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(encodedData.getBytes("UTF-8"));
            wr.flush();
            wr.close();
        } catch (UnknownHostException e) {
            // Handle being offline nice
            return -1;
        } catch (IOException e) {
            if (useSSL) {
                return buildInternalConnection(userId, "http://api.dustplanet.de/", false);
            }
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (5)");
            return -1;
        }

        // Get response
        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            // Handle case when Dustplanet is down gracefully.
            return responseCode;
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        // Ignore all server errors
        if (responseCode >= SERVER_ERROR) {
            return responseCode;
        } else if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (6)");
                return responseCode;
            }
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (7)");
                return responseCode;
            }
        }
        boolean blacklisted = true;
        try {
            JsonElement parse = parser.parse(response.toString());
            blacklisted = parse.getAsJsonObject().get("blacklisted").getAsBoolean();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (8)");
            return responseCode;
        }
        if (blacklisted) {
            disableDueToError("You are blacklisted...");
        }
        return responseCode;
    }

    private void disableDueToError(String... messages) throws BaseQueryFactory {
        for (String message : messages) {
            plugin.getLogger().severe(message);
        }
        throw new BaseQueryFactory();
    }
}
