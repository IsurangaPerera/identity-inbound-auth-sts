package org.wso2.carbon.sts.policy.mgt.internal;

import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.sts.policy.mgt.SecurityPolicyManager;
import org.wso2.carbon.sts.policy.mgt.SecurityPolicyProvider;

@Component(name = "org.wso2.carbon.sts.policy.mgt.internal.ServiceComponent", immediate = true)
public class ServiceComponent {

	Logger logger = Logger.getLogger(ServiceComponent.class.getName());
	private ServiceRegistration<?> serviceRegistration;

	@Activate
	protected void start(BundleContext bundleContext) throws Exception {
		logger.info("Policy Management Component is activated");
		
		//Replace this with appropriate policy retrieval method
		DataHolder.getInstance().setPolicyScenario(1);

		serviceRegistration = bundleContext.registerService(
				SecurityPolicyProvider.class.getName(),
				new SecurityPolicyManager(), null);
	}

	@Deactivate
	protected void stop() throws Exception {
		logger.info("Service Component is deactivated");

		serviceRegistration.unregister();
	}
}
