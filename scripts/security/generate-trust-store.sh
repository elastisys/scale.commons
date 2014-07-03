#!/bin/bash

#
# This script generates a trust store where certificates belonging to
# trusted clients can be stored for (HTTPS) servers wishing to perform
# client certificate-based authentication, where clients need to supply a
# certificate to prove their identity.
#
# The script generates the following file:
#  - server_truststore.jks: a JKS (Java Key Store) key store used to store 
#    trusted client certificates. The trust store is protected by a password.
#
# The contents of the trust store can be verified with the following command
# (note that the trust store password will need to be entered):
# 
#   keytool -v -list -storetype jks -keystore ${destdir}/server_truststore.jks
#
# NOTE: relies on keytool being installed.
#

if [ $# -lt 2 ]; then
  echo "error: missing argument(s)" 2>&1
  echo "usage: ${0} <truststore-password> <trusted-certificate> ..." 2>&1
  exit 1
fi

destdir=server
mkdir -p ${destdir}

truststore_password=${1}
shift
# Create a JKS trust store to which all trusted client certificates that are 
# to be trusted by the server are added.
for trusted_cert in $@; do
  certkey=$(date +%s)
  keytool -importcert -trustcacerts -noprompt -alias ${certkey} \
    -keystore ${destdir}/server_truststore.jks -storetype jks \
    -file ${trusted_cert} -storepass ${truststore_password}
done





