package org.wso2.carbon.sts.resource.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.Assertor;
import org.apache.cxf.ws.policy.PolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistryImpl;
import org.apache.cxf.ws.policy.PolicyVerificationInInterceptor;
import org.apache.cxf.ws.security.policy.interceptors.HttpsTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.IssuedTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.KerberosTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.SamlTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.SecureConversationTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.SpnegoTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.UsernameTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.WSSecurityInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.WSSecurityPolicyInterceptorProvider;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyContainingAssertion;

public class PolicyEngine {
	
	static PolicyInterceptorProviderRegistry reg = new PolicyInterceptorProviderRegistryImpl();
	static Collection<Assertion> assertions = new ArrayList<Assertion>();
	static PolicyEngine instance = new PolicyEngine();

	private PolicyEngine() {
		reg.register(new WSSecurityPolicyInterceptorProvider());
		reg.register(new WSSecurityInterceptorProvider());
		reg.register(new UsernameTokenInterceptorProvider());
		reg.register(new HttpsTokenInterceptorProvider());
		reg.register(new IssuedTokenInterceptorProvider());
		reg.register(new KerberosTokenInterceptorProvider());
		reg.register(new SamlTokenInterceptorProvider());
		reg.register(new SecureConversationTokenInterceptorProvider());
		reg.register(new SpnegoTokenInterceptorProvider());
	}

	public static PolicyEngine getInstance() {
		return instance;
	}

	static void initialiseInterceptors(Policy p, Message m) {
		Set<Interceptor<? extends Message>> out = new LinkedHashSet<Interceptor<? extends Message>>();
		for (Assertion a : getSupportedAlternatives(p, m)) {
			initialiseInterceptors(out, a, m);
		}
		
		m.getInterceptorChain().add(out);
        if (!assertions.isEmpty()) {
            m.put(AssertionInfoMap.class, new AssertionInfoMap(assertions));
            m.getInterceptorChain().add(PolicyVerificationInInterceptor.INSTANCE);
        }
	}

	static void initialiseInterceptors(
			Set<Interceptor<? extends org.apache.cxf.message.Message>> out,
			Assertion a, Message m) {
		QName qn = a.getName();

		List<Interceptor<? extends org.apache.cxf.message.Message>> i = null;
		i = reg.getInInterceptorsForAssertion(qn);
		out.addAll(i);

		if (a instanceof PolicyContainingAssertion) {
			Policy p = ((PolicyContainingAssertion) a).getPolicy();
			if (p != null) {
				for (Assertion a2 : getSupportedAlternatives(p, m)) {
					initialiseInterceptors(out, a2, m);
				}
			}
		}
	}

	static Collection<Assertion> getSupportedAlternatives(Policy p, Message msg) {
		Collection<Assertion> alternatives = new ArrayList<Assertion>();
		for (Iterator<List<Assertion>> it = p.getAlternatives(); it.hasNext();) {
			List<Assertion> alternative = it.next();
			if (supportsAlternative(alternative, null, msg)) {
				alternatives.addAll(alternative);
			}
		}
		assertions.addAll(alternatives);
		return alternatives;
	}

	public static boolean supportsAlternative(
			Collection<? extends PolicyComponent> alternative,
			Assertor assertor, Message m) {

		for (PolicyComponent pc : alternative) {
			if (pc instanceof Assertion) {
				Assertion a = (Assertion) pc;
				if (!a.isOptional()) {
					if (null != assertor && assertor.canAssert(a.getName())) {
						continue;
					}
					Set<PolicyInterceptorProvider> s = reg.get(a.getName());
					if (s.isEmpty()) {
						// System.out.println("Alternative " + a.getName() +
						// " is not supported");
						return false;
					}
					for (PolicyInterceptorProvider p : s) {
						if (!p.configurationPresent(m, a)) {
							// System.out.println("Alternative " + a.getName() +
							// " is not supported");
							return false;
						}
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
