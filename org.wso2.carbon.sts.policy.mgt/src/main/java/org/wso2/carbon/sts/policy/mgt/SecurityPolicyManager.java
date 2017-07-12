package org.wso2.carbon.sts.policy.mgt;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.neethi.PolicyBuilder;
import org.apache.neethi.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sts.policy.mgt.internal.DataHolder;
import org.wso2.carbon.sts.policy.util.PolicyConstants;

public class SecurityPolicyManager implements SecurityPolicyProvider {
	
	private static final Logger logger = LoggerFactory
			.getLogger(DataHolder.class);

	private Policy effectivePolicy;

	@Override
	public void updateEffectivePolicy(int scenario) {

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader streamReader = null;
		try {
			streamReader = factory.createXMLStreamReader(new FileReader(
					PolicyConstants.POLICY_REPOSITORY_PATH + Integer.toString(scenario)
							+ ".xml"));
		} catch (FileNotFoundException | XMLStreamException e) {
			logger.error("Policy File Not Found");
		}
		PolicyBuilder builder = new PolicyBuilder();
		setEffectivePolicy(builder.getPolicy(streamReader));
	}

	public Policy getEffectivePolicy() {
		return effectivePolicy;
	}

	public void setEffectivePolicy(Policy effectivePolicy) {
		this.effectivePolicy = effectivePolicy;
	}

}
