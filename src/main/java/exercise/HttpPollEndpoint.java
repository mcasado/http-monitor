package exercise;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

@UriEndpoint(scheme="httpPoller", title="HttpPoller", syntax="httpPoller://url", consumerOnly=true, consumerClass=HttpPollConsumer.class)
public class HttpPollEndpoint extends DefaultPollingEndpoint {

	public HttpPollEndpoint(String uri, String url, HttpPollComponent component) {
		super(uri, component);
		this.url = url;
	}

	private String url;

	@UriParam
	private HttpPollConfiguration configuration;

	public Producer createProducer() throws Exception {
		throw new UnsupportedOperationException("HttpPollProducer is not implemented");
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
        HttpPollConsumer consumer = new HttpPollConsumer(this, processor);
        return consumer;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HttpPollConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(HttpPollConfiguration configuration) {
		this.configuration = configuration;
	}

}