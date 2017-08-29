package org.wso2.carbon.sts.security.provider;

import org.apache.neethi.Policy;

public interface SecurityPolicyService {
	
	public void updateEffectivePolicy(String scenario);
	public Policy getEffectivePolicy();
}
