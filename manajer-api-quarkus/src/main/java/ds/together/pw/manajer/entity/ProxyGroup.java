package ds.together.pw.manajer.entity;

import java.util.List;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/5 17:23
 */
public class ProxyGroup {
    private String name;
    private String type;

    private List<String> proxies;

    private Integer tolerance;
    private Boolean lazy;

    // 'http://www.gstatic.com/generate_204'
    private String url;
    // 300
    private Integer interval;

    // consistent-hashing # or round-robin
    private String strategy;

    private String interfaceName;

    private Integer routingMark;

    private String use;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getProxies() {
        return proxies;
    }

    public void setProxies(List<String> proxies) {
        this.proxies = proxies;
    }

    public Integer getTolerance() {
        return tolerance;
    }

    public void setTolerance(Integer tolerance) {
        this.tolerance = tolerance;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Integer getRoutingMark() {
        return routingMark;
    }

    public void setRoutingMark(Integer routingMark) {
        this.routingMark = routingMark;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }
}
