package de.cyklon.tibbermanager.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TibberClient {

    private static final String API_ENDPOINT = "https://api.tibber.com/v1-beta/gql";

    private final Logger logger = LoggerFactory.getLogger(TibberClient.class);
    private final String token;
    private final OkHttpClient client;

    public TibberClient(String token) throws IOException {
        this.token = token;
        this.client = new OkHttpClient();
    }

    public int sendPushNotification(String tile, String message, AppScreen screenToOpen) throws IOException {
        JsonObject response = sendMutation("""
                sendPushNotification(input: {
                    title: "%s",
                    message: "%s",
                    screenToOpen: %s
                  }) {
                    successful
                    pushedToNumberOfDevices
                  }
                """.formatted(tile, message, screenToOpen)).getAsJsonObject("data").getAsJsonObject("sendPushNotification");

        if (!response.get("successful").getAsBoolean()) return -1;
        return response.get("pushedToNumberOfDevices").getAsInt();
    }

    public JsonObject sendMutation(String mutation) throws IOException {
        return jsonResponse(sendRequest(postRequest(query("""
                mutation {
                  %s
        }
        """.formatted(mutation)))));
    }

    public JsonObject sendQuery(String query) throws IOException {
        return jsonResponse(sendRequest(postRequest(query("{\nviewer {\n%s\n}\n}".formatted(query)))));
    }

    private Response sendRequest(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    public Logger getLogger() {
        return logger;
    }

    private Request postRequest(String json) {
        return builder()
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
            .build();
    }

    private Request.Builder builder() {
        return new Request.Builder()
            .url(API_ENDPOINT)
            .addHeader("Authorization", "Bearer " + token);
    }

    public static String query(String query) {
        JsonObject json = new JsonObject();
        json.addProperty("query", query);
        return json.toString();
    }

    private static JsonObject jsonResponse(Response response) throws IOException {
        try (response) {
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

}
