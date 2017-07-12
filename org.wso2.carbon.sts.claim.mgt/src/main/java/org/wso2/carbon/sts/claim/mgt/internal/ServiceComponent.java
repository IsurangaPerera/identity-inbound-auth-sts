package org.wso2.carbon.sts.claim.mgt.internal;

import org.apache.cxf.sts.claims.ClaimsHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sts.claim.mgt.ClaimsManagerFactory;
import org.wso2.carbon.sts.claim.mgt.STSClaimsHandler;

@Component(name = "org.wso2.carbon.sts.claim.mgt.internal.ServiceComponent", immediate = true)
public class ServiceComponent {

	Logger logger = LoggerFactory.getLogger(ServiceComponent.class.getName());
	private ServiceRegistration<?> serviceRegistration;

	@Activate
	protected void start(BundleContext bundleContext) throws Exception {
		logger.info("Service Component is activated");

		bundleContext.registerService(
				ClaimsHandler.class.getName(), new STSClaimsHandler(), null);

		serviceRegistration = bundleContext.registerService(ClaimsManagerFactory.class.getName(),
				ClaimsManagerFactory.getInstance(), null);
	}

	@Deactivate
	protected void stop() throws Exception {
		logger.info("Service Component is deactivated");
		serviceRegistration.unregister();
	}

	// @Reference(name = "carbon.runtime.service", service =
	// CarbonRuntime.class, cardinality = ReferenceCardinality.MANDATORY, policy
	// = ReferencePolicy.DYNAMIC, unbind = "unsetCarbonRuntime")
	// protected void deactivate(ComponentContext ctxt) {
	// if (logger.isDebugEnabled()) {
	// logger.debug("Carbon STS bundle is deactivated");
	// }
	// }
}
