package com.dictionary;

public class StringWrapper {
    private final int maxCharacters;
    private final int numNewLines;
    private final int numIndentations;
    private final boolean includeFirstLine;

    private static final String EMPTY_SPACE_AND_HYPHEN_WITH_DELIMINATOR = "(?<=-)|\\s+";
    private static final String EMPTY_SPACE = "\\s+";

    public StringWrapper(int maxCharacters, int numNewLines, int numIndentations, boolean includeFirstLine) {
        this.maxCharacters = maxCharacters;
        this.numNewLines = numNewLines;
        this.numIndentations = numIndentations;
        this.includeFirstLine = includeFirstLine;
    }

    public String wrapAndIndent(String input) {
        String newLines = new String(new char[numNewLines]).replace("\0", "\n");
        String indentations = new String(new char[numIndentations]).replace("\0", "\t");
        String insertion = newLines + indentations;

        String[] words = input.split(EMPTY_SPACE_AND_HYPHEN_WITH_DELIMINATOR);

        if (includeFirstLine) {
            words[0] = insertion + words[0];
        }

        int characterCount = 0;

        for (int i = 0; i < words.length; i++) {
            int numCharacters = words[i].length() + 1; // +1 for space
            characterCount += numCharacters;

            if (characterCount > maxCharacters) {
                characterCount = numCharacters;
                words[i] = insertion + words[i];
            }
        }

        String joinedWords = String.join(" ", words);
        String joinedHyphens = joinedWords.replaceAll("- ", "-");
        return joinedHyphens;
    }
}
