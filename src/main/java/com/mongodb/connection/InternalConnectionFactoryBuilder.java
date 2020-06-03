package com.mongodb.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * InternalConnectionFactoryBuilder.
 *
 * @author timbru31
 */
@SuppressFBWarnings("IMC_IMMATURE_CLASS_PRINTSTACKTRACE")
@SuppressWarnings({ "checkstyle:ClassDataAbstractionCoupling", "checkstyle:MultipleStringLiterals" })
public class InternalConnectionFactoryBuilder {
    private static final int TIMEOUT = 5000;
    private static final int SERVER_ERROR = 500;
    private final SilkSpawnersShopAddon plugin;

    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "IMC_IMMATURE_CLASS_NO_TOSTRING" })
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public InternalConnectionFactoryBuilder(final SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public int buildInternalConnection(final String userId) throws BaseQueryFactory {
        return buildInternalConnection(userId, "https://api.dustplanet.de/", true);
    }

    @SuppressFBWarnings({ "SSCU_SUSPICIOUS_SHADED_CLASS_USE", "URLCONNECTION_SSRF_FD", "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE",
            "CC_CYCLOMATIC_COMPLEXITY" })
    @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.DataflowAnomalyAnalysis", "PMD.CyclomaticComplexity", "PMD.AssignmentInOperand",
            "checkstyle:MissingJavadocMethod", "PMD.AvoidPrintStackTrace", "PMD.AvoidCatchingGenericException", "checkstyle:ReturnCount",
            "checkstyle:IllegalCatch", "checkstyle:SeparatorWrap", "PMD.NcssCount", "PMD.NPathComplexity", "checkstyle:JavaNCSS",
            "checkstyle:ExecutableStatementCount", "checkstyle:CyclomaticComplexity", "checkstyle:NPathComplexity" })
    public int buildInternalConnection(final String userId, final String apiHost, final boolean useSSL) throws BaseQueryFactory {
        URL url = null;
        try {
            url = new URL(apiHost);
        } catch (@SuppressWarnings("unused") final MalformedURLException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (1)");
            return -1;
        }

        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (@SuppressWarnings("unused") final IOException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (2)");
            return -1;
        }

        try {
            con.setRequestMethod("POST");
        } catch (@SuppressWarnings("unused") final ProtocolException e) {
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (3)");
            return -1;
        }

        final String serverPort = String.valueOf(plugin.getServer().getPort());
        final String data = createDataObject(userId, serverPort);
        setConnectionPropertiesAndData(con, serverPort, data);
        try (DataOutputStream outputStream = new DataOutputStream(con.getOutputStream())) {
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (@SuppressWarnings("unused") final UnknownHostException e) {
            return -1;
        } catch (@SuppressWarnings("unused") final IOException e) {
            if (useSSL) {
                return buildInternalConnection(userId, "http://api.dustplanet.de/", false);
            }
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (4)");
            return -1;
        }

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (@SuppressWarnings("unused") final IOException e) {
            return responseCode;
        }

        String inputLine;
        final StringBuilder response = new StringBuilder();
        if (responseCode >= SERVER_ERROR) {
            return responseCode;
        } else if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                while ((inputLine = inputReader.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (@SuppressWarnings("unused") final IOException e) {
                disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (5)");
                return responseCode;
            }
        } else {
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                while ((inputLine = inputReader.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (@SuppressWarnings("unused") final IOException e) {
                disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (6)");
                return responseCode;
            }
        }
        boolean blacklisted = true;
        try {
            final JsonElement parse = new JsonParser().parse(response.toString());
            blacklisted = parse.getAsJsonObject().get("blacklisted").getAsBoolean();
        } catch (@SuppressWarnings("unused") final NoClassDefFoundError e) {
            @SuppressWarnings("checkstyle:LineLength")
            final org.bukkit.craftbukkit.libs.com.google.gson.JsonElement parse = new org.bukkit.craftbukkit.libs.com.google.gson.JsonParser()
                    .parse(response.toString());
            blacklisted = parse.getAsJsonObject().get("blacklisted").getAsBoolean();
        } catch (final Exception e) {
            e.printStackTrace();
            disableDueToError("An error occurred, disabling SilkSpawnersShopAddon (7)");
            return responseCode;
        }
        if (blacklisted) {
            disableDueToError("You are blacklisted...");
        }
        return responseCode;
    }

    @SuppressWarnings("static-method")
    private void setConnectionPropertiesAndData(final HttpURLConnection con, final String serverPort, final String data) {
        con.setRequestProperty("Content-Length", String.valueOf(data.length()));
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Bukkit-Server-Port", serverPort);
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        con.setDoOutput(true);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private String createDataObject(final String userId, final String serverPort) {
        String data;
        try {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("user_id", userId);
            jsonObject.addProperty("port", serverPort);
            jsonObject.addProperty("plugin", plugin.getDescription().getFullName());
            data = jsonObject.toString();
        } catch (@SuppressWarnings("unused") final NoClassDefFoundError e) {
            @SuppressWarnings("checkstyle:LineLength")
            final org.bukkit.craftbukkit.libs.com.google.gson.JsonObject jsonObject = new org.bukkit.craftbukkit.libs.com.google.gson.JsonObject();
            jsonObject.addProperty("user_id", userId);
            jsonObject.addProperty("port", serverPort);
            jsonObject.addProperty("plugin", plugin.getDescription().getFullName());
            data = jsonObject.toString();
        }
        return data;
    }

    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    private void disableDueToError(final String... messages) throws BaseQueryFactory {
        for (final String message : messages) {
            plugin.getLogger().severe(message);
        }
        throw new BaseQueryFactory();
    }
}
