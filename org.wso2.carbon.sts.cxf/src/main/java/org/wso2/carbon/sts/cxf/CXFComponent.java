package org.wso2.carbon.sts.cxf;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.wso2.carbon.sts.cxf",
		immediate = true
)
public class CXFComponent {
	
	@Activate
	public void start(BundleContext bundleContext) {
		
	}

}
