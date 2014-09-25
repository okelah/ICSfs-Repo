call mvn install:install-file -Dfile=frmall.jar -DgroupId=oracle.forms -DartifactId=frmall -Dversion=11g -Dpackaging=jar

call mvn install:install-file -Dfile=morena_license.jar -DgroupId=morena -DartifactId=morena-license -Dversion=1 -Dpackaging=jar
call mvn install:install-file -Dfile=morena7_osx.jar -DgroupId=morena -DartifactId=morena-osx -Dversion=7 -Dpackaging=jar
call mvn install:install-file -Dfile=morena7_win.jar -DgroupId=morena -DartifactId=morena-win -Dversion=7 -Dpackaging=jar
call mvn install:install-file -Dfile=morena7.jar -DgroupId=morena -DartifactId=morena -Dversion=7 -Dpackaging=jar

call mvn install:install-file -Dfile=morena7-sk.jar -DgroupId=morena -DartifactId=morena-sk -Dversion=7 -Dpackaging=jar






