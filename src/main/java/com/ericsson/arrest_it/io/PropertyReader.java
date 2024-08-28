package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.arrest_it.common.Constants;

public class PropertyReader {
    private static PropertyReader instance = null;
    private int[] times;
    private String timezone;
    private int maxNoOfValidationsPerUrl;
    private boolean isSucRaw;
    private boolean isVAppUsingVPN;
    private boolean validateCsv;
    private String endTime;
    private int uniqueTests;
    private final Map<String, String> properties;
    private int sybaseMillisBuffer = 0;

    protected PropertyReader() {
        properties = readPropertiesFile();

        final String[] stringTimes = properties.get(TIMES).split(COMMA);
        times = new int[stringTimes.length];

        for (int i = 0; i < stringTimes.length; i++) {
            times[i] = Integer.parseInt(stringTimes[i].trim());
        }
        setVAppUsingVPN(Boolean.parseBoolean(properties.get(IS_V_APP_USING_VPN).trim()));
        setSucRaw(Boolean.parseBoolean(properties.get(IS_SUC_RAW).trim()));
        setValidateCsv(Boolean.parseBoolean(properties.get(SHOULD_VALIDATE_CSV).trim()));
        timezone = properties.get(TIMEZONE).trim();
        maxNoOfValidationsPerUrl = Integer.parseInt(properties.get(MAX_NO_OF_VALIDATIONS_PER_URL).trim());
        endTime = properties.get(END_TIME).trim();
        uniqueTests = Integer.parseInt(properties.get(UNIQUE_TESTS).trim());
        setSybaseMillisBuffer(Integer.parseInt(properties.get(SYBASE_MILLIS_BUFFER)));
    }

    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }

        return instance;
    }

    private static Map<String, String> readPropertiesFile() {
        final Map<String, String> properties = new HashMap<String, String>();
        final File propertiesFile = findPropertiesFile();

        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(propertiesFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(TIMES + EQUALS)) {
                    properties.put(TIMES, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(MAX_NO_OF_VALIDATIONS_PER_URL + EQUALS)) {
                    properties.put(MAX_NO_OF_VALIDATIONS_PER_URL, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(TIMEZONE + EQUALS)) {
                    properties.put(TIMEZONE, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(IS_SUC_RAW + EQUALS)) {
                    properties.put(IS_SUC_RAW, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(IS_V_APP_USING_VPN + EQUALS)) {
                    properties.put(IS_V_APP_USING_VPN, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(SHOULD_VALIDATE_CSV + EQUALS)) {
                    properties.put(SHOULD_VALIDATE_CSV, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(END_TIME + EQUALS)) {
                    properties.put(END_TIME, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(UNIQUE_TESTS + EQUALS)) {
                    properties.put(UNIQUE_TESTS, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(UI_SERVER_NAME + EQUALS)) {
                    properties.put(UI_SERVER_NAME, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(DB_SERVER_NAME + EQUALS)) {
                    properties.put(DB_SERVER_NAME, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(UI_USR + EQUALS)) {
                    properties.put(UI_USR, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(UI_PASS + EQUALS)) {
                    properties.put(UI_PASS, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(DB_USR + EQUALS)) {
                    properties.put(DB_USR, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(DB_PASS + EQUALS)) {
                    properties.put(DB_PASS, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(DB_PORT + EQUALS)) {
                    properties.put(DB_PORT, line.substring(line.indexOf(EQUALS) + 1));
                } else if (line.startsWith(SERVICES_NAME + EQUALS)) {
                    properties.put(SERVICES_NAME, line.substring(line.indexOf(EQUALS) + 1));
                }
                else if (line.startsWith(SYBASE_MILLIS_BUFFER + EQUALS)) {
                    properties.put(SYBASE_MILLIS_BUFFER, line.substring(line.indexOf(EQUALS) + 1));
                }
            }

            bufferedReader.close();

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (final IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return properties;
    }

    private static File findPropertiesFile() {
        final StringBuilder propertiesFileBuilder = new StringBuilder();
        propertiesFileBuilder.append(Constants.RELATIVE_PATH);
        propertiesFileBuilder.append("config");
        propertiesFileBuilder.append(SEPARATOR);
        propertiesFileBuilder.append("properties.txt");

        return new File(propertiesFileBuilder.toString());
    }

    public int[] getTimes() {
        return times;
    }

    public void setTimes(final int[] times) {
        this.times = times;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public int getMaxNoOfValidationsPerUrl() {
        return maxNoOfValidationsPerUrl;
    }

    public void setMaxNoOfValidationsPerUrl(final int maxNoOfValidationsPerUrl) {
        this.maxNoOfValidationsPerUrl = maxNoOfValidationsPerUrl;
    }

    public boolean isSucRaw() {
        return isSucRaw;
    }

    public void setSucRaw(final boolean isSucRaw) {
        this.isSucRaw = isSucRaw;
    }

    public boolean isVAppUsingVPN() {
        return isVAppUsingVPN;
    }

    public void setVAppUsingVPN(final boolean isVAppUsingVPN) {
        this.isVAppUsingVPN = isVAppUsingVPN;
    }

    public boolean isValidateCsv() {
        return validateCsv;
    }

    public void setValidateCsv(final boolean validateCsv) {
        this.validateCsv = validateCsv;
    }

    public int getUniqueTests() {
        return uniqueTests;
    }

    public void setUniqueTests(final int uniqueTests) {
        this.uniqueTests = uniqueTests;
    }

    public String getUiServerURL() {
        final String serverName = this.properties.get(UI_SERVER_NAME).trim();
        final StringBuilder sb = new StringBuilder("https://");
        if (!isVAppUsingVPN) {
            sb.append(serverName);
            sb.append(SERVICES_URL);
        } else {
            sb.append(SERVICES_VAPP_URL);
        }
        sb.append(URL_SEPARATOR);
        sb.append(this.properties.get(SERVICES_NAME).trim());
        sb.append(URL_SEPARATOR);
        return sb.toString();
    }

    public String getDbServerURL() {
        final String serverName = this.properties.get(DB_SERVER_NAME).trim();
        final StringBuilder sb = new StringBuilder("https://");
        if (!isVAppUsingVPN) {
            sb.append(serverName);
            sb.append(SERVICES_URL);
        } else {
            sb.append(SERVICES_VAPP_URL);
        }
        sb.append(URL_SEPARATOR);
        sb.append(this.properties.get(SERVICES_NAME).trim());
        sb.append(URL_SEPARATOR);
        return sb.toString();
    }

    public String getDBPort() {
        return this.properties.get(DB_PORT).trim();
    }

    public String getDBUserName() {
        return this.properties.get(DB_USR).trim();
    }

    public String getDBPassword() {
        return this.properties.get(DB_PASS).trim();
    }

    public String getUIUserName() {
        return this.properties.get(UI_USR).trim();
    }

    public String getUIPassword() {
        return this.properties.get(UI_PASS).trim();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }
    public int getSybaseMillisBuffer() {
		return sybaseMillisBuffer;
	}

	public void setSybaseMillisBuffer(int sybaseMillisBuffer) {
		if(sybaseMillisBuffer > 0)
		{
		Math.abs(sybaseMillisBuffer);
		}
		this.sybaseMillisBuffer = sybaseMillisBuffer;
	}
}