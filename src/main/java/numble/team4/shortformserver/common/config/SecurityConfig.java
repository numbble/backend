package numble.team4.shortformserver.common.config;

import static numble.team4.shortformserver.member.member.domain.Role.ADMIN;
import static numble.team4.shortformserver.member.member.domain.Role.MEMBER;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import numble.team4.shortformserver.common.filter.JwtAuthenticationFilter;
import numble.team4.shortformserver.common.filter.JwtExceptionHandleFilter;
import numble.team4.shortformserver.member.auth.util.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .httpBasic().disable()
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtExceptionHandleFilter(objectMapper), JwtAuthenticationFilter.class)
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET,
                "/v1/videos/{videoId}",
                "/v1/videos",
                "/v1/videos/{videoId}",
                "/v1/videos/search-condition",
                "/vi/videos/status-condition",
                "/v1/categories",
                "/v1/users/following/from",
                "/v1/users/following/to",
                "/v1/users/{memberId}/videos",
                "/v1/users/{memberId}/likes",
                "/v1/users/{memberId}").permitAll()
            .antMatchers(HttpMethod.POST,
                "/v1/users/following/{followId}",
                "/v1/users/email",
                "/v1/videos/{videoId}/likes").hasAnyRole(MEMBER.name(), ADMIN.name())
            .antMatchers(HttpMethod.PUT,
                "/v1/users/image",
                "/v1/users/name",
                "/v1/videos/{videoId}").hasAnyRole(MEMBER.name(), ADMIN.name())
            .antMatchers(HttpMethod.DELETE,
                "/v1/videos/{videoId}",
                "/v1/videos/likes/{likesId}",
                "/v1/users/following/{toMemberId}").hasAnyRole(MEMBER.name(), ADMIN.name())
            .antMatchers(
                "/login/oauth2/code/**",
                "/oauth/**",
                "/ws-connection/**",
                "/renew",
                "/v1/users/email/auth").permitAll()
            .antMatchers("/v1/admin/videos").hasAnyRole(ADMIN.name())
            .antMatchers(HttpMethod.GET, "/v1/users").hasRole(ADMIN.name())
            .antMatchers(HttpMethod.DELETE, "/v1/users/**").hasRole(ADMIN.name())
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
