jar -ufm /u01/BEA/as_1/forms/java/$1 icsfsmanifest.txt
/u01/BEA/as_1/jdk/bin/jarsigner -storetype pkcs12 -keystore /u/oracle/bank/java/ICSFS.p12 -storepass icsfs1234 -keypass icsfs1234 $1 "international computer systems (london) ltd's comodo ca limited id"
chmod 775 /u01/BEA/as_1/forms/java/$1