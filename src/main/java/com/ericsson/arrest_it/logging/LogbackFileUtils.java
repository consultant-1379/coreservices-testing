package com.ericsson.arrest_it.logging;

import static com.ericsson.arrest_it.common.Constants.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class LogbackFileUtils {
    public static final String ARREST_IT_LOGGER = "ARREST_IT_LOGGER";
    private static FileAppender<ILoggingEvent> fileAppender;
    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final Logger myLogger = loggerContext.getLogger(ARREST_IT_LOGGER);

            final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(loggerContext);
            encoder.setPattern("%d{HH:mm:ss.SSS} [%-5level] %msg %n");
            encoder.start();

            fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setContext(loggerContext);
            fileAppender.setName("FILE_LOGGER");
            fileAppender.setAppend(false);
            fileAppender.setEncoder(encoder);
            myLogger.addAppender(fileAppender);

            initialized = true;
        }
    }

    public static void start(String filePath) {
        init();
        stop();
        filePath = splitFileName(filePath);
        fileAppender.setFile(RELATIVE_PATH + "logs" + SEPARATOR + filePath);
        fileAppender.start();
    }

    public static String splitFileName(String filePath) {
        filePath = filePath.substring(filePath.indexOf(TEST_FOLDER) + TEST_FOLDER.length() + 1);

        if (SEPARATOR.equals("\\")) {
            filePath = filePath.replaceAll("\\" + SEPARATOR, "_");
        } else {
            filePath = filePath.replaceAll("/", "_");
        }
        filePath = filePath.replace(".xml", ".log");

        return filePath;
    }

    public static void stop() {
        if (fileAppender.isStarted()) {
            fileAppender.stop();
        }
    }
}
