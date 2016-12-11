package com.alesharik.webserver.main;

import com.alesharik.webserver.logger.configuration.LoggerConfiguration;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationBasePackage;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationPrefix;

@LoggerConfiguration
public final class MainLoggerConfiguration {
    public MainLoggerConfiguration() {
    }

    @LoggerConfigurationBasePackage
    public static final String basePackage = "com.alesharik.webserver";

    @LoggerConfigurationPrefix("*.main")
    public static final String mainPackage = "[Main]";

    @LoggerConfigurationPrefix("*.microservices")
    public static final String microservicePackage = "[Microservices]";

    @LoggerConfigurationPrefix("*.router")
    public static final String routerPackage = "[Router]";

    @LoggerConfigurationPrefix("*.serverless")
    public static final String serverless = "[Serverless]";
}
