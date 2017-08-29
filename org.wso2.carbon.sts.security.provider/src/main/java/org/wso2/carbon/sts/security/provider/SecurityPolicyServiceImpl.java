package org.wso2.carbon.sts.security.provider;

import org.apache.neethi.Policy;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.sts.security.provider.util.SecurityScenarioDatabase;

@Component(
		name = "org.wso2.carbon.sts.security.policy.service",
		service = SecurityPolicyService.class,
		immediate = true
)
public class SecurityPolicyServiceImpl implements SecurityPolicyService {

	@Override
	public void updateEffectivePolicy(String scenario) {
		
		//Update the security policy used by current tenant
	}
	
	@Override
	public Policy getEffectivePolicy() {
		
		//Need to get policy using tenant information
		/*String scenario = DataHolder.getInstance().getScenario();*/
		String scenario = "UTOverTransport";
		return SecurityScenarioDatabase.getInstance().getByWsuId(scenario).getPolicy();
	}
}