package org.springframework.cloud.openfeign;

import feign.Client;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Objects;

public class FeignFactoryProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FeignClientFactoryBean) {
            ProxyFactory factory = new ProxyFactory();
            factory.setTarget(bean);
            factory.addAdvice(new FeignFactoryMethodInterceptor());
            FeignClientFactoryBean newConnection = (FeignClientFactoryBean) factory.getProxy();//getClass().getClassLoader()

            return newConnection;
        }
        return bean;
    }

    private static class FeignFactoryMethodInterceptor implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            String methodName = methodInvocation.getMethod().getName();
            Object proceed = methodInvocation.proceed();
            if (!Objects.equals(methodName, "getTarget")) {
                return proceed;
            }

            if (!(proceed instanceof Client)) {
                return proceed;
            }
            ProxyFactory factory = new ProxyFactory();
            factory.setTarget(proceed);
            factory.addAdvice(new ClientMethodInterceptor());
            Client client = (Client) factory.getProxy();
            return client;
        }
    }

    private static class ClientMethodInterceptor implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            String methodName = methodInvocation.getMethod().getName();
            Object proceed;
            if (Objects.equals(methodName, "execute")) {
                System.out.println("befor exe");
                proceed = methodInvocation.proceed();
                System.out.println("end exe");
            } else {
                proceed = methodInvocation.proceed();
            }
            return proceed;
        }
    }
}
