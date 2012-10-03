@echo off

openssl pkcs12 -in %1 -out temppemfile.pem -passin pass:%3 -passout pass:password -chain
openssl pkcs12 -export -in temppemfile.pem -out tempkeystore.p12 -name "signapplet" -passin pass:password -passout pass:password

del %2 2> nul

keytool -importkeystore -srckeystore tempkeystore.p12 -destkeystore %2 -srcstoretype pkcs12 -deststoretype JKS -srcstorepass password -deststorepass password

del temppemfile.pem
del tempkeystore.p12

