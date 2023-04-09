package ds.together.pw.subscriptionmanajer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import ds.together.pw.subscriptionmanajer.config.SubscriptionProperties;
import ds.together.pw.subscriptionmanajer.entity.ProxyGroup;
import ds.together.pw.subscriptionmanajer.entity.Subscription;
import ds.together.pw.subscriptionmanajer.entity.SubscriptionResult;
import ds.together.pw.subscriptionmanajer.service.SubscriptionService;
import jakarta.annotation.Resource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.lang.Boolean.TRUE;

/**
 * @author zkq
 * @version 1.0
 * @date 2023/4/5 15:49
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    public static final String TROJAN = "trojan://";
    public static final String SS = "ss://";
    @Resource
    private WebClient webClient;
    @Resource
    private ObjectMapper yamlMapper;
    @Resource
    private SubscriptionProperties subscriptionProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    @Override
    public Mono<String> get(String token) {
        if (!Objects.equals(subscriptionProperties.getToken(), token)) {
            throw new RuntimeException("Unsupported token");
        }
        return Flux.fromIterable(getSubscription())
                   .flatMapSequential (this::extractProxies)
                   .collectList()
                   .map(results -> {
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
            List<String> proxies = StreamSupport.stream(result.getProxies().spliterator(), false)
                                                .map(jsonNode -> jsonNode.get("name").asText())
                                                .toList();
            if (ObjectUtils.isEmpty(proxies)) {
                continue;
            }

            ProxyGroup auto = new ProxyGroup();
            auto.setName("[" + groupName + "] 自动选择");
            auto.setType("url-test");
            auto.setUrl("http://www.gstatic.com/generate_204");
            auto.setInterval(300);
            auto.setTolerance(50);
            auto.setProxies(proxies);
            autoProxyGroups.add(auto);

            ProxyGroup select = new ProxyGroup();
            select.setName("[" + groupName + "] 节点选择");
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
        results.stream()
               .map(SubscriptionResult::getProxies)
               .forEach(allProxies::addAll);
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
        proxyGroups.addAll(autoProxyGroups);
        proxyGroups.addAll(selectProxyGroups);

        root.set("proxy-groups", new POJONode(proxyGroups));
        return root;
    }


    private Mono<SubscriptionResult> extractProxies(Subscription subscription) {
        String url = subscription.getUrl();
        String name = subscription.getName();
        return webClient.get()
                        .uri(url)
                        .exchangeToMono(response -> response.bodyToMono(String.class))
                        .map(str -> {
                            // TODO 这里的str如果是base64 则需要进行解析
                            SubscriptionResult result = new SubscriptionResult(subscription);
                            ArrayNode proxies;
                            try {
                                proxies = getYamlProxies(name, str);
                            } catch (Exception e) {
                                LOGGER.info("[{}] is not yaml config,try base64...", name);
                                proxies = getBase64Proxies(name, str);
                            }
                            result.setProxies(proxies);
                            return result;
                        });
    }

    private ArrayNode getBase64Proxies(String name, String str) {
        if (!BaseEncoding.base64().canDecode(str)) {
            throw new RuntimeException("Unknown config " + name);
        }
        String[] split = decodeBase64(str).trim().split("\\r?\\n");
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
        return s.startsWith(SS) || s.startsWith(TROJAN); //to support SSR+Vmess
    }

    private static Map<String, Object> parseProxyInfo(String uri) {
        if (uri.startsWith(SS)) {
            return getSSInfo(uri);
        }else if (uri.startsWith(TROJAN)){
            return getTrojanInfo(uri);
        }
        // shouldn't happen
        throw new UnsupportedOperationException("unable to parse uri [" + uri + "]");
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
        List<String> userInfo = Arrays.asList(decodeBase64(uriComponents.getUserInfo()).split(":"));

        map.put("cipher", Iterables.get((userInfo), 0, null));
        map.put("password", Iterables.get((userInfo), 1, null));


        //
        //nodeConfig.obfs && ['tls', 'http'].includes(nodeConfig.obfs)
        //        ? {
        //        plugin: 'obfs',
        //        'plugin-opts': {
        //            mode: nodeConfig.obfs,
        //            host: nodeConfig['obfs-host'],
        //},
        //        }
        //      : null
        //

        Object obfs = pluginInfo.get("obfs");
        Object simpleObfs = pluginInfo.get("simple-obfs");
        Object v2rayPlugin = pluginInfo.get("v2ray-plugin");
        Object obfsHost = pluginInfo.get("obfs-host");
        Object obfsLocal = pluginInfo.get("obfs-local");

        if ((TRUE.equals(obfsLocal) || TRUE.equals(simpleObfs))
                && Arrays.asList("tls", "http").contains(obfs)) {

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

    private static String decodeBase64(String str) {
        if (str == null) {
            return null;
        }
        return new String(BaseEncoding.base64().decode(str));
    }

    private static String decodeUri(String str) {
        if (str == null) {
            return null;
        }
        return UriUtils.decode(str, StandardCharsets.UTF_8);
    }


    private ArrayNode getYamlProxies(String name, String str) {
        try {
            ArrayNode proxies;
            ObjectNode jsonNode = yamlMapper.readValue(str, ObjectNode.class);
            proxies = jsonNode.withArray("proxies");
            for (JsonNode proxy : proxies) {
                ObjectNode obj = (ObjectNode) proxy;
                String proxyName = obj.get("name").asText("");
                obj.set("name", new TextNode(MessageFormat.format("[{0}] {1}", name, proxyName)));
            }
            return proxies;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Subscription> getSubscription() {
        return subscriptionProperties.getSubscriptions();
    }

}
