package de.cyklon.tibbermanager.cli;

import de.codeshelf.consoleui.elements.PromptableElementIF;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Getter
public class Command {

	public static Command placeholder(String name, String description, String... aliases) {
		return new Command(name, description, p -> System.out.println(name + " executed!"), aliases);
	}

	public static Command promptCommand(String name, String description, List<PromptableElementIF> prompt, Consumer<Map<String, ? extends PromtResultItemIF>> handle, boolean clear, Runnable printTitle, String... aliases) {
		return new Command(name, description, cli -> {
			if (clear) {
				cli.clear(false);
				printTitle.run();
			}
			try {
				handle.accept(cli.getPrompt().prompt(prompt));
			} catch (IOException e) {
				log.error("Cannot execute prompt for command " + name, e);
			}
		}, aliases);
	}

	public static Command printCommand(String name, String description, String msg, String... aliases) {
		return printCommand(name, description, () -> msg, aliases);
	}

	public static Command printCommand(String name, String description, Supplier<String> msg, String... aliases) {
		return new Command(name, description, p -> System.out.println(msg.get()), aliases);
	}

	public static Command consumerCommand(String name, String description, Consumer<CLI> consumer, String... aliases) {
		return new Command(name, description, consumer, aliases);
	}

	private final String name;
	private final String description;
	private final String[] aliases;
	private final Consumer<CLI> consumer;

	private Command(String name, String description, Consumer<CLI> consumer, String... aliases) {
		this.name = name;
		this.description = description;
		this.aliases = aliases;
		this.consumer = consumer;
	}


	public boolean hasAlias() {
		return getAliases().length>0;
	}

	public boolean matches(String cmd) {
		if (getName().equalsIgnoreCase(cmd)) return true;
		for (String alias : aliases) if (alias.equalsIgnoreCase(cmd)) return true;
		return false;
	}

	public void execute(CLI cli) {
		consumer.accept(cli);
	}
}
