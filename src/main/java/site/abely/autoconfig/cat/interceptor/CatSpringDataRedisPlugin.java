package site.abely.autoconfig.cat.interceptor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import site.abely.autoconfig.cat.condition.CatCondition;

@Conditional(CatCondition.class)
@Aspect
public class CatSpringDataRedisPlugin {

    private static Object interceptorRedisMethods(MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        //忽略Object基类中的方法
        if (methodName.equals("toString") || methodName.equals("hashCode") || methodName.equals("equals")) {
            return invocation.proceed();
        }

        Transaction transaction;
//        if ("get".equals(methodName)) {
//            transaction = Cat.newTransaction("Cache.dataRedis", methodName + ":" + methodName);
//        } else {
            transaction = Cat.newTransaction("Cache.dataRedis", methodName);
//        }
        try {
            Object value = invocation.proceed();
            if (value == null && "get".equals(methodName)) {
                Cat.logEvent("Cache.dataRedis", methodName + ":missed");
            }
            transaction.setStatus(Message.SUCCESS);
            return value;
        } catch (Throwable e) {
            Cat.logError(e);
            transaction.setStatus(e);
            throw e;
        } finally {
            transaction.complete();
        }
    }

    @Around("execution(* org.springframework.data.redis.connection.jedis.JedisConnectionFactory.getConnection(..))")
    public Object springDataRedisJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        if ("getConnection".equals(methodName)) {
            try {
                JedisConnection connection = (JedisConnection) joinPoint.proceed();

                ProxyFactory factory = new ProxyFactory();
                factory.setTarget(connection);
                //factory.setTargetClass(JedisConnection.class);
                factory.addAdvice(new JedisConnectionMethodInterceptor());
                JedisConnection newConnection = (JedisConnection) factory.getProxy();//getClass().getClassLoader()

                return newConnection;
            } catch (Throwable e) {
                //失败才记录
                Transaction transaction = Cat.newTransaction("Cache.dataRedis", methodName);
                Cat.logError(e);
                transaction.setStatus(e);
                transaction.complete();
                throw e;
            }
        }


        return joinPoint.proceed();
    }

    /**
     * {@link MethodInterceptor}
     */
    private static class JedisConnectionMethodInterceptor implements MethodInterceptor {

        JedisConnectionMethodInterceptor() {
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            String methodName = invocation.getMethod().getName();

            if (methodName.equals("isPipelined") || methodName.equals("openPipeline") || methodName.equals("isQueueing")
                    || methodName.equals("isClosed")
                    || methodName.equals("close")
                    || methodName.equals("closePipeline")) {
                return invocation.proceed();
            } else if (methodName.equals("getNativeConnection")) {
                Object nativeConnection = invocation.proceed();

                ProxyFactory factory = new ProxyFactory();
                factory.setTarget(nativeConnection);
                //factory.setTargetClass(JedisConnection.class);
                factory.addAdvice(new JedisMethodInterceptor());
                Object newNativeConnection = factory.getProxy();//getClass().getClassLoader()

                return newNativeConnection;
            }

            return interceptorRedisMethods(invocation);
        }

    }

    /**
     * {@link MethodInterceptor}
     */
    private static class JedisMethodInterceptor implements MethodInterceptor {

        JedisMethodInterceptor() {
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return interceptorRedisMethods(invocation);
        }

    }
}
