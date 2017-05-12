package org.wso2.carbon.sts.store;

public class DBQueries {

    public static final String ADD_TOKEN = "INSERT INTO IDN_STS_STORE (TOKEN_ID, TOKEN_CONTENT,CREATE_DATE," +
                                           " EXPIRE_DATE,STATE)  VALUES (?,?,?,?,?)";

    public static final String UPDATE_TOKEN = "UPDATE  IDN_STS_STORE SET TOKEN_CONTENT = ? ,CREATE_DATE = ?," +
                                              "EXPIRE_DATE = ?, STATE = ?  WHERE TOKEN_ID = ?";

    public static final String REMOVE_TOKEN = "DELETE FROM  IDN_STS_STORE WHERE TOKEN_ID = ?";

    public static final String ALL_TOKEN_KEYS = "SELECT TOKEN_ID  FROM  IDN_STS_STORE";

    public static final String GET_TOKEN = "SELECT TOKEN_CONTENT  FROM  IDN_STS_STORE  WHERE TOKEN_ID = ?";

    public static final String GET_ALL_TOKENS = "SELECT *  FROM  IDN_STS_STORE";

    public static final String VALID_TOKENS = "SELECT *  FROM  IDN_STS_STORE WHERE STATE =? OR STATE =?";

    public static final String GET_TOKENS_BY_STATE = "SELECT *  FROM  IDN_STS_STORE WHERE STATE = ?";

    public static final String TOKENS_EXISTS = "SELECT 1  FROM  IDN_STS_STORE";

    private DBQueries() {
    }
}
