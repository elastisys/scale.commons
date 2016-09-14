package com.elastisys.scale.commons.server;

/**
 * The recognized types of SSL key and trust stores that can be used for SSL
 * clients/servers.
 */
public enum SslKeyStoreType {
    /** Java Key Store (JKS) type of key store. */
    JKS,
    /** PKCS #12 type of key store. */
    PKCS12
}
