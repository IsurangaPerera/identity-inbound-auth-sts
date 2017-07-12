package org.wso2.carbon.sts.store.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "org.wso2.carbon.sts.store.internal.STSStoreComponent", immediate = true)
public class STSStoreComponent {

	private static final Logger logger = LoggerFactory
			.getLogger(STSStoreComponent.class);

	@Activate
	protected void activate(BundleContext context) {
		logger.info("Identity STS Mgt bundle is activated");
	}
}
