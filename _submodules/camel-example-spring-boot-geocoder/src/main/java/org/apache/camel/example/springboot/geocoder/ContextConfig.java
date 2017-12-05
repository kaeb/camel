package org.apache.camel.example.springboot.geocoder;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A class for setting Proxy for HTTPComponent.
 * @author w2327eng
 *
 */
//@Configuration
//@ConfigurationProperties(prefix = "camel.component.http")
public class ContextConfig {
	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Proxy host for Camel HTTP component.
	 */
	private String proxyHost;
	/**
	 * Proxy port for Camel HTTP component.
	 */
	private String proxyPort;

	/**
	 * Proxy host getter.
	 * @return proxyHost
	 */
	public String getProxyHost() {
		return proxyHost;
	}

	/**
	 * Proxy host setter.
	 * @param proxyHost proxyHost
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	/**
	 * Proxy port getter.
	 * @return proxyPort
	 */
	public String getProxyPort() {
		return proxyPort;
	}

	/**
	 * Proxy port setter.
	 * @param proxyPort proxyPort
	 */
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * If Proxy is set, set it to GlobalOptions of CamelContext.
	 * @return CamelContextConfiguration instance.
	 */
	@Bean
	public CamelContextConfiguration contextConfiguration() {
		return new CamelContextConfiguration() {
			@Override
			public void beforeApplicationStart(CamelContext camelContext) {
				// LOG INFO
				logger.info("★beforeApplicationStart : GlobalOptions = {}", camelContext.getGlobalOptions());
				if(StringUtils.isNotBlank(getProxyHost()) && StringUtils.isNotBlank(getProxyPort())) {
					// FIXME CamelContextにセットすることで、HttpComponentに対しては有効であるが、
					// ・nonProxyHostsが考慮されない。
					// ・GeoCodeComponentはGeoCodeEndPointの設定に使っていない。
					 camelContext.getGlobalOptions().put("http.proxyHost", getProxyHost());
					 camelContext.getGlobalOptions().put("http.proxyPort", getProxyPort());
				}
			}

			@Override
			public void afterApplicationStart(CamelContext camelContext) {
				// LOG INFO
				logger.info("★afterApplicationStart : GlobalOptions = {}", camelContext.getGlobalOptions());
			}
		};
	}
}
