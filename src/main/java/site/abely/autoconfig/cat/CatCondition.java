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

    private boolean init = false;

    private Logger logger = LoggerFactory.getLogger(Condition.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment environment = conditionContext.getEnvironment();
        String applicationName = environment.getProperty("spring.application.name");
        String servers = environment.getProperty("cat.servers");
        //如果取不到项目名称则不进行
        if (StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(servers)) {
            logger.debug("CatCondition init fail,applicationName={},servers={}", applicationName, servers);
            return false;
        }
        //项目启动过程中，此方法会被调用多次，这里第一次进行初始化cat服务
        if (init) {
            return true;
        }

        String[] serverArray = servers.split(",");
        Cat.initializeByDomain(applicationName, 2280, 80, serverArray);
        boolean catEnabled = Cat.getManager().isCatEnabled();
        init = true;
        logger.debug("cat 初始化{}", catEnabled);
        return catEnabled;

    }
}
