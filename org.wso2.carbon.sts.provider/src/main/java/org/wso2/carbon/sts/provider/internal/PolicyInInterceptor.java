package org.wso2.carbon.sts.provider.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.AbstractPolicyInterceptor;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.EffectivePolicy;
import org.apache.cxf.ws.policy.EffectivePolicyImpl;
import org.apache.cxf.ws.policy.EndpointPolicyImpl;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.PolicyEngineImpl;
import org.apache.cxf.ws.policy.PolicyException;
import org.apache.cxf.ws.policy.PolicyVerificationInInterceptor;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

public class PolicyInInterceptor extends AbstractPolicyInterceptor {

	public PolicyInInterceptor(String phase) {
		super(PolicyConstants.POLICY_IN_INTERCEPTOR_ID, Phase.RECEIVE);
	}

	@Override
	protected void handle(Message msg) throws PolicyException {

		List<Interceptor<? extends Message>> interceptors = new ArrayList<Interceptor<? extends Message>>();
		Collection<Assertion> assertions = new ArrayList<>();
		Policy p = null;

		PolicyEngine pe = new PolicyEngineImpl();

		EndpointPolicyImpl endpi = new EndpointPolicyImpl(p);
		EffectivePolicyImpl effectivePolicy = new EffectivePolicyImpl();
		effectivePolicy.initialise(endpi, (PolicyEngineImpl) pe, true, msg);
		msg.put(EffectivePolicy.class, effectivePolicy);

		interceptors.addAll(effectivePolicy.getInterceptors());
		assertions.addAll(effectivePolicy.getChosenAlternative());

		// add interceptors into message chain
		for (Interceptor<? extends Message> i : interceptors) {
			msg.getInterceptorChain().add(i);
		}

		// Insert assertions of endpoint's vocabulary into message
		if (!assertions.isEmpty()) {
			msg.put(AssertionInfoMap.class, new AssertionInfoMap(assertions));
			msg.getInterceptorChain().add(
					PolicyVerificationInInterceptor.INSTANCE);
		}

	}

}
