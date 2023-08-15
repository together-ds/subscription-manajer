package ds.together.pw.subscriptionmanajer.entity;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/5 18:37
 */
public class Subscription {
    private String name;
    private String url;
    private int order;

    public Subscription() {
    }

    public Subscription(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
