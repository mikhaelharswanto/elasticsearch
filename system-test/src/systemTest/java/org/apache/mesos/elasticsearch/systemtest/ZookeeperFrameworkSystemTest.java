package org.apache.mesos.elasticsearch.systemtest;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import com.containersol.minimesos.MesosCluster;
import com.containersol.minimesos.mesos.MesosClusterConfig;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * System tests which verifies configuring a separate Zookeeper cluster for the framework.
 */
@SuppressWarnings({"PMD.AvoidUsingHardCodedIP"})
public class ZookeeperFrameworkSystemTest {

    private static final Logger LOGGER = Logger.getLogger(ZookeeperFrameworkSystemTest.class);
    protected static final Configuration TEST_CONFIG = new Configuration();

    @Rule
    public final MesosCluster CLUSTER = new MesosCluster(
        MesosClusterConfig.builder()
            .slaveResources(new String[]{"ports(*):[9200-9200,9300-9300]", "ports(*):[9201-9201,9301-9301]", "ports(*):[9202-9202,9302-9302]"})
            .build()
    );

    private ElasticsearchSchedulerContainer scheduler;
    private ZookeeperContainer zookeeper;

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            CLUSTER.stop();
            scheduler.remove();
        }
    };

    @Before
    public void startScheduler() throws Exception {
        LOGGER.info("Starting Elasticsearch scheduler");

        zookeeper = new ZookeeperContainer(CLUSTER.getConfig().dockerClient);
        CLUSTER.addAndStartContainer(zookeeper);

        scheduler = new ElasticsearchSchedulerContainer(CLUSTER.getConfig().dockerClient, CLUSTER.getZkContainer().getIpAddress());

        LOGGER.info("Started Elasticsearch scheduler on " + scheduler.getIpAddress() + ":" + TEST_CONFIG.getSchedulerGuiPort());
    }

    @Test
    public void testZookeeperFramework() throws UnirestException {
        scheduler.setZookeeperFrameworkUrl("zk://" + zookeeper.getIpAddress() + ":2181");
        CLUSTER.addAndStartContainer(scheduler);

        TasksResponse tasksResponse = new TasksResponse(scheduler.getIpAddress(), CLUSTER.getConfig().getNumberOfSlaves());

        List<JSONObject> tasks = tasksResponse.getTasks();

        ElasticsearchNodesResponse nodesResponse = new ElasticsearchNodesResponse(tasks, CLUSTER.getConfig().getNumberOfSlaves());
        assertTrue("Elasticsearch nodes did not discover each other within 5 minutes", nodesResponse.isDiscoverySuccessful());

        ElasticsearchZookeeperResponse elasticsearchZookeeperResponse = new ElasticsearchZookeeperResponse(tasks.get(0).getString("http_address"));
        assertEquals("zk://" + zookeeper.getIpAddress() + ":2181", elasticsearchZookeeperResponse.getHost());
    }

    @Test
    public void testZookeeperFramework_differentPath() throws UnirestException {
        scheduler.setZookeeperFrameworkUrl("zk://" + zookeeper.getIpAddress() + ":2181/framework");
        CLUSTER.addAndStartContainer(scheduler);

        TasksResponse tasksResponse = new TasksResponse(scheduler.getIpAddress(), CLUSTER.getConfig().getNumberOfSlaves());

        List<JSONObject> tasks = tasksResponse.getTasks();

        ElasticsearchNodesResponse nodesResponse = new ElasticsearchNodesResponse(tasks, CLUSTER.getConfig().getNumberOfSlaves());
        assertTrue("Elasticsearch nodes did not discover each other within 5 minutes", nodesResponse.isDiscoverySuccessful());

        ElasticsearchZookeeperResponse elasticsearchZookeeperResponse = new ElasticsearchZookeeperResponse(tasks.get(0).getString("http_address"));
        assertEquals("zk://" + zookeeper.getIpAddress() + ":2181", elasticsearchZookeeperResponse.getHost());
    }

}
