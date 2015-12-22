package de.dustplanet.silkspawnersshopaddon.piracy.checker;

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

import org.json.JSONException;
import org.json.JSONObject;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.piracy.SilkSpawnersShopAddonPiracyDetector;

public class SilkSpawnersShopAddonPiracyChecker {
    private SilkSpawnersShopAddon plugin;

    public SilkSpawnersShopAddonPiracyChecker(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    // HTTP POST request
    public void sendPost() throws BlackListedException {
        // URL
        URL url = null;
        try {
            url = new URL("http://api.dustplanet.de/");
        } catch (MalformedURLException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }

        // HTTP Connection
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }

        // Get user id
        String rawData = "user_id=";
        String userId = new SilkSpawnersShopAddonPiracyDetector().getUserID();
        String encodedData = null;
        try {
            encodedData = rawData + URLEncoder.encode(userId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }

        // Make POST request
        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }
        con.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Send POST request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(encodedData.getBytes("UTF-8"));
            wr.flush();
            wr.close();
        } catch (IOException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }

        // Get response
        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
                return;
            }
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"))) {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
                return;
            }
        }
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(response.toString());
        } catch (JSONException e) {
            disableDueToError("An error occured, disabling SilkSpawnersShopAddon");
            return;
        }
        boolean blacklisted = responseJSON.getBoolean("blacklisted");
        if (blacklisted) {
            disableDueToError("You are blacklisted...");
        }
    }

    private void disableDueToError(String... messages) throws BlackListedException {
        for (String message : messages) {
            plugin.getLogger().severe(message);
        }
        throw new BlackListedException();
    }
}
