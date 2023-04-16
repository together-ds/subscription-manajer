package ds.together.pw.subscriptionmanajer.entity;

/**
 * @author zkq
 * @version 1.0
 * @date 2023/4/5 18:37
 */
public class Subscription {
    private String name;
    private String url;

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
}
