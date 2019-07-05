package site.abely.autoconfig.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;
import com.dianping.cat.status.StatusExtensionRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

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

    /**
     * 监控mybatis耗时
     *
     * @return
     */
    @Bean
    public static BeanPostProcessor myBatisPostProcessorConfigurer() {
        return new CatMybatisBeanPostProcessor();
    }

    /**
     * 配置url监控
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

    /**
     * tomcat 线程池获取
     *
     * @param event
     */
    @EventListener(value = {WebServerInitializedEvent.class})
    public void tomcatConectionStatus(WebServerInitializedEvent event) {
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
        TomcatConectionStatus tomcatConectionStatus = new TomcatConectionStatus();
        tomcatConectionStatus.setHttpThreadPool(httpThreadPool);
        StatusExtensionRegister.getInstance().register(tomcatConectionStatus);
    }


}
