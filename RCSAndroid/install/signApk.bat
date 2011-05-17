
dir android_service-unaligned.apk 

delete android_service.apk
delete android_service-unaligned.apk

copy android_service-unsigned.apk android_service-unaligned.apk

jarsigner -verbose -keystore service.keystore  -storepass serviceStorePassword -keypass serviceAliasPassword android_service-unaligned.apk ServiceCore
jarsigner -verify -verbose -certs android_service-unaligned.apk
zipalign -v 4 android_service-unaligned.apk android_service.apk