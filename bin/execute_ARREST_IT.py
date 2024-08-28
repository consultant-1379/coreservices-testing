import sys, getopt, subprocess, os, shutil
from multiprocessing.pool import Pool
from time import gmtime, strftime
from datetime import datetime

SEPARATOR = os.sep
DATE_FORMAT = "%Y-%m-%d %H:%M:%S"
PROPERTIES_FILE = '..' + SEPARATOR + 'config' + SEPARATOR + 'properties.txt'
JAR_NAME = ''
CONFIG_FILE_KEY = 'configKey'
NO_OF_USERS_KEY = 'usersKey'

def getJarName():
    files = [file_name for file_name in os.listdir('.') if os.path.isfile(file_name)]
    global JAR_NAME
    for file_name in files:
        if '.jar' in file_name:
            JAR_NAME = file_name
            print 'Initially found jar name: ' + JAR_NAME

def createGroups():
    print 'Creating groups to use in test cases.'
    print 'Value of jar name in create groups ' + JAR_NAME
    command = 'java -cp '+ JAR_NAME +' com.ericsson.arrest_it.main.GroupManagementMain -create'
    subprocess.call(command,shell=True)
    print 'Finishing creating groups.'

def getTestPathsFromConfigFile(configFile):
    lines = open(configFile).readlines()
    lines = [line.rstrip() for line in lines]
    return lines

def executeTest(testPath):
    command = generateArrestItCommand(testPath)
    print 'Starting Test: ' + command
    subprocess.call(command,shell=True)    

def isRunningOnWindows():
    return os.name == 'nt'

def generateArrestItCommand(testPath):
	getJarName()
	if not isRunningOnWindows():
		testPath = '\'' + testPath + '\''
	command = 'java -cp '+ JAR_NAME +' com.ericsson.arrest_it.main.TestRunner -t -f ' + testPath
	return command

def cleanUpInterimFiles():
    print 'Cleaning up interim folder.'
    if os.path.exists('../interim'):
        shutil.rmtree('../interim')
        
def runResultSummariser():
    properties = getDetailsFromPropertiesFile();
    command = 'java -cp ' + JAR_NAME + ' com.ericsson.arrest_it.io.SummaryWriter ' + properties['timezone'] + ' ' + properties['times'] + ' ' + properties['csv']
    subprocess.call(command,shell=True)
    
def deleteGroups():
    print 'Deleting groups that were used in the test cases.'
    command = 'java -cp '+ JAR_NAME +' com.ericsson.arrest_it.main.GroupManagementMain -delete'
    subprocess.call(command,shell=True)
    print 'Finishing deleting groups.'
    
def getDetailsFromPropertiesFile():
    lines = open(PROPERTIES_FILE).readlines();
    properties = {}
    for line in lines:
        if 'timezone' in line:
            line = line.rstrip()
            properties['timezone'] = line[line.index('=') + 1:]
        if 'times' in line:
            line = line.rstrip()
            properties['times'] = line[line.index('=') + 1:]
        if 'csv' in line:
            line = line.rstrip()
            properties['csv'] = line[line.index('=') + 1:]
    return properties

def getResultsArchiveFolderName(summaryFile):
    if len(summaryFile) > 0:
        newFolderName = summaryFile.replace('Arrest_It_Summary_','Arrest_It_Archive_')
        newFolderName = newFolderName.replace('.html','')
    else:
        newFolderName = 'Arrest_It_Archive_Incomplete_' + strftime("%Y-%m-%dT%H-%M-%S")
    return newFolderName

def archiveResults():
    print 'archiving results'
    resultFolder = '..' + SEPARATOR + 'results'
    filesToMove = []
    summaryFile = ''
    if os.path.exists(resultFolder):
        print 'found result folder'
        for files in os.walk(resultFolder).next():
            
            for file in files:
                print 'File I found' + file
                if file.endswith('.txt') or file.endswith('.csv'):
                    filesToMove.append(file)
                if file.endswith('.html'):
                    summaryFile = file
                    filesToMove.append(file)
    if len(filesToMove) > 0:
        newFolder = getResultsArchiveFolderName(summaryFile)
        os.mkdir(resultFolder + SEPARATOR + newFolder)
        for file in filesToMove:
            shutil.move(resultFolder + SEPARATOR + file, resultFolder + SEPARATOR + newFolder + SEPARATOR + file)

def getCommandLineArgs(argv):
    commLineArgs = {}
    configFile = ''
    noOfUsers = 4
    hasNoOfUsers = False
    hasConfigFile = False
    instructionMessage = 'execute_ARREST_IT.py -f <configFile> -u <noOfConcurrentUsers>'
    
    if len(argv) != 4:
        print instructionMessage
        sys.exit(2)
    try:
        opts, args = getopt.getopt(argv,"h:f:u:",["file=","users="])
    except getopt.GetoptError:
        print instructionMessage
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print instructionMessage
            print 'A config file contains a list of the tests that are going to be run concurrently.'
            print '-u denotes the number of concurrent executions Arrest-It will simulate, a suitable value will depend on the capabilities of the server under test'
            sys.exit()
        if opt in ("-f", "--configFile"):
            configFile = arg
            hasConfigFile = True
        if opt in ('-u', '--users'):
            try:
                noOfUsers = int(arg)
                hasNoOfUsers = True
            except (ValueError):
                print '-u must be an integer value'
                hasNoOfUsers = False
                
    
    if noOfUsers < 1:
        hasNoOfUsers = False
        
    if not hasConfigFile or not hasNoOfUsers:
        print 'Invalid Command Line Arguements'
        print instructionMessage
        sys.exit(2)
        
    commLineArgs[CONFIG_FILE_KEY] = configFile
    commLineArgs[NO_OF_USERS_KEY] = noOfUsers   
    return commLineArgs
            
def main(argv):
    
    commLineArgs = getCommandLineArgs(argv)

    startTime = strftime(DATE_FORMAT, gmtime())
    print 'Start Time: ' + startTime
    archiveResults()
    cleanUpInterimFiles()
    getJarName()
    createGroups()

 
    testPaths = getTestPathsFromConfigFile(commLineArgs[CONFIG_FILE_KEY])
    for path in testPaths:
        print 'This is the path: ' + path
    executionPool = Pool(commLineArgs[NO_OF_USERS_KEY])
    try:
        executionPool.map_async(executeTest,testPaths).get(43200)
        executionPool.close()
        executionPool.join
    except (KeyboardInterrupt):
        print "\n\n\n\n\n\n\n\n\nCaught Keyboard Interrupt, terminating workers\n\n\n\n\n\n\n\n"
        executionPool.terminate()
        executionPool.close()
        executionPool.join()
        print 'finished terminating workers'
        
        
    runResultSummariser()
    deleteGroups()
    cleanUpInterimFiles()
    
    print 'Start Time: ' + startTime
    endTime = strftime(DATE_FORMAT, gmtime())
    print 'End Time: ' + endTime
    
    difference = datetime.strptime(endTime, DATE_FORMAT) - datetime.strptime(startTime, DATE_FORMAT)
    print 'Time taken to execute: ' + str(difference)
    
if __name__ == '__main__':
    main(sys.argv[1:])