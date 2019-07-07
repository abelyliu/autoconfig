package site.abely.autoconfig.cat.status;

import com.dianping.cat.status.StatusExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控sprig线程池的连接情况
 *
 * @author abely
 */
public class ThreadPoolTaskExecutorStatus implements StatusExtension {
    private List<ThreadPoolTaskExecutor> threadPoolTaskExecutors;

    public void setThreadPoolTaskExecutors(List<ThreadPoolTaskExecutor> threadPoolTaskExecutors) {
        this.threadPoolTaskExecutors = threadPoolTaskExecutors;
    }

    @Override
    public String getDescription() {
        return "自定义线程池使用情况";
    }

    @Override
    public String getId() {
        return "ThreadPoolTaskExecutor";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();
        threadPoolTaskExecutors.forEach(o -> {
            String threadNamePrefix = o.getThreadNamePrefix();
            map.put(threadNamePrefix + "(coreSize" + o.getCorePoolSize() + ",maxSize" + o.getMaxPoolSize() + ")", String.valueOf(o.getActiveCount()));
        });
        return map;
    }
}
