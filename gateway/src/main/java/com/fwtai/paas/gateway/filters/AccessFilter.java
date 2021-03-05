package com.fwtai.paas.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 鉴权过滤器,全局的路由网关
 */
//@Component
public class AccessFilter implements GlobalFilter,Ordered {

    private final Logger logger = LoggerFactory.getLogger(AccessFilter.class);

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange,final GatewayFilterChain chain){
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();
        final String uri = request.getURI().getPath();
        if (request.getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }
        final String token = exchange.getRequest().getQueryParams().getFirst("token");
        if(token == null || token.isEmpty())
            return reply401(response);
        if(uri.contains("/user/login")){//放行
            return chain.filter(exchange);//继续执行下一个过滤器
        }
        final String accessTokenUrl = request.getQueryParams().getFirst("accessToken");
        final String accessTokenHeader = request.getHeaders().getFirst("accessToken");
        if((accessTokenUrl != null && accessTokenUrl.length() > 0 ) || (accessTokenHeader != null && accessTokenHeader.length() > 0)){
            System.out.println("鉴权|权限验证全局过滤器被执行,优先于全局过滤器,优先于网关过滤器,因为order的值越小优先权越大");
            return chain.filter(exchange);//继续执行下一个过滤器
        }else{
            logger.warn("accessToken 为null哦");
            return reply401(response);
        }
    }

    @Override
    public int getOrder(){
        return 0;//数字越小优先权越大
    }

    private Mono<Void> reply401(final ServerHttpResponse response){
        response.getHeaders().add("Content-Type","text/html;charset=utf-8");
        final String msg = "{\"code\":401,\"msg\":\"没有操作权限\"}";
        final DataBuffer db = response.bufferFactory().wrap(msg.getBytes());
        return response.writeWith(Mono.just(db));
    }

    //还可以重定向获取请求的资源
    private Mono<Void> replyRedirect(final ServerHttpResponse response){
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set(HttpHeaders.LOCATION,"http://yinlz.com");
        return response.setComplete();
    }
}