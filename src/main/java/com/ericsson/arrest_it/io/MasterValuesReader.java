package com.ericsson.arrest_it.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;
import com.ericsson.arrest_it.logging.LogbackFileUtils;

public class MasterValuesReader {
    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);
    private final Map<String, String> values = new HashMap<String, String>();

    public MasterValuesReader(final File file) throws ArrestItException {
        readValuesFromFile(file);
    }

    private void readValuesFromFile(final File file) throws ArrestItException {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                final String key = line.substring(0, line.indexOf(Constants.EQUALS));
                final String value = line.substring(line.indexOf(Constants.EQUALS) + 1);
                values.put(key, value);
            }
        } catch (final FileNotFoundException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Could not find file: " + file.getAbsolutePath());
        } catch (final IOException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Could not read file: " + file.getAbsolutePath());
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (final IOException e) {
                slf4jLogger.error(e.getMessage());
                throw new ArrestItException("Could not close buffered reader.");
            }
        }
    }

    public String getValue(final String key) {
        return values.get(key);
    }

}
