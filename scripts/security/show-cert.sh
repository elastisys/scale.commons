#!/bin/bash

#
# displays the contents of a PEM-encoded certificate
#

if [ "${1}" = "" ]; then
    echo "error: expecting a PEM-encoded certificate as only input."
    exit 1
fi
pem_encoded_cert=${1}
openssl x509 -in ${pem_encoded_cert} -text
