WSE - HW1 -problem
============

This project is mavenized and uses Java, so install both of them.

http://maven.apache.org/

Maven makes it quite easy to build and test the project. 

Once you have cloned the project, from the project root you can execute:

    mvn package

This command will compile, run the tests, package and emit an executable jar file into the /target directory. 

You can then execute the jar file with the following command

    java -jar target/instructor.jar 
    
Alternatively, you can just use the command
    
    mvn test

..to compile and run the program without packaging it as a jar.

There are lots of convenient Maven commands: http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

Note that the project makes use of Junit. So problem modelling and solution verification should happen in those classes. Tests are located in src/test/java.

This project is configured to compile Java files. The jar file that is emitted will run on any JVM.

If you are using Eclipse install the Maven plugins.

Let me know if you have questions.
