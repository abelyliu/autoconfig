package site.abely.autoconfig.cat.interceptor;

import feign.Request;
import feign.Response;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.StringWriter;

@Aspect
public class FeignClientAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientAspect.class);

    @Pointcut("execution(* org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.execute(..))")
//    @Pointcut("execution(* feign.Client.execute(..))")
    public void feignClientPointcut() {
    }

    @Around("feignClientPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("eeeeeeee");
        LOGGER.info("feignclient begin");
        long start = System.currentTimeMillis();
        Object object = joinPoint.getTarget();

        if (!(object instanceof LoadBalancerFeignClient)) {
            LOGGER.info("feignclient not LoadBalancerFeignClient");
            return joinPoint.proceed();
        }
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            if (obj instanceof Request) {
                Request request = (Request) obj;
                LOGGER.info("feignclient request url:{}", request.url());
            }
        }
        Object result = joinPoint.proceed();
        if (result instanceof Response) {
            Response response = (Response) result;

            StringWriter writer = new StringWriter();
            String theString = writer.toString();

            LOGGER.info("feignclient response body:{}", theString);
        }
        LOGGER.info("feignclient end ,耗时:{}", (System.currentTimeMillis() - start));
        return result;
    }
}
