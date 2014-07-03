#!/bin/bash

#
# This script generates a self-signed client SSL certificate with a private
# key and also places it in a PKCS12 key store, for later use by a Java 
# HTTPS/SSL client.
#
# Note: when using the PKCS12 key store from within a Java application, you 
# need to specify "pkcs12password" as "keystore password".
#
# The script generates the following files:
#  - client_private.pem: the client's private key.
#  - client_certificate.pem: the client's certificate.
#  - client_keystore.p12 -- a PKCS12 key store. The key store is protected 
#    by a password.
#
# NOTE: relies on openssl and keytool being installed.
#

sample_subject="/C=SE/O=Elastisys/CN=Client"
if [ $# -lt 2 ]; then
  echo "error: missing argument(s)" 2>&1
  echo "usage: ${0} <client-subject> <keystore-password>" 2>&1
  echo "  <client-subject> could, for example, be '${sample_subject}'" 2>&1
  exit 1
fi

client_subject=${1}
keystore_password=${2}

destdir=client
mkdir -p ${destdir}


# 1. Generate the client's private key:
echo "generating client's private key ..."
openssl genrsa -out ${destdir}/client_private.pem 2048
# 2. Create the client's self-signed X.509 certificate:
echo "generating client's certificate ..."
openssl req -new -x509 -key ${destdir}/client_private.pem -out ${destdir}/client_certificate.pem -days 365 -subj ${client_subject}
# 3. Create a PKCS12 key store and import the key and certificate.
openssl pkcs12 -export -inkey ${destdir}/client_private.pem \
  -in ${destdir}/client_certificate.pem \
  -out ${destdir}/client_keystore.p12 -password pass:${keystore_password}

# 4. (Optional) This step can be used to import the pkcs12 key store into a 
#    Java key store. Set destination keystore password to "jkspassword". The 
#    source keystore password is "pkcs12password": 
#keytool -importkeystore -srckeystore client_keystore.p12 -srcstoretype pkcs12 -#destkeystore client_keystore.jks -deststoretype jks
#Note: when using the JKS key store from withing a Java application, you need 
#to specify "jkspassword" as "keystore password" and "pkcs12password" as 
#"key [manager] password".




