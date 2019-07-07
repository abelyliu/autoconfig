package site.abely.autoconfig.cat.status;

import com.dianping.cat.status.StatusExtension;

import java.util.HashMap;
import java.util.Map;

public class JedisStatus implements StatusExtension {

    private redis.clients.util.Pool jedisPool;

    public void setJedisPool(redis.clients.util.Pool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public String getDescription() {
        return "jedis状态监控";
    }

    @Override
    public String getId() {
        return "jedis";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();
        int numActive = jedisPool.getNumActive();
        int numWaiters = jedisPool.getNumWaiters();

        map.put("活跃连接数量", String.valueOf(numActive));
        map.put("等待连接数量", String.valueOf(numWaiters));
        return map;
    }
}
