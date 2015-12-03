#!/bin/bash

set -e

subject="/C=SE/O=Elastisys/CN=Client"


pkcs12_password=pkcs12pass
jks_password=jkspass
keypass=keypass

# 1. Generate the a private key:
echo "generating private key ..."
openssl genrsa -out private.pem 2048

echo "generating certificate ..."
openssl req -new -x509 -key private.pem -out certificate.pem -days 365 -subj ${subject}

echo "generating a PKCS12 keystore ..."
openssl pkcs12 -export -inkey private.pem -in certificate.pem \
  -out keystore.p12 -password pass:${pkcs12_password}

echo "generating a JKS keystore by importing JKS keystore ..."
keytool -importkeystore -noprompt \
        -srckeystore keystore.p12 -srcstoretype pkcs12 \
        -srcstorepass ${pkcs12_password} \
        -destkeystore keystore.p12.jks -deststoretype jks \
        -storepass ${jks_password}       


echo "generating a JKS keystore with a key pair ..."
keytool -genkeypair -dname "C=SE,O=Elastisys,CN=Client" -storepass ${jks_password} -keypass ${keypass} -keystore keystore_with_storepass_and_keypass.jks


echo "listing keystore.p12 ..."
keytool -list -keystore keystore.p12 -storepass ${pkcs12_password} -storetype PKCS12
echo "listing keystore.p12.jks ..."
keytool -list -keystore keystore.p12.jks -storepass ${jks_password} -storetype JKS
echo "listing keystore_with_storepass_and_keypass.jks ..."
keytool -list -keystore keystore_with_storepass_and_keypass.jks -storepass ${jks_password} -storetype JKS

echo "****** done ******"
