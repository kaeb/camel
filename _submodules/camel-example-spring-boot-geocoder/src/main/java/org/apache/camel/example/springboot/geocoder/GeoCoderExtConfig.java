package org.apache.camel.example.springboot.geocoder;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.geocoder.GeoCoderComponent;
import org.apache.camel.component.geocoder.GeoCoderEndpoint;
import org.apache.camel.component.geocoder.springboot.GeoCoderComponentAutoConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Geocoder normally sets Proxy as a parameter to Route, but it is a setting class for replacing it with application settings.
 * @author w2327eng
 *
 */
//@Configuration
//@ConfigurationProperties(prefix = "camel.component.geocoder.ext")
public class GeoCoderExtConfig {
	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Proxy host for Camel Geocoder component.
	 */
	private String proxyHost;
	/**
	 * Proxy port for Camel Geocoder component.
	 */
	private Integer proxyPort;

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
	public Integer getProxyPort() {
		return proxyPort;
	}

	/**
	 * Proxy port setter.
	 * @param proxyPort proxyPort
	 */
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * If settings exist, create a {@linkplain GeoCoderComponent} with proxy settings
	 * <br>This method override {@linkplain GeoCoderComponentAutoConfiguration#configureGeoCoderComponent(CamelContext)} method.
	 * @param camelContext Apache Camel Context instance.
	 * @return GeocoderComponent instance with already setting up proxies;
	 */
	@Bean(name = "geocoder-component")
	public GeoCoderComponent configureGeoCoderComponent(CamelContext camelContext) {
		final GeoCoderComponent component = new GeoCoderComponent(){
			/**
			 * If Proxy is not set with parameters for Route, Proxy of application setting is set as a parameter of Route.
			 * @param uri Endpoint URI.
			 * @param remaining
			 * @param parameters
			 * @throws Exception
			 */
			@Override
			protected Endpoint createEndpoint(
					final String uri,
					final String remaining,
					final Map<String, Object> parameters) throws Exception {
				final GeoCoderEndpoint endPoint = (GeoCoderEndpoint)super.createEndpoint(uri, remaining, parameters);
				if(StringUtils.isBlank((String)parameters.get("proxyHost")) && StringUtils.isBlank((String)parameters.get("proxyPort"))) {
					if(StringUtils.isNotBlank(proxyHost) && proxyPort != null) {
						endPoint.setProxyHost(proxyHost);
						endPoint.setProxyPort(proxyPort);
						// LOG INFO
						logger.info("Setting proxy for GeoCoderEndpoint. host = {}, port = {}", proxyHost, proxyPort);
					}
				}
				return endPoint;
			}
		};
		component.setCamelContext(camelContext);
		return component;
	}
}
