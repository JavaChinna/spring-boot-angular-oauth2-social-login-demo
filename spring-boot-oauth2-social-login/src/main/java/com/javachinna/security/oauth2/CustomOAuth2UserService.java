package com.javachinna.security.oauth2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.javachinna.dto.SocialProvider;
import com.javachinna.exception.OAuth2AuthenticationProcessingException;
import com.javachinna.service.UserService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private UserService userService;

	@Autowired
	private Environment env;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
		try {
			Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
			String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId();
			if (provider.equals(SocialProvider.LINKEDIN.getProviderType())) {
				populateEmailAddressFromLinkedIn(oAuth2UserRequest, attributes);
			}
			return userService.processUserRegistration(provider, attributes, null, null);
		} catch (AuthenticationException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			// Throwing an instance of AuthenticationException will trigger the
			// OAuth2AuthenticationFailureHandler
			throw new OAuth2AuthenticationProcessingException(ex.getMessage(), ex.getCause());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateEmailAddressFromLinkedIn(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) throws OAuth2AuthenticationException {
		String emailEndpointUri = env.getProperty("linkedin.email-address-uri");
		Assert.notNull(emailEndpointUri, "LinkedIn email address end point required");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2UserRequest.getAccessToken().getTokenValue());
		HttpEntity<?> entity = new HttpEntity<>("", headers);
		ResponseEntity<Map> response = restTemplate.exchange(emailEndpointUri, HttpMethod.GET, entity, Map.class);
		List<?> list = (List<?>) response.getBody().get("elements");
		Map map = (Map<?, ?>) ((Map<?, ?>) list.get(0)).get("handle~");
		attributes.putAll(map);
	}
}