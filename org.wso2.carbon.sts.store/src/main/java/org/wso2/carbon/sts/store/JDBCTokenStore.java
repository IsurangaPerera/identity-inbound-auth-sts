package org.wso2.carbon.sts.store;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.wso2.carbon.sts.store.dao.DBStsDAO;

public class JDBCTokenStore implements TokenStore {

	private static final Logger log = LoggerFactory.getLogger(JDBCTokenStore.class);
	private static int poolSize = 100;
	private static ExecutorService executorService = Executors
			.newFixedThreadPool(poolSize);
	public static final long DEFAULT_TTL = 60L * 5L;
	public static final long MAX_TTL = DEFAULT_TTL * 12L;
	private Map<String, CacheEntry> tokenCache = new ConcurrentHashMap<>();
	private long ttl = DEFAULT_TTL;
	private DBStsDAO dbStsDAO;

	public static ExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * "#" are used for internal references. If a token-id comes with that we
	 * need to remove.
	 *
	 * @param token
	 * @return
	 */
	private String getTokenId(SecurityToken token) {
		if (token == null) {
			return "";
		}
		String tokenId = token.getId();
		if (tokenId != null && tokenId.startsWith("#")) {
			tokenId = tokenId.substring(1);
		}
		return tokenId;
	}

	/**
	 * "#" are used for internal references. If a token-id comes with that we
	 * need to remove
	 *
	 * @param tokenId
	 * @return
	 */
	private String getTokenId(String tokenId) {
		String tokenIdVal = tokenId;
		if (tokenId != null && tokenId.startsWith("#")) {
			tokenIdVal = tokenIdVal.substring(1);
		}
		return tokenIdVal;
	}

	@Override
	public void add(SecurityToken token) {
		String identifier = getTokenId(token);
		if (token != null && !StringUtils.isEmpty(identifier)) {
			CacheEntry cacheEntry = createCacheEntry(token);
			if (cacheEntry != null) {
				tokenCache.put(identifier, cacheEntry);
				executorService.submit(new TokenPersisterTask(token));
			}
		}
	}

	@Override
	public void add(String identifier, SecurityToken token) {
		if (token != null && !StringUtils.isEmpty(identifier)) {
			CacheEntry cacheEntry = createCacheEntry(token);
			if (cacheEntry != null) {
				tokenCache.put(identifier, cacheEntry);
				executorService.submit(new TokenPersisterTask(token));
			}
		}
	}

	@Override
	public SecurityToken getToken(String id) {
		processTokenExpiry();
		String tokenId = getTokenId(id);

		if (tokenCache.containsKey(tokenId)) {
			CacheEntry cacheEntry = tokenCache.get(tokenId);
			return cacheEntry.getSecurityToken();
		}
		initDao();

		SecurityToken token = null;
		try {
			token = dbStsDAO.getToken(tokenId);
		} catch (Exception e) {
			log.debug("Failed to get token from database");
		}

		if (token == null) {
			log.debug("Token is not present in cache or database");
		}

		if (token != null) {
			CacheEntry cacheEntry = createCacheEntry(token);
			tokenCache.put(tokenId, cacheEntry);
		}
		return token;
	}

	@Override
	public Collection<String> getTokenIdentifiers() {
		processTokenExpiry();
		return tokenCache.keySet();
	}

	/**
	 * Set a new (default) TTL value in seconds
	 * 
	 * @param newTtl
	 *            a new (default) TTL value in seconds
	 */
	public void setTTL(long newTtl) {
		ttl = newTtl;
	}

	@Override
	public void remove(String identifier) {
		if (!StringUtils.isEmpty(identifier) && tokenCache.containsKey(identifier)) {
            tokenCache.remove(identifier);
            executorService.submit(new TokenRemoverTask(identifier));
		}
	}

	private void initDao() {
		if (dbStsDAO == null) {
			this.dbStsDAO = new DBStsDAO();
		}
	}
	
	protected void processTokenExpiry() {
        Instant current = Instant.now();
        synchronized (tokenCache) {
            for (Map.Entry<String, CacheEntry> entry : tokenCache.entrySet()) {
                if (entry.getValue().getExpiry().isBefore(current)) {
                    tokenCache.remove(entry.getKey());
                }
            }
        }
	}

	private CacheEntry createCacheEntry(SecurityToken token) {
		Instant expires = Instant.now().plusSeconds(ttl);
		return new CacheEntry(token, expires);
	}

	protected static class TokenPersisterTask implements Runnable {

		private SecurityToken token;

		public TokenPersisterTask(SecurityToken token) {
			this.token = token;
		}

		@Override
		public void run() {
			try {
				persist();
			} catch (Exception e) {
				log.error("Failed to persist token", e);
			}
		}

		private synchronized void persist() throws Exception {
			try {
				new DBStsDAO().addToken(token);
			} catch (Exception e) {
				throw new Exception("Failed to persist token", e);
			}
		}
	}
	
	protected static class TokenRemoverTask implements Runnable {
		
		private String identifier;

		public TokenRemoverTask(String identifier){
			this.identifier = identifier;
		}
		@Override
		public void run() {
			try {
				remove(identifier);
			} catch (Exception e) {
				log.error("Failed to remove token", e);
			}
		}

		private synchronized void remove(String identifier) throws Exception {
			try {
				new DBStsDAO().removeToken(identifier);
			} catch (Exception e) {
				throw new Exception("Failed to remove token", e);
			}
		}
	}
}