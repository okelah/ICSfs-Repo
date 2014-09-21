
rem keytool -genkey -keystore ibrahim.pfx -storetype pkcs12 -storepass 123456 -keypass 123456 -alias ibrahim 



keytool -genkey -alias ibrahim -keystore ibrahim.pfx -storepass 123456 -validity 365 -keyalg RSA -keysize 2048 -storetype pkcs12
