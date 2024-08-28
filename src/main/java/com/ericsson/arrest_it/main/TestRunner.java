package com.ericsson.arrest_it.main;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;
import com.ericsson.arrest_it.io.*;
import com.ericsson.arrest_it.main.cli.CMDLine;

public class TestRunner {
    private static String INTERIMFOLDER = RELATIVE_PATH + INTERIM_FILE_FOLDER;

    public static void main(final String[] args) throws ArrestItException {
        try {
            final CMDLine cmdLine = new CMDLine(args);
            final File folder = findTestDirectory();
            final String tzOffset = PropertyReader.getInstance().getTimezone();
            final int[] times = PropertyReader.getInstance().getTimes();
            final boolean isCsv = PropertyReader.getInstance().isValidateCsv();
            final boolean isMultiExecution = cmdLine.isTimeStamp();
            if (!isMultiExecution) {
                clearInterimFile();
                initializeResults();
            }
            if (cmdLine.isTestFile()) {

                String path = folder.getCanonicalPath();
                String testFilePath = cmdLine.getTestFile();
                testFilePath = handleSeparators(testFilePath);
                path += SEPARATOR + testFilePath;
                final File testFile = new File(path);
                if (testFile.isFile()) {
                    final TestDriver testdriver = new TestDriver(testFile);
                    testdriver.run();
                } else if (testFile.isDirectory()) {
                    final String[] extensions = new String[] { "xml" };
                    final Collection<File> files = FileUtils.listFiles(testFile, extensions, true);
                    for (final File file : files) {
                        final TestDriver testdriver = new TestDriver(file);
                        testdriver.run();
                    }
                } else {
                    System.out.println("Couldn't execute the test file: " + path);
                    System.exit(1);
                }
            } else {
                runTestsInDirectory(folder);
            }
            if (!isMultiExecution) {
                final SummaryWriter sw = new SummaryWriter(isCsv, times, tzOffset);
                sw.deserializeResults();
                final BasicSummaryWriter bsw = new BasicSummaryWriter(isCsv, times, tzOffset);
                bsw.deserializeResults();
            }
        } catch (final ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTestsInDirectory(final File folder) throws ArrestItException {

        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                runTestsInDirectory(file);
            } else if (file.isFile()) {
                System.out.println(file.toString());
                final TestDriver testdriver = new TestDriver(file);
                testdriver.run();
            }
        }
    }

    private static File findTestDirectory() {
        final StringBuilder testDirBuilder = new StringBuilder();
        testDirBuilder.append(Constants.RELATIVE_PATH);
        testDirBuilder.append(SEPARATOR);
        testDirBuilder.append("src");
        testDirBuilder.append(SEPARATOR);
        testDirBuilder.append("main");
        testDirBuilder.append(SEPARATOR);
        testDirBuilder.append("resources");
        testDirBuilder.append(SEPARATOR);
        testDirBuilder.append("TestCases");

        return new File(testDirBuilder.toString());
    }

    public static void clearInterimFile() {
        final File folder = new File(INTERIMFOLDER);

        if (!folder.exists()) {
            return;
        }

        final File[] fList = folder.listFiles();
        for (int i = 0; i < fList.length; i++) {
            final String serFile = fList[i].getName();
            if (serFile.endsWith(SERIALIZATION_EXTENSION)) {
                fList[i].delete();
            }
        }
    }

    public static void initializeResults() {
        final File resultsFolder = new File(RELATIVE_PATH + RESULTS_FOLDER);

        if (!resultsFolder.exists()) {
            return;
        }
        final File[] resultsFolderFiles = resultsFolder.listFiles();
        final List<File> resultsToMove = new ArrayList<File>();
        String newResultSubDirName = "";

        for (final File resultFile : resultsFolderFiles) {
            if (resultFile.isFile()) {
                resultsToMove.add(resultFile);
            }
            if (resultFile.getName().contains(HTML_EXTENSION)) {
                newResultSubDirName = resultFile.getName();
            }
        }
        if (resultsToMove.size() > 0) {
            if (newResultSubDirName.contains(HTML_EXTENSION)) {
                newResultSubDirName = getArchiveNameFromSummaryFileName(newResultSubDirName);
            } else {
                newResultSubDirName = getNewArchiveFolderName();
            }

            final File newResultSubDir = new File(newResultSubDirName);
            newResultSubDir.getParentFile().mkdirs();
            for (final File resultFile : resultsToMove) {
                try {
                    FileUtils.moveFileToDirectory(resultFile, newResultSubDir, true);
                } catch (final IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    static String getArchiveNameFromSummaryFileName(String fileName) {
        fileName = fileName.substring(0, fileName.indexOf("."));
        fileName = fileName.replaceAll(SUMMARY_FILE, RESULTS_BACKUP_FOLDER);
        return RELATIVE_PATH + RESULTS_FOLDER + SEPARATOR + fileName;

    }

    static String getNewArchiveFolderName() {
        String now = new DateTime().toString();
        now = now.substring(0, now.indexOf("."));
        now = now.replaceAll(":", "-");
        final String newFolder = RESULTS_BACKUP_FOLDER + "Incomplete_" + now;
        return RELATIVE_PATH + RESULTS_FOLDER + SEPARATOR + newFolder;
    }

    static String handleSeparators(String filePath) {
        filePath = FilenameUtils.separatorsToSystem(filePath);

        return filePath;
    }
}
