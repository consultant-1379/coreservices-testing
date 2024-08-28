package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.List;

import com.ericsson.arrest_it.results.TestResult;
import com.ericsson.arrest_it.results.TestResultHolder;

/**
 * This class allows TestResultHolder object to be serialised. This is necessary as multiple executions will produce separate results. Once the
 * individual results are serialised they can be read together and a view of the entire execution can be obtained.
 */
public class ResultSerializer {
    private static String INTERIMFILE = RELATIVE_PATH + INTERIM_FILE_FOLDER + INTERIM_FILE_NAME;
    private final String interimFileName;
    private ObjectOutput serializeOut;
    private OutputStream fileOutStream;

    public ResultSerializer(final String filePath) {

        this.interimFileName = INTERIMFILE + "_" + splitFileName(filePath) + SERIALIZATION_EXTENSION;

    }

    public String splitFileName(String filePath) {
        filePath = filePath.substring(filePath.indexOf(TEST_FOLDER) + TEST_FOLDER.length() + 1);

        if (SEPARATOR.equals("\\")) {
            filePath = filePath.replaceAll("\\" + SEPARATOR, "_");
        } else {
            filePath = filePath.replaceAll("/", "_");
        }

        filePath = filePath.replace(".xml", "");

        return filePath;
    }

    public void startSerialization() {

        try {
            final File interimFile = new File(interimFileName);
            interimFile.getParentFile().mkdirs();
            interimFile.createNewFile();
            fileOutStream = new FileOutputStream(interimFile);
            final OutputStream buffer = new BufferedOutputStream(fileOutStream);
            serializeOut = new ObjectOutputStream(buffer);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void stopSerialization() {
        try {
            serializeOut.close();
            fileOutStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void serializeFrameWorkResults(final String filePathName, final List<TestResult> frameWorkResults) {
        final TestResultHolder frameWorkResultHolder = new TestResultHolder(filePathName, frameWorkResults);
        serializeResults(frameWorkResultHolder);
    }

    public void serializeResults(final TestResultHolder testResultHolder) {
        try {
            serializeOut.writeObject(testResultHolder);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
