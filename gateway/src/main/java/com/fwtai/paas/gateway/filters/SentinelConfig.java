package com.fwtai.paas.gateway.filters;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 整合alibaba的sentinel限流规则配置+Sentinel限流自定义异常提示,整合sentinel仅仅是做限流,鉴权可以用spring cloud gateway 自带的全局过滤器或网关过滤器
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-16 17:38
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
@Configuration//Sentinel可以单独的使用,也可以一起和Sentinel独立8719服务端口一起使用
public class SentinelConfig{

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    //构造器
    public SentinelConfig(final ObjectProvider<List<ViewResolver>> listObjectProvider,final ServerCodecConfigurer serverCodecConfigurer){
        this.viewResolvers = listObjectProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    //限流异常处理器
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler(){
        //注册到 spring cloud gateway
        return new SentinelGatewayBlockExceptionHandler(viewResolvers,serverCodecConfigurer);
    }

    //限流过滤器
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGlobalFilter(){
        return new SentinelGatewayFilter();
    }

    //spring 容器初始化的时候执行
    @PostConstruct
    public void doInit(){
        initGatewayRules();
        initBlockHandler();
    }

    //Sentinel限流自定义异常提示
    protected void initBlockHandler(){
        final BlockRequestHandler blockRequestHandler = new BlockRequestHandler(){
            @Override
            public Mono<ServerResponse> handleRequest(final ServerWebExchange exchange,final Throwable throwable){
                final ServerHttpRequest request = exchange.getRequest();
                final URI uri = request.getURI();
                final String router = uri.getPath();
                final String json = "{\"code\":199,\"router\":\""+router+"\",\"msg\":\"访问人数过多\"}";
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).contentType(new MediaType("text","html",StandardCharsets.UTF_8)).body(BodyInserters.fromValue(json));
            }
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }

    //定义网关限流规则
    public void initGatewayRules(){
        final Set<GatewayFlowRule> rules = new HashSet<GatewayFlowRule>(2);
        //单一的限流(不是分组限流!),其中第1个resource参数是资源名称,可以是网关中的router名称或自定义的api分组名称;count是限流阀值;intervalSec是统计时间窗口,单位是s秒,默认是1秒
        //rules.add(new GatewayFlowRule("order-service").setCount(3).setIntervalSec(60));
        //rules.add(new GatewayFlowRule("product-service").setCount(3).setIntervalSec(60));
        //分组限流
        rules.add(new GatewayFlowRule("consumer").setCount(3).setIntervalSec(60));//订单
        rules.add(new GatewayFlowRule("provider").setCount(3).setIntervalSec(60));//商品
        //加载网关限流规则
        GatewayRuleManager.loadRules(rules);
        //加载'限流分组'
        initCustomizedApis();
    }

    //限流分组
    protected void initCustomizedApis(){
        final Set<ApiDefinition> apiDefinitions = new HashSet<ApiDefinition>(2);
        //provider 组
        final ApiDefinition apiProduct = new ApiDefinition("provider").setPredicateItems(
            new HashSet<ApiPredicateItem>(){
                {
                    //匹配/producr-service/product 以及其子路径的所有请求 http://127.0.0.1:9000/provider/yinlz.com?token=w
                    add(new ApiPathPredicateItem().setPattern("/provider/message").setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                    //匹配/product 以及其子路径的所有请求 http://127.0.0.1:9000/provider/yinlz.com?token=w
                    add(new ApiPathPredicateItem().setPattern("/provider/getName").setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }
            });
        //consumer 组
        final ApiDefinition apiOrder = new ApiDefinition("consumer").setPredicateItems(
            new HashSet<ApiPredicateItem>(){
                {
                    //仅匹配/order-service/order/index  仅对 -> http://127.0.0.1:9000/consumer/message 限流;http://127.0.0.1:9001/order-service/order/1 这个不被限流
                    add(new ApiPathPredicateItem().setPattern("/consumer/**"));
                }
            });
        apiDefinitions.add(apiProduct);
        apiDefinitions.add(apiOrder);
        GatewayApiDefinitionManager.loadApiDefinitions(apiDefinitions);
    }
}