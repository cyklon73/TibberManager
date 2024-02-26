package de.cyklon.tibbermanager.cli;

import de.codeshelf.consoleui.elements.*;
import de.codeshelf.consoleui.prompt.CheckboxPrompt;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.ListPrompt;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import de.codeshelf.consoleui.prompt.builder.CheckboxPromptBuilder;
import de.codeshelf.consoleui.prompt.builder.ExpandableChoicePromptBuilder;
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigField<T extends PromptableElementIF, R extends PromtResultItemIF> {

	@Getter
	private final String id, displayName;
	private final Supplier<PromptBuilder> builder;
	private final Class<T> element;
	private final Consumer<R> handler;
	private final Supplier<String>[] params;

	@SafeVarargs
	public ConfigField(Supplier<PromptBuilder> builder, String id, String displayName, Class<T> element, Consumer<R> handler, Supplier<String>... params) {
		this.id = id;
		this.displayName = displayName;
		this.builder = builder;
		this.element = element;
		this.handler = handler;
		this.params = params;
	}

	private List<PromptableElementIF> buildElements() {
		PromptBuilder builder = this.builder.get();
		if (element.equals(Checkbox.class)) {
			CheckboxPromptBuilder cb = builder.createCheckboxPrompt()
					.name(id)
					.message(displayName);
			for (int i = 0; i < params.length; i++) {
				cb.newItem(params[i].get()).text(params[++i].get()).add();
			}
			cb.addPrompt();
		} else if (element.equals(ListChoice.class)) {
			ListPromptBuilder lb = builder.createListPrompt()
					.name(id)
					.message("Current " + displayName + ": " + params[0].get());
			for (int i = 1; i < params.length; i++) {
				lb.newItem(params[i].get()).text(params[++i].get()).add();
			}
			lb.addPrompt();
		} else if (element.equals(InputValue.class)) {
			builder.createInputPrompt()
					.name(id)
					.message(displayName)
					.defaultValue(params[0].get())
					.addPrompt();
		}
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public void prompt(ConsolePrompt prompt) throws IOException {
		Map<String, ? extends PromtResultItemIF> result = prompt.prompt(buildElements());
		handler.accept((R) result.get(id));
	}

}
