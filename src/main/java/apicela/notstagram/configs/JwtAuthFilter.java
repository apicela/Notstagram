package apicela.notstagram.configs;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(TokenService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        email = jwtService.extractEmail(jwt);
        System.out.println("email-> " + email + " - jwt: " + jwt);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = (User) this.userDetailsService.loadUserByUsername(email);

//            if (userDetails.isInactive() && !request.getRequestURI().equals("/users/me/activate")) {
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                response.getWriter().write("Usuário inativo. Ative a conta para acessar.");
//                return;
//            }

            if (jwtService.isTokenValid(jwt, userDetails)) {
                List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());

                String extra = jwtService.extractExtraAuthority(jwt);
                if (extra != null) {
                    authorities.add(new SimpleGrantedAuthority(extra));
                }

                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
