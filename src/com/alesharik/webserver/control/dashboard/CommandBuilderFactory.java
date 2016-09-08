package com.alesharik.webserver.control.dashboard;

public class CommandBuilderFactory {
    private static final CommandBuilderFactory INSTANCE = new CommandBuilderFactory();

    private MenuCommandBuilder builder = new MenuCommandBuilder();

    private CommandBuilderFactory() {
    }

    public static MenuCommandBuilder menu() {
        MenuCommandBuilder builder = INSTANCE.builder;
        builder.clear();
        return builder;
    }
}
