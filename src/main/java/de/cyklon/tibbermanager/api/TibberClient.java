package de.cyklon.tibbermanager.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class TibberClient {

    private static final String API_ENDPOINT = "https://api.tibber.com/v1-beta/gql";

    private final String token;
    private final OkHttpClient client;

    public TibberClient(String token) {
        this.token = token;
        this.client = new OkHttpClient();
    }

    public int sendPushNotification(String tile, String message, AppScreen screenToOpen) throws IOException {
        String input = """
                  {
                    title: "%s",
                    message: "%s",
                    screenToOpen: %s
                  }
                """.formatted(tile, message, screenToOpen);
        log.debug("send push notification: " + input);
        JsonObject response = sendMutation("""
                sendPushNotification(input: %s) {
                    successful
                    pushedToNumberOfDevices
                  }
                """.formatted(input)).getAsJsonObject("data").getAsJsonObject("sendPushNotification");

        if (!response.get("successful").getAsBoolean()) return -1;
        return response.get("pushedToNumberOfDevices").getAsInt();
    }

    public JsonObject sendMutation(String mutation) throws IOException {
        log.debug("send Mutation: " + mutation);
        return jsonResponse(sendRequest(postRequest(query("""
                mutation {
                  %s
        }
        """.formatted(mutation)))));
    }

    public JsonObject sendQuery(String query) throws IOException {
        log.debug("send Query: " + query);
        return jsonResponse(sendRequest(postRequest(query("{\nviewer {\n%s\n}\n}".formatted(query)))));
    }

    private Response sendRequest(Request request) throws IOException {
        log.debug("send request " + request);
        return client.newCall(request).execute();
    }

    private Request postRequest(String json) {
        log.debug("post request from json: " + json);
        return builder()
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
            .build();
    }

    private Request.Builder builder() {
        log.debug("create RequestBuilder");
        return new Request.Builder()
            .url(API_ENDPOINT)
            .addHeader("Authorization", "Bearer " + token);
    }

    public static String query(String query) {
        log.debug("query: " + query);
        JsonObject json = new JsonObject();
        json.addProperty("query", query);
        return json.toString();
    }

    private static JsonObject jsonResponse(Response response) throws IOException {
        try (response) {
            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            log.debug("JsonResponse: " + json);
            return json;
        }
    }

}
