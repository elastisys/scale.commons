package com.elastisys.scale.commons.openstack;

import java.util.Objects;

import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Identifier.Type;

import com.elastisys.scale.commons.json.JsonUtils;

/**
 * Represents an OpenStack domain, specified either by name or by id.
 * <p>
 * This class is a thin wrapper around the {@link Identifier} class from
 * OpenStack4j, which does not offer equality comparisons.
 */
public class Domain {

    /** A domain specified either by name or by id. */
    private final Identifier domain;

    /**
     * Creates a {@link Domain}.
     *
     * @param domain
     *            A domain specified either by name or by id.
     */
    public Domain(Identifier domain) {
        this.domain = domain;
    }

    /**
     * Returns the {@link Identifier} representation of this {@link Domain}.
     *
     * @return
     */
    public Identifier toIdentifier() {
        return this.domain;
    }

    /**
     * Returns how the domain was specified - by name or by id.
     *
     * @return
     */
    public Type getType() {
        return this.domain.getType();
    }

    /**
     * Returns the id/name of the domain (whether its a name or an id is
     * indicated by {@link #getType()} and {@link #isTypeID()}).
     *
     * @return
     */
    public String getId() {
        return this.domain.getId();
    }

    /**
     * Returns <code>true</code> if this domain is specified by id.
     * 
     * @return
     */
    public boolean isTypeID() {
        return this.domain.isTypeID();
    }

    /**
     * Creates a domain from an {@link Identifier}.
     *
     * @param domain
     * @return
     */
    public static Domain of(Identifier domain) {
        return new Domain(domain);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Domain) {
            Domain that = (Domain) obj;
            return Objects.equals(this.domain.getId(), that.domain.getId()) //
                    && Objects.equals(this.domain.getType(), that.domain.getType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.domain.getId(), this.domain.getType());
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }
}
