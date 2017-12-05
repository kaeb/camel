package org.apache.camel.example.springboot.geocoder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.geocoder.GeoCoderComponent;
import org.apache.camel.component.geocoder.GeoCoderEndpoint;
import org.apache.camel.component.geocoder.springboot.GeoCoderComponentAutoConfiguration;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.http.springboot.HttpComponentConfiguration;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.geocoder.Geocoder;

/**
 * HTTPリクエスト時、nonProxyHostsを判断した上でProxy設定を行う為のConfigurationクラス。
 * @author w2327eng
 *
 */
@Configuration
@ConfigurationProperties(prefix = "camel.component.http.ext")
public class ProxySettingConfig {
	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Proxy host for Camel Http component.
	 */
	private String proxyHost;
	/**
	 * Proxy port for Camel Http component.
	 */
	private Integer proxyPort;
	/**
	 * non-Proxy hosts for Camel Http component.
	 */
	private String nonProxyHosts;

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
	 * non-Proxy hosts getter.
	 * @return nonProxyHosts
	 */
	public String getNonProxyHosts() {
		return nonProxyHosts;
	}

	/**
	 * non-Proxy hosts setter.
	 * @param nonProxyHosts non-Proxy hosts
	 */
	public void setNonProxyHosts(String nonProxyHosts) {
		this.nonProxyHosts = nonProxyHosts;
	}

