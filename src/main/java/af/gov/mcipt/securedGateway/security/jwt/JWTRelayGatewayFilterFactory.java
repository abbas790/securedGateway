package af.gov.mcipt.securedGateway.security.jwt;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
@Component
public class JWTRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<JWTRelayGatewayFilterFactory.Config> {
     private static final String BEARER = "Bearer ";

    private JwtDecoder jwtDecoder;
    public static class Config {
        // Configuration properties can be added here if needed
    }
    public JWTRelayGatewayFilterFactory(JwtDecoder jwtDecoder) {
        super(Config.class);
        this.jwtDecoder = jwtDecoder;
    }
 
   

    @Override
    public GatewayFilter apply(Config config) {
        
         return (exchange, chain) -> {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
        if (bearerToken == null) {
            // Allow anonymous requests.
            return chain.filter(exchange);
        }
        String token = this.extractToken(bearerToken);
        return Mono.fromCallable(() -> jwtDecoder.decode(token))
                .flatMap(jwt -> {
                    ServerWebExchange modifiedExchange = withBearerAuth(exchange, token);
                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(throwable -> {
                    // Handle JWT decoding errors here
                    return Mono.error(throwable);
                });
    };
    }
    private String extractToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.length() > 7 && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Invalid token in Authorization header");
    }

    private ServerWebExchange withBearerAuth(ServerWebExchange exchange, String authorizeToken) {
        return exchange.mutate().request(r -> r.headers(headers -> headers.setBearerAuth(authorizeToken))).build();
    }
    
    
}
