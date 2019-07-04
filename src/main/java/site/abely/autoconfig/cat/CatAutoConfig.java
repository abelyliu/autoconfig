package site.abely.autoconfig.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

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

}
