rem questa operazione occorre farla solo in fase di installazione.

mkdir keys
keytool -genkey -v -dname "cn=Server, ou=JavaSoft, o=Sun, c=US" -keystore keys/service.keystore -alias ServiceCore -keyalg RSA -keysize 2048 -validity 10000 -keypass serviceAliasPassword -storepass serviceStorePassword 
