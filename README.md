# MONTEZUMA #

## Project description ##
This is a Maven project. Its purpose is to automatically generate unit tests basing on real-time execution data, captured using Aspect Oriented Programming techniques. It can also replay the same execution over and over. It is therefore split over 3 modules (Recording, Replaying, Test-Generation), a "common" module and a test-cases module.


## Code quality notes ##
This project is still under quick-and-dirty development, and therefore many classes are NOT written as I would like to, and again... that is why this repository is not public yet, as it would be "impossible" to manage contributions from different people. Before going out in the wild this project needs:

1. Single-responsibility patterns and separation of concerns (e.g. TestMethodsWriter class completely refactored).
2. Comments on most methods and several chunks of code.


#### Checking this project functionality ####
Run these sections in sequence:  

1. SETUP
1. RECORDING  
1. REPLAYING  
1. GENERATING TESTS  


### SETUP ###
#### Setting up in Eclipse ###

1. Open a shell to the root of Montezuma and issue the commands:  
  a. mvn eclipse:clean  
  b. mvn eclipse:eclipse  
1. In Eclipse import Montezuma as Existing Maven Project (File --> Import... --> Maven --> Existing Maven Projects)
1. Open Eclipse's preferences, navigate to Java —> Compiler —> Errors/Warnings —> Deprecated and restricted API, and set "Forbidden reference (access rules):" to Warning or Ignore. This will be required until the project will start using Objenesis.

No exceptions are expected to appear in the console output during the following steps, but red lines should appear due to AspectJ logging.


### RECORDING ###

1. In Eclipse navigate to montezuma-cases/src/main/java/org/montezuma/test/traffic/recording/cases/RecordAll.java (or any in that same package), right-click on it and Run As... Java Application. It will run without recording execution data.
2. Change the last Run Configuration (Menu Run --> Run Configurations...) to add VM arguments (Arguments tab --> VM arguments): add "-javaagent:/Users/username/.m2/repository/org/aspectj/aspectjweaver/1.8.6/aspectjweaver-1.8.6.jar" without double-quotes; change the path (username) and versions according to what you have on your PC.
3. Run the RecordAll.java class again. It should run recording the execution data. To check that, check the montezuma-cases/recordings directory. It should now not be empty any more.


### REPLAYING ###

4. You can replay the executions by running as Java Application: montezuma-cases/src/main/java/org/montezuma/test/traffic/replaying/cases/ReplayAll.java (it doesn't need extra VM arguments)


### GENERATING TESTS ###

5. Run as Java Application montezuma-cases/src/main/java/org/montezuma/test/traffic/writing/cases/WriteAllMocking.java
6. Refresh the montezuma-cases/src/generatedtests folder. A 'java' subfolder should appear.
7. Right-click on the appeared 'java', click on Build Path --> Use as Source Folder. The new source folder will contain the generated test classes, possibly with compile-time errors.
8. Right-click on the newly added java source folder (montezuma-cases/src/generatedtests/java) and Run As... Unit Tests. Confirm if asked to proceed regardless of the existing project errors. Unit tests should be run, succeeding except for some of those in packages with "untestable.until" in their paths.


### TEST GENERATION OPTIONS COMPARISON (extra optional steps) ###

9. In a terminal shell run the script "move\_tests\_to\_previous.sh" in the Montezuma's root folder.
10. Back in eclipse, Run As... Java Application: montezuma-cases/src/main/java/org/montezuma/test/traffic/writing/cases/WriteAllNoMocking.java
11. In the terminal run the shell script "compare\_generated\_tests.sh" in the Montezuma's root folder. This should fire-up 'meld' comparing the test classes as generated by WriteAllMocking and WriteAllNoMocking. If you don't have meld, you can install it on Mac with 'brew install meld' or on Linux probably with something like 'sudo apt-get install meld' or on Windows use WinMerge, or perhaps just compare the 'montezuma-cases/previous\_generated\_tests' and 'montezuma-cases/src/generatedtests' folders with a diff program, like 'diff'.


### MAINTENANCE TOOLS
The "WriteAll" class runs all the code with all possible option combinations, so what is not run by it is dead code.