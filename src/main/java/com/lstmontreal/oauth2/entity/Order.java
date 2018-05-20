package com.lstmontreal.oauth2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	private String orderId;
	private String customerId;
	private String orderDesc;
}
