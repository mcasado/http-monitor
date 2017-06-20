package exercise;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;


public class HttpPollConsumer extends ScheduledPollConsumer {

    private HttpPollEndpoint endpoint;

    public HttpPollConsumer(HttpPollEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.setDelay(endpoint.getConfiguration().getDelay());
    }

    @Override
    protected int poll() throws Exception {

        String protocol = endpoint.getConfiguration().getProtocol();
        URL url = new URL(protocol, endpoint.getUrl(), "");

        return processSearchFeeds(url);
    }

    private String performGetRequest(String url) throws ClientProtocolException, IOException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() != 200)
            throw new RuntimeException("Error with return code: " + response.getStatusLine().getStatusCode());

        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    private int processSearchFeeds(URL url) throws Exception {


        String response = performGetRequest(url.toString());

        Exchange exchange = getEndpoint().createExchange();
        exchange.getIn().setBody(response, String.class);
        getProcessor().process(exchange);

        return 1;
    }

}