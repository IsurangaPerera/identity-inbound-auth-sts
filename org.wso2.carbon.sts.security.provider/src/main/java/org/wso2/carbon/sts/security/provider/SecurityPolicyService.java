package org.wso2.carbon.sts.security.provider;

import javax.xml.stream.XMLStreamReader;

public interface SecurityPolicyService {
	
	public void updateEffectivePolicy(String scenario);
	public XMLStreamReader getEffectivePolicy();
}
