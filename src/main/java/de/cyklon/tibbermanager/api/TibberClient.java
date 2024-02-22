package de.cyklon.tibbermanager.api;

import com.google.gson.JsonObject;
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

        String query = """
                homes {
                    subscriptions {
                        priceInfo {
                            current {
                                total
                                energy
                                tax
                                startsAt
                                currency
                                level
                            }
                        }
                    }
                }
                """;



        logger.info(sendQuery(query));
    }

    public String sendQuery(String query) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("query", "{\nviewer {\n%s\n}\n}".formatted(query));

        Request request = new Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public Logger getLogger() {
        return logger;
    }

}
