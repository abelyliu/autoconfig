package site.abely.autoconfig.cat.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

/**
 * 这里拦截切面
 */
@Aspect
@Order(1)
public class FeignClientAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientAspect.class);

    @Pointcut("@within(org.springframework.cloud.openfeign.FeignClient)")
    public void feignClientPointcut() {
    }

    @Around("feignClientPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        LOGGER.info("feignclient begin");
        long start = System.currentTimeMillis();
        Object object = joinPoint.proceed();
        LOGGER.info("feignclient end ,耗时:{}", (System.currentTimeMillis() - start));
        return object;
    }
}
