package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.token.JwtBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20RequestParameterResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDC")
@SuppressWarnings("JavaUtilDate")
public class DefaultOAuth20RequestParameterResolverTests extends AbstractOidcTests {
    @Test
    public void verifySignedJwtWithClientId() {
        val registeredService = getOidcRegisteredService("client");
        servicesManager.save(registeredService);

        val payload = JwtBuilder.JwtRequest.builder()
            .registeredService(Optional.of(registeredService))
            .issuer("https://cas.example.org")
            .jwtId(UUID.randomUUID().toString())
            .subject("casuser")
            .issueDate(new Date())
            .attributes(Map.of(OAuth20Constants.SCOPE, List.of("openid"),
                OAuth20Constants.REDIRECT_URI, List.of("https://apereo.github.io"),
                OAuth20Constants.RESPONSE_TYPE, List.of("code"),
                OAuth20Constants.CLIENT_ID, List.of(registeredService.getClientId())))
            .build();

        val jwtString = oidcAccessTokenJwtBuilder.build(payload);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, jwtString);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val scope = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.SCOPE, String.class);
        assertFalse(scope.isEmpty());
        assertTrue(scope.get().contains("openid"));
    }
}