	/**
	 * {@linkplain HttpComponent}を、Proxy設定を踏まえて生成する。
	 * @param camelContext
	 * @param configuration
	 * @return
	 * @throws Exception
	 */
	@Bean(name = { "http-component", "https-component" })
	public HttpComponent configureHttpComponent(
			final CamelContext camelContext,
			final HttpComponentConfiguration configuration) throws Exception {
		final HttpComponent component = new HttpComponent(HttpEndPointExt.class) {
			/*
			 * (非 Javadoc)
			 * @see org.apache.camel.component.http.HttpComponent#createHttpEndpoint(java.lang.String, org.apache.camel.component.http.HttpComponent, org.apache.commons.httpclient.params.HttpClientParams, org.apache.commons.httpclient.HttpConnectionManager, org.apache.camel.component.http.HttpClientConfigurer)
			 */
			@Override
			protected HttpEndpoint createHttpEndpoint(
					final String uri, final HttpComponent component,
					final HttpClientParams clientParams, final HttpConnectionManager connectionManager,
					final HttpClientConfigurer configurer) throws URISyntaxException {
				// FIXME せっかくEndPointクラスをセットしているのに、ここでNewされるので拡張する。
				return new HttpEndPointExt(uri, component, clientParams, connectionManager, configurer);
			}
		};
		component.setCamelContext(camelContext);
		final Map<String, Object> parameters = new HashMap<>();
		IntrospectionSupport.getProperties(configuration, parameters, null, false);
		for (final Map.Entry<String, Object> entry : parameters.entrySet()) {
			final Object value = entry.getValue();
			final Class<?> paramClass = value.getClass();
			if (paramClass.getName().endsWith("NestedConfiguration")) {
				try {
					final Class<?> nestedClass = (Class<?>)paramClass.getDeclaredField("CAMEL_NESTED_CLASS").get(null);
					final Map<String, Object> nestedParameters = new HashMap<>();
					IntrospectionSupport.getProperties(value, nestedParameters, null, false);
					final Object nestedProperty = nestedClass.newInstance();
					IntrospectionSupport.setProperties(
							camelContext,
							camelContext.getTypeConverter(),
							nestedProperty,
							nestedParameters);
					entry.setValue(nestedProperty);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		}
		IntrospectionSupport.setProperties(camelContext, camelContext.getTypeConverter(), component, parameters);
		return component;
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
			 * @see org.apache.camel.component.geocoder.GeoCoderComponent#createEndpoint(java.lang.String, java.lang.String, java.util.Map)
			 */
			@Override
			protected Endpoint createEndpoint(
					final String uri,
					final String remaining,
					final Map<String, Object> parameters) throws Exception {
				final GeoCoderEndpoint endPoint = (GeoCoderEndpoint)super.createEndpoint(uri, remaining, parameters);
				if(StringUtils.isNotBlank(proxyHost) && proxyPort != null) {
					if(!uri.contains("proxyHost") && !uri.contains("proxyPort")) {
						// uriは、"/geocoder"なので、Geocoderから実際のホストを取得して判断
						if(needProxy("https://" + Geocoder.getGeocoderHost())) {
							// LOG INFO
							logger.info("Setting proxy for GeoCoderEndpoint. host = {}, port = {}", proxyHost, proxyPort);
							endPoint.setProxyHost(proxyHost);
							endPoint.setProxyPort(proxyPort);
						}
					} else {
						// LOG INFO
						logger.info("GeoCoderEndpoint already set proxy. uri = {}", uri);
					}
				}
				return endPoint;
			}
		};
		component.setCamelContext(camelContext);
		return component;
	}

	/**
	 * 指定されたURIエンドポイントと{@linkplain #nonProxyHosts}を確認し、Proxy有無を返す。
	 * @param endPointUri URIエンドポイント
	 * @return Proxyが必要な場合、true
	 */
	private boolean needProxy(final String endPointUri) {
		boolean need = true;
		try{
			final String endPointHost = new URL(endPointUri).getHost();
			final Pattern nphPattern = getNonProxyPattern();
			if(nphPattern != null) {
				if(nphPattern.matcher(endPointHost).matches()) {
					// LOG INFO
					logger.info("EndPointUri match! nonProxyHost = {}, endPointUri = {}", nonProxyHosts, endPointUri);
					need = false;
				}
			}
		} catch (MalformedURLException e) {
			// LOG WARN
			logger.warn("Invalid EndPointUri {}", endPointUri, e);
		}
		return need;
	}

	private Pattern nonProxyHostsPattern;

	/**
	 * 以下から拝借。
	 * @see https://stackoverflow.com/questions/17615300/valid-regex-for-http-nonproxyhosts
	 * @param nonProxyHosts
	 * @return
	 */
	private Pattern getNonProxyPattern() {
		if(nonProxyHostsPattern != null) return nonProxyHostsPattern;
		if (StringUtils.isBlank(nonProxyHosts)) return null;

		// "*.fedora-commons.org" -> ".*?\.fedora-commons\.org"
		String _nonProxyHosts = nonProxyHosts.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*?");

		// a|b|*.c -> (a)|(b)|(.*?\.c)
		_nonProxyHosts = "(" + _nonProxyHosts.replaceAll("\\|", ")|(") + ")";

		try {
			nonProxyHostsPattern = Pattern.compile(_nonProxyHosts);
		} catch (Exception e) {
			logger.error("Creating the nonProxyHosts pattern failed for http.nonProxyHosts=" + nonProxyHosts
					+ " with the following exception: " + e);
		}
		return nonProxyHostsPattern;
	}

	/**
	 * リクエストのエンドポイントに対し、Proxyが必要な場合にProxy設定をしたHttpClientを生成する。
	 * @author w2327eng
	 *
	 */
	class HttpEndPointExt extends HttpEndpoint {
		/**
		 * Logger
		 */
		private final Logger logger = LoggerFactory.getLogger(getClass());

		/**
		 * @see org.apache.camel.component.http.HttpEndpoint#HttpEndpoint(String, HttpComponent, HttpClientParams, HttpConnectionManager, HttpClientConfigurer)
		 */
		public HttpEndPointExt(String endPointURI, HttpComponent component, HttpClientParams clientParams,
				HttpConnectionManager httpConnectionManager, HttpClientConfigurer clientConfigurer)
				throws URISyntaxException {
			super(endPointURI, component, clientParams, httpConnectionManager, clientConfigurer);
		}

		/**
		 * 生成された{@linkplain HttpClient}にProxy設定が行われていない場合、エンドポイントに対し
		 * Proxyが必要かを判定の上、必要な場合はProxyをセットする。
		 * @see org.apache.camel.component.http.HttpEndpoint#createHttpClient()
		 */
		@Override
		public HttpClient createHttpClient() {
			final HttpClient httpClient = super.createHttpClient();
			if(StringUtils.isNotBlank(proxyHost) && proxyPort != null) {
				if(StringUtils.isBlank(httpClient.getHostConfiguration().getProxyHost())) {
					if(needProxy(getEndpointUri())) {
						// LOG INFO
						logger.info("Setting proxy for EndPoint : {}", getEndpointUri());
						httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
					}
				} else {
					// LOG INFO
					logger.info("EndPoint '{}' already set proxy. proxyHost = {}, proxyPort = {}"
							, getEndpointUri()
							, httpClient.getHostConfiguration().getProxyHost()
							, httpClient.getHostConfiguration().getProxyPort());
				}
			}
			return httpClient;
		}
	}
}
