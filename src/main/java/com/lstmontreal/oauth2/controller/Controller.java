package com.lstmontreal.oauth2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lstmontreal.oauth2.entity.Order;
import com.lstmontreal.oauth2.entity.Product;
import com.lstmontreal.oauth2.entity.User;

/**
 * Created by lishitao
 */
@RestController
public class Controller {

    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable String id) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new Product(id, "Demo Product", 999);
    }

    @GetMapping("/order/{id}")
    public Order getOrder(@PathVariable String id) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        authentication.getAuthorities().stream().forEach(ga->System.out.println(ga.getAuthority()));
        return new Order(id, "new-custom", "Demo Order");
    }
    
    @GetMapping("/api/user/{id}")  
    public User get(@PathVariable String id) {  
    	return new User(id, "Li Shitao", 1);
    }

}
