package org.wso2.carbon.sts.store;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.wso2.carbon.sts.store.dao.DBStsDAO;
import org.wso2.carbon.sts.store.util.STSStoreUtils;

public class JDBCTokenStore implements TokenStore {

	private static final Log log = LogFactory.getLog(JDBCTokenStore.class);
	private static int poolSize = 100;
	private static ExecutorService executorService = Executors
			.newFixedThreadPool(poolSize);
	private DBStsDAO dbStsDAO;

	public static ExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Getting existing cache if the cache available, else returns a newly
	 * created cache. This logic handles by javax.cache implementation
	 */
	public static Cache<String, SerializableToken> getTokenCache() {

		CacheManager manager = Caching.getCacheManagerFactory()
				.getCacheManager(STSMgtConstants.TOKEN_CACHE_MANAGER);
		return manager.getCache(STSMgtConstants.TOKEN_CACHE_ID);
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
		// put the Token to cache.
		Cache<String, SerializableToken> tokenCache = getTokenCache();
		if (tokenCache != null) {
			tokenCache.put(getTokenId(token),
					STSStoreUtils.getSerializableToken(token));
			executorService.submit(new TokenPersisterTask(token));
		}

	}

	@Override
	public void add(String arg0, SecurityToken arg1) {
	}

	@Override
	public SecurityToken getToken(String id) {
		String tokenId = getTokenId(id);

		Cache<String, SerializableToken> tokenCache = getTokenCache();
		if (tokenCache != null && tokenCache.containsKey(tokenId)) {
			try {
				return STSStoreUtils.getToken((SerializableToken) tokenCache
						.get(tokenId));
			} catch (XMLStreamException e) {
				throw new TrustException("Failed to get Token from cache", e);
			}
		}
		initDao();
		SecurityToken token = dbStsDAO.getToken(tokenId);

		if (token == null) {
			log.debug("Token is not present in cache or database");
		}

		if (tokenCache != null && token != null) {
			tokenCache.put(tokenId, STSStoreUtils.getSerializableToken(token));
		}
		return token;
	}

	@Override
	public Collection<String> getTokenIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(String arg0) {
		// TODO Auto-generated method stub

	}
	
	private void initDao() {
        if (dbStsDAO == null) {
            this.dbStsDAO = new DBStsDAO();
        }
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
			} catch (TrustException e) {
				log.error("Failed to persist token", e);
			}
		}

		private synchronized void persist() throws TrustException {
			try {
				new DBStsDAO().addToken(token);
			} catch (TrustException e) {
				throw new TrustException("Failed to persist token", e);
			}

		}
	}
}