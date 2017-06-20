package exercise;


import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("exercise")
public class Main extends CamelConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        org.apache.camel.spring.javaconfig.Main main = new org.apache.camel.spring.javaconfig.Main();
        main.setBasedPackages("exercise");
      //  main.enableTrace();
        main.run(args);
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        return new SpringCamelContext(getApplicationContext());
    }

    @Override
    protected void setupCamelContext(CamelContext camelContext) throws Exception {
        camelContext.setTracing(false);
        SpringCamelContext springCamelContext = (SpringCamelContext) camelContext;
    }

    public void afterPropertiesSet() throws Exception {
    }

}