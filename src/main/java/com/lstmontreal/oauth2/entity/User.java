package com.lstmontreal.oauth2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
	private String userId;
	private String lastName;
	private int gender;
}
