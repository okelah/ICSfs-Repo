before start working on this project, you need to have maven installed in your PC.

Steps:

1. Go to lib folder, execute install-jars.bat
2. Go to project's root, run the following command 
mvn clean install -P swing

then run 

mvn eclipse:eclipse

the project is now ready to be imported into eclipse.

...enjoy 
 
----------------------------------------------------------------------------------------------- 
 
Notes: 

add the following option to java execution line: 

-Xmx1024m