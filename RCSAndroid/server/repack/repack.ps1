del -force -recurse apk.default
java.exe -jar apktool.jar d -s -r ../../output/release/core.android.v2.apk apk.default
copy assets/c.bin apk.default/assets
copy assets/r.bin apk.default/assets

java.exe -jar apktool.jar b apk.default output.default.apk
jarsigner.exe -keystore certs/android.keystore -storepass password -keypass password output.default.apk ServiceCore
zipalign.exe -f 4 output.default.apk installer.default.apk
adb uninstall com.android.networking
adb install installer.default.apk

pause