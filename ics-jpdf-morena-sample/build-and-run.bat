cls
call mvn clean install -P swing
call java -Xmx1024m -jar target\ics-jpdf-morena-sample-0.0.1-SNAPSHOT-jar-with-dependencies.jar
pause...

