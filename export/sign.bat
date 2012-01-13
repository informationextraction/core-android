set JAVA_HOME="C:\Program Files\Java\jdk1.6.0_24"
set PATH="%PATH%;C:\Program Files\Java\jdk1.6.0_24\bin"

delete android_service.apk
delete android_service-unaligned.apk

keytool -genkey -v -dname "cn=Service, ou=JavaSoft, o=Sun, c=US" -keystore service.keystore -alias ServiceCore -keyalg RSA -keysize 2048 -validity 10000 -keypass serviceAliasPassword -storepass serviceStorePassword
copy android_service-unsigned.apk android_service-unaligned.apk

jarsigner -verbose -keystore service.keystore -storepass serviceStorePassword -keypass serviceAliasPassword android_service-unaligned.apk ServiceCore 
jarsigner -verify -verbose -certs android_service-unaligned.apk
zipalign -v 4 android_service-unaligned.apk android_service.apk
