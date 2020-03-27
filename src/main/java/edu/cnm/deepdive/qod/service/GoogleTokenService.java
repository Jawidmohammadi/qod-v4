package edu.cnm.deepdive.qod.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import edu.cnm.deepdive.qod.model.entity.User;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenService implements ResourceServerTokenServices {

  private static final String VERIFICATION_FAILURE =
      "Provided token could not be verified; check for stale credentials.";
  private static final String ROLE_PATTERN = "ROLE_%s";

  private final String clientId;
  private final AccessTokenConverter converter;
  private final UserService userService;

  @Autowired
  public GoogleTokenService(
      @Value("${oauth.clientId}") String clientId,
      UserService userService) {
    this.clientId = clientId;
    this.userService = userService;
    converter = new DefaultAccessTokenConverter();
  }

  @Override
  public OAuth2Authentication loadAuthentication(String token)
      throws AuthenticationException, InvalidTokenException {
    HttpTransport transport  = new NetHttpTransport();
    JacksonFactory jsonFactory = new JacksonFactory();
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setAudience(Collections.singletonList(clientId))
        .build();
    GoogleIdToken idToken = null;
    try {
      idToken = verifier.verify(token);
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException(e);
    }
    if (idToken != null) {
      Payload payload = idToken.getPayload();
      String oauthKey = payload.getSubject();
      String displayName = (String) payload.get("name");
      // TODO Extract any additional info from payload.
      User user = userService.getOrCreate(oauthKey, displayName);
      Collection<GrantedAuthority> grants = Collections.singleton(
          new SimpleGrantedAuthority(String.format(ROLE_PATTERN, user.getRole())));
      Authentication base = new UsernamePasswordAuthenticationToken(user, token, grants);
      OAuth2Request request = converter.extractAuthentication(payload).getOAuth2Request();
      return new OAuth2Authentication(request, base);
    } else {
      throw new InvalidTokenException(VERIFICATION_FAILURE);
    }
  }

  @Override
  public OAuth2AccessToken readAccessToken(String s) {
    return null;
  }

}
