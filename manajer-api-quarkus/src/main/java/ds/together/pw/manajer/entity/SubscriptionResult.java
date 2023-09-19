package ds.together.pw.manajer.entity;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/5 18:51
 */
public class SubscriptionResult {
	//private Subscription subscription;
	private ArrayNode proxies;


	public ArrayNode getProxies() {
		return proxies;
	}

	public void setProxies(ArrayNode proxies) {
		this.proxies = proxies;
	}
}
