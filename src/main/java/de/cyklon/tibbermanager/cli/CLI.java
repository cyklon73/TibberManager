package de.cyklon.tibbermanager.cli;

import de.codeshelf.consoleui.elements.PromptableElementIF;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import de.cyklon.reflection.entities.ReflectClass;
import jline.console.completer.StringsCompleter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class CLI {


	/*private static final String titlePrimary = """
				  _______ _ _     _             \s
				 |__   __(_) |   | |             \s
				    | |   _| |__ | |__   ___ _ __\s
				    | |  | | '_ \\| '_ \\ / _ \\ '__|
				    | |  | | |_) | |_) |  __/ |  \s
				    |_|  |_|_.__/|_.__/ \\___|_|  \s
			""";*/


	public static final Title titlePrimary, titleSecondary, titleConfig;

	private static final String titleSe = """
			 _______ _ _     _              \s
			|__   __(_) |   | |             \s
			   | |   _| |__ | |__   ___ _ __\s
			   | |  | | '_ \\| '_ \\ / _ \\ '__|
			   | |  | | |_) | |_) |  __/ |  \s
			   |_|  |_|_.__/|_.__/ \\___|_|  \s
					""";

	static {
		Title tp = null, ts = null, tc = null;
		try {
			YamlConfiguration data = YamlConfiguration.loadConfiguration(() -> CLI.class.getClassLoader().getResourceAsStream("titles.yml"));
			tp = loadTitle(data, "titlePrimary");
			ts = loadTitle(data, "titleSecondary");
			tc = loadTitle(data, "titleConfig");
		} catch (IOException e) {
			log.error("failed loading titles", e);
			System.exit(1);
		}
		titlePrimary = tp;
		titleSecondary = ts;
		titleConfig = tc;
	}

	public static Title loadTitle(ConfigurationSection data, String name) {
		data = data.getConfigurationSection(name);
		if (data==null) return new Title("", "", 0);
		return new Title(data.getString("title"), ReflectClass.wrap(AnsiColor.class).getField(String.class, data.getString("color")).getStaticValue(), data.getInt("spacing"));
	}

	private static void printTitle() {
		System.out.print(titlePrimary);
		System.out.println(titleSecondary);

		System.out.println();
		System.out.println();
	}

	@Getter
	private final ConsolePrompt prompt;
	private final List<Command> commands = new ArrayList<>();
	private String helpMsg;
	private boolean running = true;

	public CLI() throws IOException {
		AnsiConsole.systemInstall();

		printTitle();

		this.prompt = new ConsolePrompt();

		ConfigHandler configHandler = new ConfigHandler(this);

		PromptBuilder builder = prompt.getPromptBuilder();

		builder.createListPrompt()
				.name("configsection")
				.message("Select a section")
				.newItem("apiEndpoint").text("API Endpoint").add()
				.newItem("accessToken").text("Access Token").add()
				.newItem("testToken").text("Test Token").add()
				.newItem("activeToken").text("Active Token").add()
				.newItem("priceThreshold").text("Price Threshold").add()
				.addPrompt();

		List<PromptableElementIF> configPrompt = builder.build();

		commands.add(Command.printCommand("help", "Displays all commands with their aliases and their description", () -> helpMsg, "h"));
		commands.add(Command.promptCommand("config", "Opens the Config menu", configPrompt, configHandler, true, () -> System.out.println(titleConfig)));
		commands.add(Command.consumerCommand("clear", "Clears the entire console", p -> clear(true), "cl"));
		commands.add(Command.consumerCommand("exit", "Shuts down all running processes and closes TibberManger", p -> shutdown()));

		helpMsg = "    " + pad(pad("Name", 15) + "Aliases", 50) + "Description\n\n"
				+ commands.stream()
				.map(c -> "    " + pad(pad(AnsiColor.CYAN_BOLD + c.getName(), 15) + ("    " + AnsiColor.YELLOW_BOLD + "[" + String.join(", ", c.getAliases()) + "]"), 50) + AnsiColor.WHITE_BOLD + c.getDescription())
				.collect(Collectors.joining("\n"));

		builder = prompt.getPromptBuilder();

		builder.createInputPrompt()
				.name("cmd")
				.message(AnsiColor.CYAN_BOLD + ">")
				.addCompleter(new StringsCompleter(commands.stream()
						.map(Command::getName)
						.toList()))
				.addPrompt();

		List<PromptableElementIF> mainPrompt = builder.build();

		while (running) {
			System.out.println();
			String in = Objects.requireNonNullElse(((InputResult)prompt.prompt(mainPrompt).get("cmd")).getInput(), "").strip();
			if (in.isBlank()) continue;
			List<Command> matching = commands.stream()
					.filter(c -> c.matches(in))
					.toList();
			if (matching.isEmpty()) System.out.println(AnsiColor.RED_BOLD + "unknown command '" + in + "'\n");
			else matching.forEach(c -> c.execute(this));
		}
	}

	private String pad(String s, int length ) {
		if (s.length()>=length) return s;
		StringBuilder sb = new StringBuilder(s);
		sb.append(" ".repeat(Math.max(0, (length - sb.length()))));
		return sb.toString();
	}

	private void shutdown() {
		System.out.println(AnsiColor.RED_BOLD + "Exiting TibberManager...");
		running = false;

		System.out.println(AnsiColor.RESET);
	}

	public void clear(boolean printTitle) {
		System.out.print("\033[2J\033[H");
		if (printTitle) printTitle();
	}

}
