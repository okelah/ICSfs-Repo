call mvn install:install-file -Dfile=frmall.jar -DgroupId=oracle.forms -DartifactId=frmall -Dversion=11g -Dpackaging=jar

call mvn install:install-file -Dfile=morena_license.jar -DgroupId=morena -DartifactId=morena-license -Dversion=1 -Dpackaging=jar
call mvn install:install-file -Dfile=morena7_osx.jar -DgroupId=morena -DartifactId=morena-osx -Dversion=7 -Dpackaging=jar
call mvn install:install-file -Dfile=morena7_win.jar -DgroupId=morena -DartifactId=morena-win -Dversion=7 -Dpackaging=jar
call mvn install:install-file -Dfile=morena7.jar -DgroupId=morena -DartifactId=morena -Dversion=7 -Dpackaging=jar

call mvn install:install-file -Dfile=morena7-sk.jar -DgroupId=morena -DartifactId=morena-sk -Dversion=7 -Dpackaging=jar

call mvn install:install-file -Dfile=jpdfnotes.jar -DgroupId=qoppa.com -DartifactId=jpdfnotes -Dversion=1 -Dpackaging=jar

call mvn install:install-file -Dfile=bcpkix-1.jar -DgroupId=bouncycastle.org -DartifactId=bcpkix -Dversion=1 -Dpackaging=jar
call mvn install:install-file -Dfile=bcprovider-1.jar -DgroupId=bouncycastle.org -DartifactId=bcprovider -Dversion=1 -Dpackaging=jar

call mvn install:install-file -Dfile=cmaps-1.jar -DgroupId=cmaps -DartifactId=cmaps -Dversion=1 -Dpackaging=jar

call mvn install:install-file -Dfile=cmyk-profiles.jar -DgroupId=cmyk-profiles -DartifactId=cmyk-profiles -Dversion=1 -Dpackaging=jar

call mvn install:install-file -Dfile=codec-1.jar -DgroupId=com.sun -DartifactId=codec -Dversion=1 -Dpackaging=jar
call mvn install:install-file -Dfile=imageio-1.jar -DgroupId=com.sun -DartifactId=imageio -Dversion=1 -Dpackaging=jar

pause ...