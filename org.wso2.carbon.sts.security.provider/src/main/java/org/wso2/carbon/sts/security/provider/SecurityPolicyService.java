package org.wso2.carbon.sts.security.provider;

import org.apache.neethi.Policy;
import org.w3c.dom.Document;

public interface SecurityPolicyService {
	
	public void updateEffectivePolicy(String scenario);
	public Policy getEffectivePolicy();
	public Document gePolicyDocument();
}
