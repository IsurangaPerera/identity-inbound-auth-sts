/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.sts.token.provider.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.DefaultAttributeStatementProvider;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.TokenProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sts.token.provider.STSAttributeStatementProvider;

@Component(name = "org.wso2.carbon.sts.token.provider.internal.TokenProviderComponent", immediate = true)
public class TokenProviderComponent {

	Logger logger = LoggerFactory.getLogger(TokenProviderComponent.class
			.getName());

	@Activate
	public void start(BundleContext bundleContext) throws Exception {
		logger.info("Token Service Provider Component is activated");

		bundleContext.registerService(
				AttributeStatementProvider.class.getName(),
				new STSAttributeStatementProvider(), null);

		SAMLTokenProvider tokenProvider = new SAMLTokenProvider();
		List<AttributeStatementProvider> attributeStatementProviders = new ArrayList<AttributeStatementProvider>();
		attributeStatementProviders
				.add(new DefaultAttributeStatementProvider());
		tokenProvider
				.setAttributeStatementProviders(attributeStatementProviders);
		bundleContext.registerService(TokenProvider.class.getName(),
				tokenProvider, null);
	}

	@Deactivate
	public void stop(BundleContext bundleContext) throws Exception {

	}
}
