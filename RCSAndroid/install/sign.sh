jarsigner -verbose -keystore ../../keys/service.keystore  -storepass serviceStorePassword -keypass serviceAliasPassword android_service-bp.apk ServiceCore
jarsigner -verify -verbose -certs android_service-bp.apk
zipalign -v 4 android_service-bp.apk android_service.apk
