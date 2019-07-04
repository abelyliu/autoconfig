package site.abely.autoconfig.cat;


import com.dianping.cat.Cat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 判断是否要启动cat的自动配置
 *
 * @author abely
 */
public class CatCondition implements Condition {

    private Logger logger = LoggerFactory.getLogger(Condition.class);
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment environment = conditionContext.getEnvironment();
        String applicationName = environment.getProperty("spring.application.name");
        String servers = environment.getProperty("cat.servers");
        //如果取不到项目名称则不进行
        if (StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(servers)) {

            return false;
        }

        String[] serverArray = servers.split(",");
        Cat.initializeByDomain(applicationName, 2280, 80, serverArray);
        boolean catEnabled = Cat.getManager().isCatEnabled();
        logger.info("cat 初始化=" + catEnabled);
        return catEnabled;

    }
}
