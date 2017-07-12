package org.wso2.carbon.sts.store.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Dummy implementation. May need to replace with utility class for database operations
// provided by IS6
public class IdentityDatabaseUtil {

	public static void closeAllConnections(Connection connection, ResultSet rs,
			PreparedStatement prepStmt) {
		// TODO Auto-generated method stub
		
	}

	public static Connection getDBConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void rollBack(Connection connection) {
		// TODO Auto-generated method stub
		
	}
}
