package org.wso2.carbon.sts.security.mgt;

import org.apache.neethi.Policy;
import org.wso2.carbon.sts.security.util.SecurityScenarioDatabase;

public class SecurityPolicyManager implements SecurityPolicyProvider {
	
	private static SecurityPolicyManager instance = new SecurityPolicyManager();

	private SecurityPolicyManager() {}
	
	public static SecurityPolicyManager getInstance() {
		
		return instance;
	}

	@Override
	public void updateEffectivePolicy(String scenario) {
		
		//Update the security policy used by current tenant
	}

	public Policy getEffectivePolicy() {
		
		//Need to get policy using tenant information
		/*String scenario = DataHolder.getInstance().getScenario();*/
		String scenario = "scenario1";
		return SecurityScenarioDatabase.getByWsuId(scenario).getPolicy();
	}
}
