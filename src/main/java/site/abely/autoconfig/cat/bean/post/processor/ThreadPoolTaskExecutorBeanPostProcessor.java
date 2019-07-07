package site.abely.autoconfig.cat.bean.post.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 给spring的线程池注入名称
 *
 * @author abely
 */
public class ThreadPoolTaskExecutorBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor bean1 = (ThreadPoolTaskExecutor) bean;
            bean1.setBeanName(beanName);
            return bean1;
        }
        return bean;
    }
}
