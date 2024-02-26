package de.cyklon.tibbermanager.cli;

public record Title(String title, String color, int spacing) {

    private String space(String s) {
        if (spacing==0) return s;
        String[] lines = s.split("\n");
        String spacing = " ".repeat(this.spacing);
        for (int i = 0; i < lines.length; i++) {
            lines[i] = spacing + lines[i];
        }
        return String.join("\n", lines);
    }

    @Override
    public String toString() {
        return space(color + title + AnsiColor.RESET);
    }
}
