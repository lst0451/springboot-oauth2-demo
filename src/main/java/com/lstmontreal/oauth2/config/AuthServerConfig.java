package com.lstmontreal.oauth2.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * Created by lishitao
 */
@Configuration
public class AuthServerConfig {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/api/**").hasAuthority("ADMIN")
                    //.access("hasRole('ADMIN')")
//                    .antMatchers("/api/**").access("#oauth2.hasRole('ADMIN')")
                    .antMatchers("/order/**").authenticated();
        }
    }


    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        AuthenticationManager authenticationManager;
        @Autowired
        RedisConnectionFactory redisConnectionFactory;
        
        @Autowired(required = false)
        private TokenStore inMemoryTokenStore;
        
        @Autowired
        UserDetailsService userDetailsService;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory().withClient("client_1")
                    .authorizedGrantTypes("client_credentials")
                    .scopes("read")
                    .authorities("oauth2")
                    .secret("123456")
                    .and()
                    .withClient("client_2")
                    .authorizedGrantTypes("password", "refresh_token")
                    .scopes("read")
                    .authorities("oauth2")
                    .secret("123456")
//                    .accessTokenValiditySeconds(60*10)
//                    .refreshTokenValiditySeconds(60*60*24)
                    ;
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
//                  .tokenStore(new RedisTokenStore(redisConnectionFactory))//用redis存储token
                    .tokenStore(inMemoryTokenStore)//用内存存储token
                    .authenticationManager(authenticationManager)
                    .userDetailsService(userDetailsService)
                    .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);

            endpoints.reuseRefreshTokens(true);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.allowFormAuthenticationForClients();
        }

    }

}
