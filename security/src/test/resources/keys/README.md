These key pairs were generated as follows:

The pair generated with ssh-keygen:

    ssh-keygen -t rsa -b 4096 -C "noone@elastisys.com" -f sshkeygen_key
    ssh-keygen -e -f sshkeygen_key -m PKCS8 > sshkeygen_key.pub.pem

The pair generated with openssl:

    openssl genrsa -out openssl_private.pem 4096
    openssl rsa -in openssl_private.pem -pubout > openssl_public.pem

