package security;

import FrenchGuide.config.SecurityConfig;

import java.time.Instant;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = {SecurityConfig.class, SecurityConfigTests.TestConfig.class})
@TestPropertySource(properties = {
		"app.security.jwt.secret=dGVzdC1vbmx5LXNlY3JldC1rZXktMzItYnl0ZXMtbG9uZy0xMjM0NTY=",
		"app.security.jwt.issuer=test-issuer",
		"app.security.jwt.audience=test-api",
		"app.security.cors.allowed-origins=http://localhost:5173"
})
class SecurityConfigTests {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private SecurityConfig securityConfig;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void protectedEndpointRequiresBearerToken() throws Exception {
		mockMvc.perform(get("/api/test/protected")).andExpect(status().isUnauthorized());
	}

	@Test
	void validTokenAllowsAccessWithoutCreatingSession() throws Exception {
		var result = mockMvc.perform(get("/api/test/protected")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token(validClaims())))
				.andExpect(status().isOk()).andReturn();
		assertNull(result.getRequest().getSession(false));
	}

	@Test
	void invalidSignatureIsRejected() throws Exception {
		JwtEncoder otherEncoder = NimbusJwtEncoder.withSecretKey(
				new SecretKeySpec(new byte[32], "HmacSHA256")).build();
		String otherToken = otherEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), validClaims().build())).getTokenValue();
		mockMvc.perform(get("/api/test/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void expiredTokenIsRejected() throws Exception {
		String expired = token(validClaims().issuedAt(Instant.now().minusSeconds(600))
				.expiresAt(Instant.now().minusSeconds(300)));
		mockMvc.perform(get("/api/test/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void wrongIssuerIsRejected() throws Exception {
		mockMvc.perform(get("/api/test/protected").header(HttpHeaders.AUTHORIZATION,
				"Bearer " + token(validClaims().issuer("untrusted-issuer"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void wrongAudienceIsRejected() throws Exception {
		mockMvc.perform(get("/api/test/protected").header(HttpHeaders.AUTHORIZATION,
				"Bearer " + token(validClaims().audience(List.of("other-api")))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenWithoutExpirationIsRejected() throws Exception {
		JwtClaimsSet.Builder claims = JwtClaimsSet.builder().issuer("test-issuer")
				.subject("test-user").audience(List.of("test-api")).issuedAt(Instant.now());
		mockMvc.perform(get("/api/test/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + token(claims)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void onlyLoginAndRegistrationPostsArePublic() throws Exception {
		mockMvc.perform(post("/api/auth/login")).andExpect(status().isOk());
		mockMvc.perform(post("/api/auth/register")).andExpect(status().isOk());
		mockMvc.perform(get("/api/auth/login")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/admin")).andExpect(status().isUnauthorized());
	}

	@Test
	void configuredOriginCanSendPreflightWithoutToken() throws Exception {
		mockMvc.perform(options("/api/test/protected")
				.header(HttpHeaders.ORIGIN, "http://localhost:5173")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
	}

	@Test
	void unconfiguredOriginIsRejected() throws Exception {
		mockMvc.perform(options("/api/test/protected")
				.header(HttpHeaders.ORIGIN, "https://untrusted.example")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
				.andExpect(status().isForbidden());
	}

	@Test
	void weakOrInvalidSigningSecretsAreRejected() {
		assertThrows(IllegalStateException.class, () -> securityConfig.jwtSigningKey("not-base64!"));
		assertThrows(IllegalStateException.class, () -> securityConfig.jwtSigningKey("c2hvcnQ="));
	}

	private JwtClaimsSet.Builder validClaims() {
		return JwtClaimsSet.builder().issuer("test-issuer").subject("test-user").audience(List.of("test-api"))
				.issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300));
	}

	private String token(JwtClaimsSet.Builder claims) {
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
				.getTokenValue();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableWebMvc
	static class TestConfig {

		@Bean
		ProbeController probeController() {
			return new ProbeController();
		}
	}

	@RestController
	static class ProbeController {

		@GetMapping("/api/test/protected")
		String protectedEndpoint() {
			return "ok";
		}

		@PostMapping({"/api/auth/login", "/api/auth/register"})
		String authEndpoint() {
			return "ok";
		}
	}
}
