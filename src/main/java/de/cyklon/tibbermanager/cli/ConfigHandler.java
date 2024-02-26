package de.cyklon.tibbermanager.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.codeshelf.consoleui.elements.ExpandableChoice;
import de.codeshelf.consoleui.elements.InputValue;
import de.codeshelf.consoleui.elements.ListChoice;
import de.codeshelf.consoleui.prompt.*;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class ConfigHandler implements Consumer<Map<String, ? extends PromtResultItemIF>> {

	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final JsonObject defaultConfig = new JsonObject();

	static {
		defaultConfig.addProperty("apiEndpoint", "https://api.tibber.com/v1-beta/gql");
		defaultConfig.addProperty("accessToken", "");
		defaultConfig.addProperty("testToken", "5K4MVS-OjfWhK_4yrjOlFe1F6kJXPVf7eQYggo8ebAE");
		defaultConfig.addProperty("activeToken", "test");
		defaultConfig.addProperty("priceThreshold", 0.20);
	}

	public static JsonObject loadConfig() {
		try {
			return JsonParser.parseReader(new FileReader("settings.json")).getAsJsonObject();
		} catch (FileNotFoundException | IllegalStateException e) {
			log.error("failed to load config", e);
			return defaultConfig;
		}
	}

	private void writeConfig(String key, String value) {
		cli.clear(true);
		value = value.strip();
		if (checkInput(value)) return;
		JsonObject config = loadConfig();
		config.addProperty(key, value);
		try (FileWriter writer = new FileWriter("settings.json")) {
			gson.toJson(config, writer);
			writer.flush();
		} catch (IOException e) {
			log.error("failed to save config", e);
		}
	}

	private void writeConfig(String key, Number value) {
		JsonObject config = loadConfig();
		config.addProperty(key, value);
		cli.clear(true);
		try (FileWriter writer = new FileWriter("settings.json")) {
			gson.toJson(config, writer);
			writer.flush();
		} catch (IOException e) {
			log.error("failed to save config", e);
		}
	}

    @Getter
    private final List<ConfigField<?, ?>> configFields = new ArrayList<>();


	private final CLI cli;
    private final ConsolePrompt prompt;


    public ConfigHandler(CLI cli) {
		this.cli = cli;
        this.prompt = cli.getPrompt();
	    Supplier<PromptBuilder> builderSupplier = prompt::getPromptBuilder;
        configFields.add(new ConfigField<InputValue, InputResult>(builderSupplier, "apiEndpoint", "API Endpoint", InputValue.class, v -> writeConfig("apiEndpoint", v.getInput()), () -> loadConfig().get("apiEndpoint").getAsString()));
        configFields.add(new ConfigField<InputValue, InputResult>(builderSupplier, "accessToken", "Access Token", InputValue.class, v -> writeConfig("accessToken", v.getInput()), () -> loadConfig().get("accessToken").getAsString()));
        configFields.add(new ConfigField<InputValue, InputResult>(builderSupplier, "testToken", "Test Token", InputValue.class, v -> writeConfig("testToken", v.getInput()), () -> loadConfig().get("testToken").getAsString()));
		configFields.add(new ConfigField<ListChoice, ListResult>(builderSupplier, "activeToken", "Active Token", ListChoice.class, v -> writeConfig("activeToken", v.getSelectedId()), () -> loadConfig().get("activeToken").getAsString().equals("test") ? "Test Token" : "Access Token", () -> "access", () -> "Access Token", () -> "test", () -> "Test Token"));
		configFields.add(new ConfigField<InputValue, InputResult>(builderSupplier, "priceThreshold", "Price Threshold", InputValue.class, v -> {
			if (checkInput(v.getInput())) {
				cli.clear(true);
				return;
			}
			try {
				writeConfig("priceThreshold", Float.parseFloat(v.getInput()));
			} catch (NumberFormatException e) {
				cli.clear(true);
				log.error("Invalid Input", e);
			}
		}, () -> loadConfig().get("priceThreshold").getAsString()));
    }

	private boolean checkInput(String in) {
		return in.equals("-");
	}

    @Override
    public void accept(Map<String, ? extends PromtResultItemIF> map) {
        ListResult result = (ListResult) map.get("configsection");
        ConfigField<?, ?> field = configFields.stream()
                .filter(f -> f.getId().equals(result.getSelectedId()))
                .findFirst()
                .orElse(null);
        if (field==null) return;
	    try {
			cli.clear(false);
		    System.out.println(CLI.titleConfig);
		    field.prompt(prompt);
	    } catch (IOException e) {
		    log.error("exception in config", e);
	    }
    }
}
