package site.abely.autoconfig.cat.bean.post.processor;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import site.abely.autoconfig.cat.interceptor.CatMybatisInterceptor;

/**
 * 因为mybatis plus使用了自动配置sessionFactory，所以这里用后处理器的方式提供
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
        String url = environment.getProperty("spring.datasource.druid.url");
        sessionFactory.getConfiguration().addInterceptor(new CatMybatisInterceptor(url));
        return sessionFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
