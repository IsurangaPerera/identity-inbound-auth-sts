package org.wso2.carbon.sts.resource.security;


import org.apache.neethi.Policy;

public class SecurityPolicyServiceImpl {

	public void updateEffectivePolicy(String scenario) {
		
		//Update the security policy used by current tenant
	}
	
	public Policy getEffectivePolicy() {
		
		//Need to get policy using tenant information
		/*String scenario = DataHolder.getInstance().getScenario();*/
		String scenario = "UTOverTransport";
		return SecurityScenarioDatabase.getByWsuId(scenario).getPolicy();
	}
}