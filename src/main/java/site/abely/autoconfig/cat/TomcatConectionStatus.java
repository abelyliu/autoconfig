package site.abely.autoconfig.cat;

import com.dianping.cat.status.StatusExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * tomcat连接数获取
 *
 * @author abely
 */
public class TomcatConectionStatus implements StatusExtension {
    private ThreadPoolExecutor httpThreadPool;

    public void setHttpThreadPool(ThreadPoolExecutor httpThreadPool) {
        this.httpThreadPool = httpThreadPool;
    }

    @Override
    public String getDescription() {
        return "Tomcat线程池监控";
    }

    @Override
    public String getId() {
        return "tomcat_thread_pool";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> maps = new HashMap<>();

        if (httpThreadPool != null) {
            maps.put("Tomcat激活连接数(核心线程数:" + httpThreadPool.getCorePoolSize() + ",最大线程数:" + httpThreadPool.getMaximumPoolSize() + ")", String.valueOf(httpThreadPool.getActiveCount()));
        }

        return maps;
    }
}
