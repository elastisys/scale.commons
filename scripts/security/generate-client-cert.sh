#!/bin/bash

#
# This script generates a self-signed client SSL certificate with a private
# key and also places it in a PKCS12 key store, for later use by a Java 
# HTTPS/SSL client. The script can also be told to generate a Java Key Store 
# (JKS) that imports the contents of the PKCS12 key store, if a JKS keystore
# password is given on the command-line.
#
# Note: when using the PKCS12 key store from within a Java application, you 
# need to specify the PKCS12 keystore password as "keystore password".
#
# Note: when using the JKS key store from within a Java application, you need 
# to specify the JKS password as "keystore password" and the PKCS12 keystore 
# password as "key [manager] password".
#
# The script generates the following files:
#  - client_private.pem: the client's private key.
#  - client_certificate.pem: the client's certificate.
#  - client_keystore.p12 -- a PKCS12 key store. The key store is protected 
#    by the PKCS12 keystore password.
#  - client_keystore.jks -- a JKS key store. The key store is protected 
#    by the JKS keystore password.
#
# NOTE: relies on openssl and keytool being installed.
#

sample_subject="/C=SE/O=Elastisys/CN=Client"
if [ $# -lt 2 ]; then
  echo "error: missing argument(s)" 2>&1
  echo "usage: ${0} <client-subject> <PKCS12 keystore password> [JKS keystore password]" 2>&1
  echo "  <client-subject> could, for example, be '${sample_subject}'" 2>&1
  exit 1
fi

client_subject=${1}
keystore_password=${2}
jks_password=${3}

destdir=client
mkdir -p ${destdir}


# 1. Generate the client's private key:
echo "generating client's private key ..."
openssl genrsa -out ${destdir}/client_private.pem 2048
# 2. Create the client's self-signed X.509 certificate:
echo "generating client's certificate ..."
openssl req -new -x509 -key ${destdir}/client_private.pem -out ${destdir}/client_certificate.pem -days 365 -subj ${client_subject}
# 3. Create a PKCS12 key store and import the key and certificate.
echo "generating PKCS12 keystore ..."
openssl pkcs12 -export -inkey ${destdir}/client_private.pem \
  -in ${destdir}/client_certificate.pem \
  -out ${destdir}/client_keystore.p12 -password pass:${keystore_password}

# 4. (Optional) This step imports the pkcs12 key store into a 
#    Java key store, if a JKS password was given.
if [ "${jks_password}" != "" ]; then
    echo "importing PKCS12 keystore to a JKS keystore ..."
    keytool -importkeystore -noprompt \
       -srckeystore ${destdir}/client_keystore.p12 -srcstoretype pkcs12 \
       -srcstorepass ${keystore_password} \
       -destkeystore ${destdir}/client_keystore.jks -deststoretype jks \
       -storepass ${jks_password}       
fi




