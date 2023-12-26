package com.smart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import org.springframework.context.annotation.Bean;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class Myconfig  {

	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
	

	// configure method
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
	{
		httpSecurity.csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(t -> t
                .requestMatchers(antMatcher("/user/**")).hasRole("USER")
                .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher("/**")).permitAll()
	        .anyRequest()
                .authenticated()
    )
	        .formLogin(form -> form
	    			.loginPage("/login")
	    			.loginProcessingUrl("/do_login")
	    			.defaultSuccessUrl("/user/index")
	    		);
	
		return httpSecurity.build();
	}
	
	}


