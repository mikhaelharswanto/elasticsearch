package org.apache.mesos.elasticsearch.scheduler.controllers;

import org.apache.http.client.HttpClient;
import org.apache.mesos.elasticsearch.common.cli.ZookeeperCLIParameter;
import org.apache.mesos.elasticsearch.scheduler.ElasticsearchScheduler;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration for controllers
 */
@EnableAutoConfiguration
@ComponentScan
@Configuration
public class TestConfiguration {

    @Bean
    public ClusterController getClusterController() {
        return new ClusterController();
    }

    @Bean
    public ElasticsearchScheduler getMockScheduler() {
        return Mockito.mock(ElasticsearchScheduler.class);
    }

    @Bean
    public org.apache.mesos.elasticsearch.scheduler.Configuration getConfig() {
        return new org.apache.mesos.elasticsearch.scheduler.Configuration(ZookeeperCLIParameter.ZOOKEEPER_MESOS_URL, "zk://dummy.mesos.master:2181");
    }

    @Bean
    public HttpClient getMockHttpClient() {
        return Mockito.mock(HttpClient.class);
    }
}
