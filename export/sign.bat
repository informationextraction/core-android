set JAVA_HOME="C:\Program Files\Java\jdk1.6.0_24"
set PATH="%PATH%;C:\Program Files\Java\jdk1.6.0_24\bin"

delete android_service.apk
delete android_service-unaligned.apk

keytool -genkey -v -dname "cn=Server, ou=JavaSoft, o=Sun, c=US" -keystore server.keystore -alias server -keyalg RSA -keysize 2048 -validity 10000 -keypass myserver -storepass mystore 
copy android_service-unsigned.apk android_service-unaligned.apk

jarsigner -verbose -keystore server.keystore  -storepass mystore -keypass myserver android_service-unaligned.apk server
jarsigner -verify -verbose -certs android_service-unaligned.apk
zipalign -v 4 android_service-unaligned.apk android_service.apk
