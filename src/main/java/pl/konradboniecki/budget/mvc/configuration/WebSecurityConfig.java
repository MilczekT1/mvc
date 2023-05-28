package pl.konradboniecki.budget.mvc.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pl.konradboniecki.budget.mvc.service.CustomAuthenticationProvider;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@ConditionalOnWebApplication
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Scope(scopeName = SCOPE_SINGLETON)
public class WebSecurityConfig {

    @Autowired
    private CustomAuthenticationProvider authProvider;

    @Value("${budget.baseUrl.gateway}")
    private String gatewayUrl;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/prometheus",
                                "/login",
                                "/logout",
                                "/register",
                                "/budget/family/*/addMember/*",
                                "/resources/*")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin()
                    .loginPage(gatewayUrl + "/login")
                    .loginProcessingUrl("/authenticate")
                    .successForwardUrl("/")
                    .permitAll()
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl(gatewayUrl + "/login?logout")
                    .permitAll()
                    .and()
                .exceptionHandling()
                    .accessDeniedPage("/403")
                    .and()
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
