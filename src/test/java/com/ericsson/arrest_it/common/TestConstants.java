package com.ericsson.arrest_it.common;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.File;
import java.net.URISyntaxException;

public class TestConstants {
    public static final String SEPARATOR = System.getProperty("file.separator");
    public static final File ROOT_DIR = getRootDirectory();
    public static final String PROJECT_RELATIVE_PATH = getRelativePath(ROOT_DIR);
    public static final String RELATIVE_PATH = PROJECT_RELATIVE_PATH + SEPARATOR + "src" + SEPARATOR + "test" + SEPARATOR + "resources";
    public static final String RESULT_FILE = RELATIVE_PATH + SEPARATOR + RESULTS_FOLDER + SEPARATOR + BASIC_SUMMARY_FILE + TXT_EXTENSION;

    public static final String TEST_SERVER_ADDRESS = "http://atrcxb1128.athtem.eei.ericsson.se:18080";

    public static String getRelativePath(final File rootDir) {
        final String absolutePath = rootDir.getAbsolutePath();
        String relativePath = absolutePath;

        if (absolutePath.endsWith(".")) {
            relativePath = removeLastCharacter(absolutePath);
        }

        final String binDir = SEPARATOR + "bin" + SEPARATOR;
        if (relativePath.endsWith(".jar")) {
            relativePath = absolutePath.subSequence(0, relativePath.indexOf(binDir)).toString();
        }

        return relativePath + SEPARATOR;
    }

    public static File getRootDirectory() {
        File rootDir = null;

        try {
            rootDir = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }

        if (!rootDir.getAbsolutePath().endsWith(".jar")) {
            final File relativePath = new File(".");
            final String relativePathName = removeLastCharacter(relativePath.getAbsolutePath());

            rootDir = new File(relativePathName);
        }

        return rootDir;
    }

    public static String removeLastCharacter(final String string) {
        return string.subSequence(0, string.length() - 1).toString();
    }
}
