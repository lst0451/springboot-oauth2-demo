## Springboot + OAuth2 进行分级权限控制的Demo

系统使用Maven构建一个 Springboot 应用， 由于只是用来演示 OAuth2 与 Spring 集成， 功能比较简单。 没有UI 界面。 权限控制描述：

模拟三个用户：

 	1. user_1 和 user_2，分配“USER”权限。
 	2. admin, 分配 ”ADMIN“ 和 ”USER“ 权限。

由于是演示系统，在代码中对用户进行硬编码，正常情况应该从数据库中读取用户数据，在此不进行演示。

```java

    @Bean
    @Override
    protected UserDetailsService userDetailsService(){
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user_1").password("123456").authorities("USER").build());
        manager.createUser(User.withUsername("user_2").password("123456").authorities("USER").build());
        manager.createUser(User.withUsername("admin").password("123456").roles("ADMIN","USER").authorities("ADMIN").build());
        return manager;
    }
```

在配置类中创建一个 Bean， 这里采用 InMemoryUserDetailsManager 的方式，创建模拟的三个用户并分配权限。

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.requestMatchers().anyRequest()
        .and()
        .authorizeRequests()
        .antMatchers("/oauth/**").permitAll();
}
```
这个配置用来展示 oauth2 自带的页面，如果不配置这段，所有oauth2的提示信息会以 json 格式返回，而不是定位到 oauth2 预定义的HTML页面。

```java
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/api/**").hasAuthority("ADMIN")
                .antMatchers("/order/**").authenticated();
    }
```
这一段用来对访问进行权限控制，，所有调用 /order/ 的访问必须要带有认证服务器返回的access_code, 所有被转发到 /api/ 的访问，还必须要有 ADMIN 权限。

```java
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
                ;
    }
```
此段用来配置两个客户端，client_1 用来进行 client 认证，client_2 用来进行password认证。



下面对权限分级进行访问测试：

启动Application之后，使用 postman 进行测试：

首先对 /api/user/{id} 请求进行测试，该资源需要access_code并且拥有 ADMIN 权限：

访问：

http://localhost:8080/oauth/token?username=admin&password=123456&grant_type=password&scope=read&client_id=client_2&client_secret=123456 

返回：

```json
{
    "access_token": "013e635a-38c8-488a-bf3d-f30ae429d572",
    "token_type": "bearer",
    "refresh_token": "41b4f807-3907-4bf8-b7af-9051841b1855",
    "expires_in": 43199,
    "scope": "read"
}
```

此URL请求以password认证方式对认证系统申请access_tocken，返回的 "access_token" 将会用于后面对系统中受保护资源的访问。其中 refresh_token 用于刷新申请新的 access_token。

访问：

http://localhost:8080/oauth/token?grant_type=client_credentials&scope=read&client_id=client_1&client_secret=123456 

返回：

```json
{
    "access_token": "a033dadf-746b-4a70-b1b4-265160aa425e",
    "token_type": "bearer",
    "expires_in": 43199,
    "scope": "read"
}
```

此URL请求以password认证方式对认证系统申请access_tocken，返回的 "access_token" 也可用于后面对系统中受保护资源的访问。需要注意此 access_token 是client端的，而不是属于用户的。如果用于访问拥有特殊用户权限如admin 用户才可以访问的资源，此access_token将会被显示为未授权。

现在用刚刚password模式申请到的admin用户的access_code访问受保护资源 /api/user/2 :

http://localhost:8080/api/user/2?access_token=013e635a-38c8-488a-bf3d-f30ae429d572 

返回：

```json
{
    "userId": "2",
    "lastName": "Li Shitao",
    "gender": 1
}
```

如果用客户端模式返回的access_token访问：

http://localhost:8080/api/user/2?access_token=a033dadf-746b-4a70-b1b4-265160aa425e 

则会返回：

```json
{
    "error": "access_denied",
    "error_description": "Access is denied"
}
```

同理，如果使用的是只有"USER"权限的用户access_token来访问/api/**的资源，也会得到如上信息，因为该资源被配置为要求拥有ADMIN权限才可以访问。

但是如果使用过期的或者错误的access_token访问该资源，则会返回"invalid_token"提示：

```json
{
    "error": "invalid_token",
    "error_description": "Invalid access token: a033dadf-746b-4a70-b1b4-265160aa4444"
}
```



下面对 /order/{id} 资源进行访问测试，该资源只需要拥有 access_token 即可访问：

为了得到access_token，可以用上面章节的password模式获取access_token的方式，分别获得普通用户和admin用户的 access_token 进行测试（由于admin用户的access_token一定可以访问没有要求拥有“ADMIN”权限的资源，在这里不进行重复）：

http://localhost:8080/oauth/token?username=user_1&password=123456&grant_type=password&scope=read&client_id=client_2&client_secret=123456 

返回：

```json
{
    "access_token": "8ff27102-f6ac-4cef-bb1a-307527268560",
    "token_type": "bearer",
    "refresh_token": "c94035cd-05ab-4624-820b-4c3f2128fd6e",
    "expires_in": 43172,
    "scope": "read"
}
```

使用该access_token对受保护的/order/1 进行访问：

http://localhost:8080/order/1?access_token=8ff27102-f6ac-4cef-bb1a-307527268560 

返回：

```json
{
    "orderId": "1",
    "customerId": "new-custom",
    "orderDesc": "Demo Order"
}
```

如果不使用 ?access_token参数直接访问: http://localhost:8080/order/1 

则返回未授权信息：

```json
{
    "error": "unauthorized",
    "error_description": "Full authentication is required to access this resource"
}
```

如果使用过期或者错误的access_token访问，则会返回 invalid_token 的错误提示。



下面对于未受保护的资源 /product/1 进行访问，则不需要在 URL 中附加 access_token，但是如果使用错误的 access_token 则依然会返回未授权或者非法token的提示。如果是正确的 access_token 则也可以正确访问。

http://localhost:8080/product/1 

返回：

```json
{
    "productId": "1",
    "productName": "Demo Product",
    "price": 999
}

```



最后测试一下刷新 access_token 

http://localhost:8080/oauth/token?grant_type=refresh_token&refresh_token=cc2078fa-5698-4230-9996-0dca9e9663b6&client_id=client_2&client_secret=123456 

替换掉参数中的使用 password 模式申请到的 refresh_token 即可得到新的 access_token 和 refresh_token 。注意 client_id 参数，这里必须是 password 模式的客户端，不能用 client 模式的客户端 id ，用 client 模式申请返回的数据中也并不包括 refresh_token。

总结：

使用OAuth2可以对访问用户进行访问权限分级设定，在生产环境中需要针对具体业务进行设计。