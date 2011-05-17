jarsigner -verbose -keystore ../keys/service.keystore  -storepass serviceStorePassword -keypass serviceAliasPassword tmp/android_service-bp.apk ServiceCore
jarsigner -verify -verbose -certs tmp/android_service-bp.apk
zipalign -v 4 tmp/android_service-bp.apk tmp/android_service.apk
