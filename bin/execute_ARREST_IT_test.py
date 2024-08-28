import unittest
import execute_ARREST_IT

class executeArrestItTests(unittest.TestCase):
    
    def testGetTestPathsFromConfigFile(self):
        testPaths = execute_ARREST_IT.getTestPathsFromConfigFile('test_arrest_it_config_file.txt')
        self.failUnless(testPaths[0] == 'AllTests\\Workspace\\Access_Area\\')
        self.failUnless(testPaths[1] == 'AllTests\\Workspace\\APN\\')
        self.failUnless(testPaths[2] == 'AllTests\\Workspace\\test.xml')
        
    def testGenerateArrestItCommand(self):
        testCommand = execute_ARREST_IT.generateArrestItCommand('AllTests\\Workspace\\APN\\')
        print 'testCommand:\n' + testCommand
        self.failUnless(testCommand == 'java -jar ArrestIT-jar-with-dependencies.jar -f AllTests\\Workspace\\APN\\')

def main():
    unittest.main()

if __name__ == '__main__':
    main()