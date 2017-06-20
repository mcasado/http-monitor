package exercise;

import difflib.Chunk;
import exercise.util.ContentComparator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cache.CacheConstants;
import org.apache.camel.component.properties.PropertiesComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpMonitorRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMonitorRouteBuilder.class);

    private static final String NOTIFY_HEADER = "notify";
    private static final String PERCENTAGE_HEADER = "percentage";

    @Override
    public void configure() {

        // TODO set previous content from cache
        String previousContent = null;

        PropertiesComponent properties = new PropertiesComponent();
        properties.setLocation("classpath:config.properties");
        getContext().addComponent("properties", properties);

        Processor diffProcessor = new DiffProcessor(previousContent);

        from("httpPoller://{{url}}?delay={{delay}}&protocol={{protocol}}")
                .multicast().to("direct:cacheContentAdd", "direct:diff").end().choice()
                .when(header(NOTIFY_HEADER).isEqualTo(true)).setHeader("url", constant("{{url}}"))
                .multicast().to("direct:cacheContentUpdate", "direct:sendEmailNotification");

        from("direct:ContentCache").to("cache://ContentCache" +
                "?maxElementsInMemory=100" +
                "&eternal=true" +
                "&diskPersistent=true");

        from("direct:cacheContentAdd")
                // Prepare headers
                .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
                .setHeader(CacheConstants.CACHE_KEY, constant("content"))
                .to("direct:ContentCache")
                // Check if entry was not found
                .choice().when(header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNull())
                // If not found, get the content and put it to cache
                .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
                .setHeader(CacheConstants.CACHE_KEY, constant("content"))
                .to("direct:ContentCache")
                .otherwise().to("direct:noop")
                .to("direct:noop");

        from("direct:cacheContentUpdate")
                // Prepare headers
                .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_UPDATE))
                .setHeader(CacheConstants.CACHE_KEY, constant("content"))
                .to("direct:ContentCache");


        from("direct:diff").setHeader("diffThreshold", constant("{{diffThreshold}}")).process(diffProcessor);

        from("direct:noop").process(new NoOpProcessor());

        from("direct:sendEmailNotification").setHeader("subject", header("url").append(" content has changed ")
                .append(header(PERCENTAGE_HEADER)).append(constant("%"))).to("{{smtp_protocol}}://{{smtp}}?username={{user}}&password={{password}}&to={{to}}");
    }

    public class DiffProcessor implements Processor {

        private String previousContent = null;
        private double diffThreshold = 10;

        public DiffProcessor(String previousContent) {
            this.previousContent = previousContent;
        }

        @Override
        public void process(Exchange exchange) throws Exception {

            String content = exchange.getIn().getBody(String.class);
            diffThreshold = exchange.getIn().getHeader("diffThreshold", Double.class);

            if (previousContent == null) {
                previousContent = content;
            } else if (!previousContent.equals(content)) {
                LOGGER.info("There are changes");
                ContentComparator comparator = new ContentComparator(previousContent, content);

                for (Chunk chunk : comparator.getChangesFromOriginal()) {
                    LOGGER.debug("Changed\n" + chunk.getLines());
                }

                LOGGER.info(comparator.getInsertsFromOriginal().toString());
                for (Chunk chunk : comparator.getInsertsFromOriginal()) {
                    LOGGER.debug("Inserted\n" + chunk.getLines());
                }

                LOGGER.info(comparator.getDeletesFromOriginal().toString());
                for (Chunk chunk : comparator.getDeletesFromOriginal()) {
                    LOGGER.debug("Deleted\n" + chunk.getLines());
                }

                long changesCount = comparator.getChangesCount();
                double percentageChange = comparator.getChangesPercentage();

                LOGGER.info("Changes Count: " + changesCount);
                LOGGER.info("Percentage: " + percentageChange);

                if (percentageChange > diffThreshold) {
                    LOGGER.info("Content changes, " + percentageChange + "%, exceeded " + diffThreshold + " % threshold");
                    previousContent = content;
                    exchange.getIn().setHeader(NOTIFY_HEADER, true);
                    exchange.getIn().setHeader(PERCENTAGE_HEADER, percentageChange);
                }
            }
        }
    }

    private static class NoOpProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            // System.out.print("no op exchange: {}"); // exchange);
        }
    }

}
