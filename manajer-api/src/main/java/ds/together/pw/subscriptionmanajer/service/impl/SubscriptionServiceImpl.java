package ds.together.pw.subscriptionmanajer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.alexpumpkin.reactorlock.concurrency.LockMono;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import ds.together.pw.subscriptionmanajer.config.SubscriptionProperties;
import ds.together.pw.subscriptionmanajer.entity.ProxyGroup;
import ds.together.pw.subscriptionmanajer.entity.Subscription;
import ds.together.pw.subscriptionmanajer.entity.SubscriptionResult;
import ds.together.pw.subscriptionmanajer.service.MaCacheService;
import ds.together.pw.subscriptionmanajer.service.SubscriptionService;
import ds.together.pw.subscriptionmanajer.util.Beans;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.Boolean.TRUE;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/5 15:49
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {
	public static final String TROJAN = "trojan://";
	public static final String SS = "ss://";
	public static final String VMESS = "vmess://";
	@Autowired
	private WebClient webClient;
	@Autowired
	@Qualifier("yamlMapper")
	private ObjectMapper yamlMapper;
	@Autowired
	private SubscriptionProperties subscriptionProperties;

	@Autowired
	private MaCacheService maCacheService;
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

	@Override
	public Mono<String> get(String token) {
		return LockMono.key(token).lock(doGet(token));
	}

	public Mono<String> doGet(String token) {
		if (!Objects.equals(subscriptionProperties.getToken(), token)) {
			throw new RuntimeException("Unsupported token");
		}
		return Flux.fromIterable(getSubscription()).flatMap(this::extractProxies).subscribeOn(Schedulers.parallel()).collectSortedList(
				Comparator.comparingInt(value -> value.getSubscription().getOrder())).map(results -> {
			Object o = this.combine(results);
			try {
				return yamlMapper.writeValueAsString(o);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});

	}

	private Object combine(List<SubscriptionResult> results) {
		ClassPathResource resource = new ClassPathResource("template.yaml");
		ObjectNode root;
		try {
			String template = resource.getContentAsString(StandardCharsets.UTF_8);
			root = yamlMapper.readValue(template, ObjectNode.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<ProxyGroup> autoProxyGroups = new ArrayList<>();
		List<ProxyGroup> selectProxyGroups = new ArrayList<>();
		for (SubscriptionResult result : results) {
			String groupName = result.getSubscription().getName();
			ArrayNode proxies = result.getProxies();
			if (proxies.isEmpty()) {
				//continue;
				proxies.add(this.getEmptyNode(groupName));
			}

			List<String> proxyNames = StreamSupport.stream(proxies.spliterator(), false)
												   .map(jsonNode -> jsonNode.get("name").asText())
												   .collect(Collectors.toList());

			ProxyGroup auto = new ProxyGroup();
			auto.setName("[" + groupName + "] AUTO");
			auto.setType("url-test");
			auto.setUrl("http://www.gstatic.com/generate_204");
			auto.setInterval(300);
			auto.setTolerance(50);
			auto.setProxies(proxyNames);
			autoProxyGroups.add(auto);

			ProxyGroup select = new ProxyGroup();
			select.setName("[" + groupName + "]");
			select.setType("select");
			ArrayList<String> selectProxies = new ArrayList<>();
			selectProxies.add(auto.getName());
			selectProxies.add("DIRECT");
			selectProxies.addAll(auto.getProxies());
			select.setProxies(selectProxies);
			selectProxyGroups.add(select);
		}

		List<String> autoNames = autoProxyGroups.stream().map(ProxyGroup::getName).toList();
		List<String> selectNames = selectProxyGroups.stream().map(ProxyGroup::getName).toList();

		ArrayNode allProxies = yamlMapper.createArrayNode();
		results.stream().map(SubscriptionResult::getProxies).forEach(allProxies::addAll);
		root.set("proxies", allProxies);

		List<ProxyGroup> proxyGroups = yamlMapper.convertValue(root.get("proxy-groups"), new TypeReference<>() {
		});
		for (ProxyGroup proxyGroup : proxyGroups) {
			List<String> list = proxyGroup.getProxies();
			if (ObjectUtils.isEmpty(list)) {
				continue;
			}
			if (list.remove("$$selectGroups$$")) {
				list.addAll(selectNames);
			}
			if (list.remove("$$autoGroups$$")) {
				list.addAll(autoNames);
			}
		}

		replaceGroups(proxyGroups, selectProxyGroups, "$$selectProxyGroups$$");
		replaceGroups(proxyGroups, autoProxyGroups, "$$autoProxyGroups$$");

		root.set("proxy-groups", new POJONode(proxyGroups));
		return root;
	}

	private void replaceGroups(List<ProxyGroup> proxyGroups, List<ProxyGroup> toInsert, String placeholder) {
		int i = Iterables.indexOf(proxyGroups, group -> Objects.equals(placeholder, group.getName()));
		if (i > 0) {
			proxyGroups.remove(i);
			proxyGroups.addAll(i, toInsert);
		} else {
			proxyGroups.addAll(toInsert);
		}
	}




	private JsonNode getEmptyNode(String groupName) {
		ObjectNode objectNode = yamlMapper.createObjectNode();

		objectNode.put("name", "[" + groupName + "] empty");
		objectNode.put("type", "ss");
		objectNode.put("server", "server");
		objectNode.put("port", 443);
		objectNode.put("password", "password");
		objectNode.put("cipher", "chacha20-ietf-poly1305");
		return objectNode;
	}


	private Mono<SubscriptionResult> extractProxies(Subscription subscription) {
		String url = subscription.getUrl();
		String name = subscription.getName();

		Mono<SubscriptionResult> loader = webClient.get()
												   .uri(URI.create(url))
												   .exchangeToMono(response -> response.bodyToMono(String.class))
												   .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)))//每隔2秒，尝试一次
												   .defaultIfEmpty("").onErrorResume(e -> {
					LOGGER.info("获取[{}],失败. {}", name, e.getMessage());
					return Mono.just("");
				}).map(str -> {
					SubscriptionResult result = new SubscriptionResult(subscription);
					ArrayNode proxies;
					try {
						proxies = getYamlProxies(str);
						LOGGER.info("[{}] is yaml config.", name);
					} catch (Exception e) {
						try {
							proxies = getBase64Proxies(name, str);
							LOGGER.info("[{}] is base64 config", name);
						} catch (Exception ex) {
							LOGGER.warn(e.getMessage());
							proxies = yamlMapper.createArrayNode();
						}
					}

					for (JsonNode proxy : proxies) {
						ObjectNode obj = (ObjectNode) proxy;
						String proxyName = obj.get("name").asText("");
						obj.set("name", new TextNode(MessageFormat.format("[{0}] {1}", name, proxyName)));
					}
					result.setProxies(proxies);
					return result;
				});


		return maCacheService.load(name + "@" + url, loader, new TypeReference<>() {
		});
	}

	private ArrayNode getBase64Proxies(String name, String str) {
		if (!BaseEncoding.base64().canDecode(str)) {
			throw new RuntimeException("Unknown config " + name);
		}
		String[] split = decodeBase64(str).orElse("").trim().split("\\r?\\n");
		ArrayNode arrayNode = yamlMapper.createArrayNode();
		Arrays.stream(split)
			  .filter(Objects::nonNull)
			  .filter(SubscriptionServiceImpl::isSupport)
			  .map(SubscriptionServiceImpl::parseProxyInfo)
			  .map(proxyInfo -> yamlMapper.convertValue(proxyInfo, JsonNode.class))
			  .forEach(arrayNode::add);

		return arrayNode;
	}

	private static boolean isSupport(String s) {
		if (ObjectUtils.isEmpty(s)) {
			return false;
		}
		return s.startsWith(SS) || s.startsWith(TROJAN) || s.startsWith(VMESS); //to support SSR+Vmess
	}

	private static Map<String, Object> parseProxyInfo(String uri) {
		if (uri.startsWith(SS)) {
			return getSSInfo(uri);
		} else if (uri.startsWith(TROJAN)) {
			return getTrojanInfo(uri);
		} else if (uri.startsWith(VMESS)) {
			return getVmessInfo(uri);
		}
		// shouldn't happen
		throw new UnsupportedOperationException("unable to parse uri [" + uri + "]");
	}

	private static Map<String, Object> getVmessInfo(String link) {
		Map<String, Object> result = Maps.newLinkedHashMap();
		try {
			String json = decodeBase64(link.replace(VMESS, "")).orElse(null);
			ObjectMapper mapper = new ObjectMapper();
			Map<Object, Object> config = mapper.readValue(json, new TypeReference<>() {
			});

			result.put("name", config.get("ps"));
			result.put("type", "vmess");
			result.put("server", config.get("add"));
			result.put("port", config.get("port"));
			result.put("cipher", "auto");
			result.put("uuid", config.get("id"));
			result.put("alterId", config.getOrDefault("aid", "0"));
			result.put("tls", "tls".equals(config.get("tls")));
			result.put("skip-cert-verify", true);
			Object network = config.get("net");
			if (Objects.equals(network, "ws")) {
				var wsOpts = Maps.newHashMap();
				var headers = Maps.newHashMap();
				wsOpts.put("path", config.getOrDefault("path", "/"));
				headers.put("host", config.get("host"));
				wsOpts.put("headers", headers);
				result.put("ws-opts", wsOpts);
			}
			if (!Objects.equals("tcp", network)) {
				result.put("network", network);
			}

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


	}

	private static Map<String, Object> getTrojanInfo(String uri) {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(uri).build();
		MultiValueMap<String, String> query = uriComponents.getQueryParams();
		boolean allowInsecure = Arrays.asList("1", "true").contains(query.getFirst("allowInsecure"));
		var sni = query.getFirst("sni");
		if (sni == null) {
			sni = query.getFirst("peer");
		}

		var result = Maps.<String, Object>newHashMap();
		result.put("type", "trojan");
		result.put("name", decodeUri(uriComponents.getFragment()));
		result.put("server", uriComponents.getHost());
		result.put("port", uriComponents.getPort());
		result.put("password", uriComponents.getUserInfo());
		if (sni != null) {
			result.put("sni", sni);
		}
		//result.put("udp", false);
		if (allowInsecure) {
			result.put("skip-cert-verify", true);
		}
		return result;
	}

	private static Map<String, Object> getSSInfo(String uri) {


		// 使用UriComponentsBuilder获取查询参数
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(uri).build();
		String decodedFragment = decodeUri(uriComponents.getFragment());
		String plugin = uriComponents.getQueryParams().getFirst("plugin");
		Map<String, Object> pluginInfo;
		if (plugin == null) {
			pluginInfo = Collections.emptyMap();
		} else {
			pluginInfo = Maps.newHashMap();
			Pattern pattern = Pattern.compile("^(.*?)=(.*?)$");
			for (String item : decodeUri(plugin).split(";")) {
				if (item.contains("=")) {
					Matcher matcher = pattern.matcher(item);
					if (matcher.matches()) {
						String key = matcher.group(1);
						String value = matcher.group(2);
						value = value.isBlank() ? "true" : value.trim();
						pluginInfo.put(key, value);
					} else {
						LOGGER.info("No match found , {}", item);
					}
				} else {
					pluginInfo.put(item.trim(), true);
				}
			}
		}

		HashMap<@Nullable String, @Nullable Object> map = Maps.newHashMap();
		map.put("type", "ss");
		map.put("name", decodedFragment);
		map.put("server", uriComponents.getHost());
		map.put("port", uriComponents.getPort());
		String userInfoStr = decodeBase64(uriComponents.getUserInfo()).orElse("");
		List<String> userInfo = Arrays.asList(userInfoStr.split(":"));

		map.put("cipher", Iterables.get((userInfo), 0, null));
		map.put("password", Iterables.get((userInfo), 1, null));


		Object obfs = pluginInfo.get("obfs");
		Object simpleObfs = pluginInfo.get("simple-obfs");
		Object v2rayPlugin = pluginInfo.get("v2ray-plugin");
		Object obfsHost = pluginInfo.get("obfs-host");
		Object obfsLocal = pluginInfo.get("obfs-local");

		if ((TRUE.equals(obfsLocal) || TRUE.equals(simpleObfs)) && Arrays.<Object>asList("tls", "http").contains(obfs)) {

			map.put("plugin", "obfs");
			HashMap<String, Object> pluginOpts = Maps.newHashMap();
			pluginOpts.put("mode", obfs);
			pluginOpts.put("host", obfsHost);

			map.put("plugin-opts", pluginOpts);
		} else if (TRUE.equals(v2rayPlugin)) {

			map.put("plugin", "v2ray-plugin");
			HashMap<String, Object> pluginOpts = Maps.newHashMap();
			pluginOpts.put("mode", "websocket");
			pluginOpts.put("tls", TRUE.equals(obfs));
			pluginOpts.put("skip-cert-verify", TRUE);
			pluginOpts.put("host", pluginInfo.get("host"));
			pluginOpts.put("path", "/");
			pluginOpts.put("mux", false);
			pluginOpts.put("headers", Maps.newHashMap());

			map.put("plugin-opts", pluginOpts);
		}

		return map;
	}

	private static Optional<String> decodeBase64(String str) {
		if (str == null) {
			return Optional.empty();
		}
		return Optional.of(new String(BaseEncoding.base64().decode(str)));
	}

	private static String decodeUri(String str) {
		if (str == null) {
			return null;
		}
		return UriUtils.decode(str, StandardCharsets.UTF_8);
	}


	private ArrayNode getYamlProxies(String str) {
		try {
			ArrayNode proxies;
			ObjectNode jsonNode = yamlMapper.readValue(str, ObjectNode.class);
			proxies = jsonNode.withArray("proxies");
			return proxies;
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Subscription> getSubscription() {

		List<Subscription> list = Beans.copyList(subscriptionProperties.getSubscriptions(), Subscription.class);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setOrder(i);
		}
		return list;
	}

}
