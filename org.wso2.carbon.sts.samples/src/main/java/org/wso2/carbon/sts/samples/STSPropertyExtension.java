package org.wso2.carbon.sts.samples;

import java.util.Properties;

import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.wso2.carbon.cxf.sts.sample",
		service = STSPropertiesMBean.class,
		immediate = true
)
public class STSPropertyExtension extends StaticSTSProperties {
	
	@Activate
	public void start(BundleContext bundleContext) {
		
        Crypto crypto = null;
		try {
			crypto = CryptoFactory.getInstance(getEncryptionProps(bundleContext));
		} catch (WSSecurityException e) {
			e.printStackTrace();
		}
        
		setEncryptionCrypto(crypto);
        setSignatureCrypto(crypto);
        setEncryptionUsername("myservicekey");
        setSignatureUsername("mystskey");
        setCallbackHandler(new PasswordCallbackHandler());
        setIssuer("wso2-sts");
        
	}
	
	private Properties getEncryptionProps(BundleContext c) {
        Properties properties = new Properties();
        properties.put(
            "org.apache.wss4j.crypto.provider", "org.apache.wss4j.common.crypto.Merlin"
        );
        properties.put("org.apache.wss4j.crypto.merlin.keystore.password", "stsspass");
        properties.put("org.apache.wss4j.crypto.merlin.keystore.file", c.getBundle().getResource("stsstore.jks").toString());

        return properties;
    }
}
