package org.wso2.carbon.sts.claim.mgt;

import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsManager;

public class ClaimsManagerFactory {

	private static ClaimsManager claimsManager;
	
	private ClaimsManagerFactory () {
		
	}
	
	public static ClaimsManager getInstance() {
		if(claimsManager != null) {
			return claimsManager;
		} else {
			claimsManager = new ClaimsManager();
			claimsManager.getClaimHandlers().add(new STSClaimsHandler());
			
			return claimsManager;
		}
	}
	
	public static void setClaimsHandler(ClaimsHandler handler) {
		claimsManager.getClaimHandlers().add(handler);
	}
	
	public static void removeAllClaimsHandlers() {
		claimsManager.getClaimHandlers().clear();
	}
}
