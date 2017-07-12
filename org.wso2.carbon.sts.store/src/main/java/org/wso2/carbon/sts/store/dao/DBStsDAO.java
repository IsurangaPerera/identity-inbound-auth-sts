package org.wso2.carbon.sts.store.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.TrustException;

import org.wso2.carbon.sts.store.DBQueries;
import org.wso2.carbon.sts.store.STSMgtConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * This utility used to manage the DB operations for token store.
 */
public class DBStsDAO {

    private static final Logger log = LoggerFactory.getLogger(DBStsDAO.class);

    /**
     * This is for adding token to DB.
     *
     * @param token Token
     * @throws Exception 
     */
    public void addToken(SecurityToken token) throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = DBQueries.ADD_TOKEN;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, token.getId());
            byte[] tokenByteContainer = getTokenContent(token);
            InputStream tokenInputStream = new ByteArrayInputStream(tokenByteContainer);
            prepStmt.setBinaryStream(2, tokenInputStream, tokenByteContainer.length);
            prepStmt.setTimestamp(3, new Timestamp(token.getCreated().getTime()));
            prepStmt.setTimestamp(4, new Timestamp(token.getExpires().getTime()));
            prepStmt.execute();
            connection.commit();

        } catch (Exception e) {
            IdentityDatabaseUtil.rollBack(connection);
            String msg = "Failed to add token";
            throw new Exception(msg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }

    }

    /**
     * This is for updating the token in DB
     *
     * @param token Token
     * @throws Exception 
     */
    public void updateToken(SecurityToken token) throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = DBQueries.UPDATE_TOKEN;

        try {
            prepStmt = connection.prepareStatement(query);
            byte[] tokenByteContainer = getTokenContent(token);
            InputStream tokenInputStream = new ByteArrayInputStream(tokenByteContainer);
            prepStmt.setBinaryStream(1, tokenInputStream, tokenByteContainer.length);
            prepStmt.setTimestamp(2, new Timestamp(token.getCreated().getTime()));
            prepStmt.setTimestamp(3, new Timestamp(token.getExpires().getTime()));
            
            prepStmt.setString(4, token.getId());
            prepStmt.executeUpdate();
            connection.commit();

        } catch (Exception e) {
            IdentityDatabaseUtil.rollBack(connection);
            String msg = "Failed to update token ";
            throw new Exception(msg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }

    }

    /**
     * This is for removing token
     *
     * @param tokenId tokenId
     * @throws Exception 
     */
    public void removeToken(String tokenId) throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = DBQueries.REMOVE_TOKEN;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tokenId);
            prepStmt.executeUpdate();

        } catch (Exception e) {
            String msg = "Failed to remove token";
            throw new Exception(msg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }

    }

    /**
     * This is for get all the token keys
     *
     * @return arrays of keys
     * @throws Exception 
     */
    public String[] getAllTokenKeys() throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> keyList;
        String[] keys = new String[0];
        String query = DBQueries.ALL_TOKEN_KEYS;
        try {
            prepStmt = connection.prepareStatement(query);
            rs = prepStmt.executeQuery();

            keyList = new ArrayList<>();
            if (rs != null) {
                while (rs.next()) {
                    keyList.add(rs.getString(STSMgtConstants.TOKEN_ID));
                }
            }
            if (!keyList.isEmpty()) {
                keys = keyList.toArray(new String[keyList.size()]);
            }
        } catch (Exception e) {
            String msg = "Failed to get all tokens";
            throw new Exception(msg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }
        return keys;
    }

    /**
     * This is to get Token from token id
     *
     * @param tokenId tokenId
     * @return Token
     */
    public SecurityToken getToken(String tokenId) throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        SecurityToken token = null;

        String query = DBQueries.GET_TOKEN;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tokenId);
            rs = prepStmt.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    Blob tokenContent = rs.getBlob(STSMgtConstants.TOKEN_CONTENT);
                    byte[] tokenContentBytes = tokenContent.getBytes(1, (int) tokenContent.length());
                    token = getToken(tokenContentBytes);
                }
            }
        } catch (Exception e) {
            String msg = "Failed to get token";
            throw new Exception(msg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }
        return token;
    }

    private SecurityToken getToken(byte[] tokenContentBytes) throws Exception {
        SecurityToken token;
        try {
            ByteArrayInputStream tokenContentByteArray = new ByteArrayInputStream(tokenContentBytes);
            ObjectInputStream tokenContentObject = new ObjectInputStream(tokenContentByteArray);
            Object tokenObj = tokenContentObject.readObject();
            token = (SecurityToken) tokenObj;
        } catch (Exception e) {
            String msg = "Failed to convert blob content to Token object ";
            throw new Exception(msg, e);
        }
        return token;
    }

    /**
     * This is to get all tokens from token store
     *
     * @return List of Tokens
     */
    public List<SecurityToken> getTokens() throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<SecurityToken> tokens = new ArrayList<SecurityToken>();

        String query = DBQueries.GET_ALL_TOKENS;
        try {
            prepStmt = connection.prepareStatement(query);
            rs = prepStmt.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    tokens.add(getToken((byte[]) rs.getObject(STSMgtConstants.TOKEN_CONTENT)));
                }
            }
            return tokens;
        } catch (Exception e) {
            String msg = "Failed to get all  tokens";
            throw new Exception(msg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }
    }



    /**
     * This is to get expired tokens from token store
     *
     * @param status state id of expired tokens
     * @return Token[]
     * @throws TrustException if failed to get expired tokens
     */
    public SecurityToken[] getExpiredTokens(int status) throws Exception {
        return getTokens(status);
    }

    /**
     * This is to get the renewed tokens from token store
     *
     * @param status state id of the renewed token
     * @return Token[]
     * @throws TrustException if failed to get renewed tokens
     */
    public SecurityToken[] getRenewedTokens(int status) throws Exception {
        return getTokens(status);
    }

    /**
     * This is to get canceled tokens from token store
     *
     * @param status state id of cancel token
     * @return Token[]
     * @throws Exception 
     */
    public SecurityToken[] getCancelledTokens(int status) throws Exception {
        return getTokens(status);
    }

    private SecurityToken[] getTokens(int status) throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<SecurityToken> tokens = new ArrayList<SecurityToken>();

        String query = DBQueries.GET_TOKENS_BY_STATE;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, status);
            rs = prepStmt.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                	SecurityToken token = getToken((byte[]) rs.getObject(STSMgtConstants.TOKEN_CONTENT));
                    tokens.add(token);
                }
            }
            return tokens.toArray(new SecurityToken[tokens.size()]);
        } catch (Exception e) {
            String msg = "Failed to get token";
            log.error(msg, e);
            throw new Exception(msg);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }
    }

    /**
     * This is to check token store empty or not
     *
     * @return boolean
     * @throws TrustException if failed to check the tokens availability
     */
    public boolean isTokensExist() throws Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean tokenExist = false;

        String query = DBQueries.TOKENS_EXISTS;
        try {
            prepStmt = connection.prepareStatement(query);
            rs = prepStmt.executeQuery();

            if (rs != null && rs.next()) {
                return true;
            }

        } catch (Exception e) {
            String msg = "Failed to check token exist";
            log.error(msg, e);
            throw new Exception(msg);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }
        return tokenExist;
    }

    private byte[] getTokenContent(SecurityToken token) throws IOException {
        ByteArrayOutputStream tokenArrayStream = new ByteArrayOutputStream();
        ObjectOutputStream tokenObjectStream = null;
        byte[] tokenBytes = null;
        try {
            tokenObjectStream = new ObjectOutputStream(tokenArrayStream);
            tokenObjectStream.writeObject(token);
            tokenObjectStream.flush();
            tokenBytes = tokenArrayStream.toByteArray();
        } finally {
            if (tokenObjectStream != null) {
                tokenObjectStream.close();
            }
        }
        return tokenBytes;
    }

}