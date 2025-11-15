package com.example.groceries_jwt_project.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.groceries_jwt_project.entity.User;
 
//this is the main heart of JWT mechanism
//Implements spring security's user details - wraps the user entity
//covert db user into security user details object used by the authentication
//system
public class MyUserDetails implements UserDetails{
	private static final long serialVersionUID = 1L;
	private User user;
	public MyUserDetails(User user) {
		this.user = user;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(){
		return List.of(new SimpleGrantedAuthority(user.getRole()));
	}
	@Override
	public String getPassword() {
		return user.getPassword();
	}
	@Override
	public String  getUsername() {
		return user.getUsername();
	}
	
}
 
 