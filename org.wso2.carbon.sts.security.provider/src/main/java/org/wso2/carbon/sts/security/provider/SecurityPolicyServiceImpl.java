package org.wso2.carbon.sts.security.provider;

import org.apache.neethi.Policy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.wso2.carbon.sts.security.internal.DataHolder;
import org.wso2.carbon.sts.security.provider.util.SecurityScenarioDatabase;

@Component(
		name = "org.wso2.carbon.sts.security.policy.service",
		service = SecurityPolicyService.class,
		immediate = true
)
public class SecurityPolicyServiceImpl implements SecurityPolicyService {

	@Activate
	public void start() {
		// Need to get policy using tenant information
		/* String scenario = DataHolder.getInstance().getScenario(); */
		String scenario = "UTOverTransport";

		DataHolder.getInstance().setScenario(scenario);
	}

	@Override
	public void updateEffectivePolicy(String scenario) {

		// Update the security policy used by current tenant
	}

	@Override
	public Policy getEffectivePolicy() {
		return SecurityScenarioDatabase.getInstance()
				.getByWsuId(DataHolder.getInstance().getScenario()).getPolicy();
	}

	@Override
	public Document gePolicyDocument() {
		return SecurityScenarioDatabase.getInstance()
				.getByWsuId(DataHolder.getInstance().getScenario())
				.getPolicyDocument();
	}
}