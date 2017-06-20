package exercise;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

public class HttpPollComponent extends UriEndpointComponent {

	public HttpPollComponent() {
		super(HttpPollEndpoint.class);
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {

        HttpPollEndpoint endpoint = new HttpPollEndpoint(uri, remaining, this);
		HttpPollConfiguration configuration = new HttpPollConfiguration();

		// use the built-in setProperties method to clean the camel parameters map
		setProperties(configuration, parameters);

		endpoint.setConfiguration(configuration);
		return endpoint;
	}
}