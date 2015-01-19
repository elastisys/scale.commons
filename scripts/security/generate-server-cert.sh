#!/bin/bash

#
# Generates a PKCS12 key store with a self-signed server certificate that
# can be used by servers wishing to serve HTTPS requests.
#
# 
# The script generates the following files:
#  - server_private.pem: the server's private key.
#  - server_certificate.pem: the server's certificate.
#  - server_keystore.p12: a PKCS12 key store (stores a set of private 
#    key-certificate pairs). The key store is protected by a password.
#
# The contents of the key store can be verified with the following command
# (note that the key password will need to be entered):
#   keytool -v -list -storetype pkcs12 -keystore ${destdir}/server_keystore.p12
#
# NOTE: relies on openssl and keytool being installed.
#

sample_subject="/C=SE/ST=AC/L=Umea/O=Elastisys/OU=TechTeam/CN=Server"
if [ $# -lt 2 ]; then
  echo "error: missing argument(s)" 2>&1
  echo "usage: ${0} <server-subject> <keystore-password>" 2>&1
  echo "  <server-subject> could, for example, be '${sample_subject}'" 2>&1
  exit 1
fi

server_subject=${1}
keystore_password=${2}

destdir=server
mkdir -p ${destdir}

# 1. Create a private key for the server:
echo "generating server's private key ..."
openssl genrsa -out ${destdir}/server_private.pem 2048
# 2. Create the server's self-signed X.509 certificate:
echo "generating server's certificate ..."
openssl req -new -x509 -key ${destdir}/server_private.pem \
   -out ${destdir}/server_certificate.pem -days 365 -subj ${server_subject}
# 3. Create a PKCS12 key store and import the key and certificate. Set password
#    to ${keystore_password}. This password also becomes the "key password":
echo "creating server key store ..."
openssl pkcs12 -export -inkey ${destdir}/server_private.pem -in ${destdir}/server_certificate.pem -out ${destdir}/server_keystore.p12 -password pass:${keystore_password}
