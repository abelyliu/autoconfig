package site.abely.autoconfig.cat;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * 因为SqlSessionFactory可能被覆盖，所以用后处理器的方式提供
 *
 * @author abely
 */
public class CatMybatisBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof SqlSessionFactory)) {
            return bean;
        }
        SqlSessionFactory sessionFactory = (SqlSessionFactory) bean;
        String url = environment.getProperty("spring.datasource.url");
        if (StringUtils.isEmpty(url)) {
            //这里兼容一下druid数据源的url
            url = environment.getProperty("spring.datasource.druid.url");
        }
        sessionFactory.getConfiguration().addInterceptor(new CatMybatisInterceptor(url));
        return sessionFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
