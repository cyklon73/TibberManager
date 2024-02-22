package de.cyklon.tibbermanager;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cyklon.tibbermanager.api.TibberClient;

import java.io.FileReader;
import java.io.IOException;

public class Manager {

    public static void main(String[] args) throws IOException {
        new Manager();
    }

    public Manager() throws IOException {
        JsonObject json = JsonParser.parseReader(new FileReader("settings.json")).getAsJsonObject();
        //String token = json.get("accessToken").getAsString();
        String token = json.get("testToken").getAsString();
        TibberClient client = new TibberClient(token);
    }

}
