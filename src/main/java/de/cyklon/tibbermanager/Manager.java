package de.cyklon.tibbermanager;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cyklon.tibbermanager.api.AppScreen;
import de.cyklon.tibbermanager.api.TibberClient;
import de.cyklon.tibbermanager.cli.CLI;
import de.cyklon.tibbermanager.cli.ConfigHandler;

import java.io.*;

public class Manager {

    public static void main(String[] args) throws IOException {
        new Manager();
        new CLI();
    }

    public Manager() throws IOException {
        File file = new File("settings.json");

        boolean writeConfig = false;
        if (!file.exists()) writeConfig = true;
        else {
            try (FileInputStream fis = new FileInputStream(file)) {
                if (new String(fis.readAllBytes()).isBlank()) writeConfig = true;
            }
        }
        if (writeConfig) {
            FileWriter writer = new FileWriter(file);
            ConfigHandler.gson.toJson(ConfigHandler.defaultConfig, writer);
            writer.flush();
            writer.close();
        }
        //JsonObject json = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        //String token = json.get("accessToken").getAsString();
        //String token = json.get("testToken").getAsString();
        /*TibberClient client = new TibberClient(token);
        System.out.println(client.sendPushNotification("Test", "ist nur ein Test", AppScreen.NOTIFICATIONS));*/
        //https://api.corrently.io/v2.0/gsi/marketdata?zip=[POSTLEITZAHL]
    }

}
