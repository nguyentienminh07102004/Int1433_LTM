package com.ptit.b22cn539.int1433.Configuration;

import com.ptit.b22cn539.int1433.Models.UserEntity;
import com.ptit.b22cn539.int1433.Repository.IUserRepository;
import com.ptit.b22cn539.int1433.Utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppFilterSecurity extends OncePerRequestFilter {
    IUserRepository userRepository;
    JwtUtils jwtUtils;

    List<Pair<String, HttpMethod>> WHITE_LIST = List.of(
            Pair.of("/users/login", HttpMethod.POST),
            Pair.of("/users/register", HttpMethod.POST),
            Pair.of("/musics", HttpMethod.POST)
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        for (Pair<String, HttpMethod> entry : WHITE_LIST) {
            String path = entry.getFirst();
            HttpMethod method = entry.getSecond();
            if (request.getServletPath().matches(path) && request.getMethod().equalsIgnoreCase(method.name())) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
            return;
        }
        token = token.substring(7);
        Claims claims = this.jwtUtils.extractClaims(token);
        String username = claims.getSubject();
        if (!StringUtils.hasText(username)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
            return;
        }
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
