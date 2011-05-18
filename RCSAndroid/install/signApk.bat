rem questo è il file estratto dal DB
dir android_service-unaligned.apk 

del android_service.apk

rem questa copia in realtà dovrebbe essere l'operazione di binary patch, che consiste nell'aggiunta del config.bin e nella modifica binaria del file resources.bin
copy android_service-unsigned.apk android_service-bp.apk

rem firma digitale con le chiavi generate da keysApkCreate.bat
jarsigner -verbose -keystore keys/service.keystore  -storepass serviceStorePassword -keypass serviceAliasPassword android_service-bp.apk ServiceCore
jarsigner -verify -verbose -certs android_service-bp.apk
zipalign -v 4 android_service-bp.apk android_service.apk