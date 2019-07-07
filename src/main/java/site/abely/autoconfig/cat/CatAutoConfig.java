package site.abely.autoconfig.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;
import com.dianping.cat.status.StatusExtensionRegister;
import feign.Client;
import feign.CustomClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.util.Pool;
import site.abely.autoconfig.cat.bean.post.processor.CatMybatisBeanPostProcessor;
import site.abely.autoconfig.cat.condition.CatCondition;
import site.abely.autoconfig.cat.interceptor.CatSpringDataRedisPlugin;
import site.abely.autoconfig.cat.status.JedisStatus;
import site.abely.autoconfig.cat.status.ThreadPoolTaskExecutorStatus;
import site.abely.autoconfig.cat.status.TomcatConnectionStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author abely
 */
@Configuration
@ConditionalOnClass(Cat.class)
@ConditionalOnProperty(prefix = "cat", name = "enabled", havingValue = "true")
@Conditional(CatCondition.class)
public class CatAutoConfig {

    private Logger logger = LoggerFactory.getLogger(Condition.class);

    @Autowired
    private List<ThreadPoolTaskExecutor> threadPoolTaskExecutors;


    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    /**
     * mybatis耗时监控
     *
     * @return
     */
    @Bean
    public static BeanPostProcessor myBatisPostProcessorConfigurer() {
        return new CatMybatisBeanPostProcessor();
    }

    /**
     * url监控
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean catFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        CatFilter filter = new CatFilter();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("cat-filter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public CatSpringDataRedisPlugin catSpringDataRedisPlugin() {
        return new CatSpringDataRedisPlugin();
    }

    @Bean
    @ConditionalOnMissingClass({"ApacheHttpClient.class", "OkHttpClient.class"})
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                              SpringClientFactory clientFactory) {
        return new LoadBalancerFeignClient(new CustomClient(null, null), cachingFactory,
                clientFactory);
    }

    @EventListener(value = {ContextRefreshedEvent.class})
    public void afterRefreshed() throws NoSuchFieldException, IllegalAccessException {
        threadPoolMonitor();
        redisMonitor();
    }

    //这里强依赖了jedis的版本
    private void redisMonitor() throws NoSuchFieldException, IllegalAccessException {
        Class<? extends JedisConnectionFactory> aClass = JedisConnectionFactory.class;
        Field pool = aClass.getDeclaredField("pool");
        pool.setAccessible(true);
        Pool o = (Pool) pool.get(jedisConnectionFactory);
        JedisStatus jedisStatus = new JedisStatus();
        jedisStatus.setJedisPool(o);
        StatusExtensionRegister.getInstance().register(jedisStatus);
    }

    /**
     * 监控自定义线程池
     */
    private void threadPoolMonitor() {
        ThreadPoolTaskExecutorStatus threadPoolTaskExecutorStatus = new ThreadPoolTaskExecutorStatus();
        threadPoolTaskExecutorStatus.setThreadPoolTaskExecutors(threadPoolTaskExecutors);
        StatusExtensionRegister.getInstance().register(threadPoolTaskExecutorStatus);
    }

    /**
     * tomcat 线程池监控
     *
     * @param event
     */
    @EventListener(value = {WebServerInitializedEvent.class})
    public void tomcatConnectionStatus(WebServerInitializedEvent event) {
        WebServer webServer = event.getWebServer();
        if (!(webServer instanceof TomcatWebServer)) {
            return;
        }
        TomcatWebServer tomcatWebServer = (TomcatWebServer) event.getWebServer();

        Executor executor = tomcatWebServer.getTomcat().getConnector().getProtocolHandler().getExecutor();
        if (!(executor instanceof ThreadPoolExecutor)) {
            return;
        }
        ThreadPoolExecutor httpThreadPool = (ThreadPoolExecutor) executor;
        TomcatConnectionStatus tomcatConnectionStatus = new TomcatConnectionStatus();
        tomcatConnectionStatus.setHttpThreadPool(httpThreadPool);
        StatusExtensionRegister.getInstance().register(tomcatConnectionStatus);
    }


}
