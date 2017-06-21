# HTTP MONITOR
This an application that polls a URL at a certain interval and sends an email notification to someone if more than X% of the content on the page has changed. 

It has been implemented with Java and Apache Camel routes and created a custom component for the HTTP poller.

The current implementation has the following restrictions;

* Use the Myers diff algorithm (com.googlecode.java-diff-utils) to find the differences between page contents 
*   

### TODOs

* Improve calculation of % of change. 
* Set latest persisted URL content when starting the app from the cache
* Add unit tests for the new component
* Improve route tests

### Build and Test It

```
$ cd http-monitor
$ mvn clean install
```

### Run It

Change some of the config.properties like email information ad/or URL to monitor 

```
$ cd http-monitor
$ mvn camel:run
```


