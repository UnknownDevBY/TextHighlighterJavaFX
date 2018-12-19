package sample;

import java.util.regex.Pattern;

public class StringConstants {
    public static final String  pathToFolder = "plugins";
    public static String        pathToOpenedFile = "";
    public static String[]      KEYWORDS;
    public static String        KEYWORD_PATTERN;
    public static final String  PAREN_PATTERN = "\\(|\\)";
    public static final String  BRACE_PATTERN = "\\{|\\}";
    public static final String  BRACKET_PATTERN = "\\[|\\]";
    public static final String  SEMICOLON_PATTERN = "\\;";
    public static final String  STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    public static final String  COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    public static final String  sampleCode =
            "package com.company;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello World\");\n" +
            "    }\n" +
            "}";
    public static Pattern       PATTERN;
}
