package com.elastisys.scale.commons.security.jwt.impl;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;

import java.security.PublicKey;
import java.util.function.Supplier;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import com.elastisys.scale.commons.security.jwt.AuthTokenValidator;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * A simple {@link AuthTokenValidator} that validates Elastisys-issued auth
 * tokens. It does this by using the public key of a public/private signature
 * key pair to validate the signature of an authentication token and verifies
 * that the token claims look legitimate.
 */
public class ElastisysAuthTokenValidator implements AuthTokenValidator {
    /** The supplier of a public key used to sign tokens. */
    private final Supplier<PublicKey> publicKeySupplier;

    /**
     * Creates an {@link ElastisysAuthTokenValidator} with a given
     * {@link Supplier} of a signature key pair.
     *
     * @param publicKeySupplier
     *            A {@link Supplier} that retrieves a public key used to
     *            validate auth token signatures.
     */
    public ElastisysAuthTokenValidator(Supplier<PublicKey> publicKeySupplier) {
        this.publicKeySupplier = publicKeySupplier;
    }

    @Override
    public JwtClaims validate(String signedToken) throws InvalidJwtException {
        checkArgument(signedToken != null, "auth token cannot be null");
        checkArgument(!signedToken.isEmpty(), "auth token cannot be empty");

        JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
                // verify the signature with the public key
                .setVerificationKey(this.publicKeySupplier.get());
        jwtConsumerBuilder.setExpectedIssuer(ElastisysClaims.ISSUER);
        // set time of token expiry evaluation to now
        jwtConsumerBuilder.setRequireExpirationTime();
        NumericDate now = NumericDate.fromMilliseconds(UtcTime.now().getMillis());
        jwtConsumerBuilder.setEvaluationTime(now);
        JwtConsumer jwtConsumer = jwtConsumerBuilder.build();

        // Deserialize and validate the JWT and process it to the Claims
        return jwtConsumer.processToClaims(signedToken);
    }

}
