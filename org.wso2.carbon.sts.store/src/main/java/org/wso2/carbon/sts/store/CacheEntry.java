package org.wso2.carbon.sts.store;

import java.time.Instant;

import org.apache.cxf.ws.security.tokenstore.SecurityToken;

public class CacheEntry {

    private final SecurityToken securityToken;
    private final Instant expires;

    CacheEntry(SecurityToken securityToken, Instant expires) {
        this.securityToken = securityToken;
        this.expires = expires;
    }

    /**
     * Get the SecurityToken
     * @return the SecurityToken
     */
    public SecurityToken getSecurityToken() {
        return securityToken;
    }

    /**
     * Get when this CacheEntry is to be removed from the cache
     * @return when this CacheEntry is to be removed from the cache
     */
    public Instant getExpiry() {
        return expires;
    }

}