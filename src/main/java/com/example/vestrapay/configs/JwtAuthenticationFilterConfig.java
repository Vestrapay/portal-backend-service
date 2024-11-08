package com.example.vestrapay.configs;


import com.example.vestrapay.merchant.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.merchant.roles_and_permissions.repository.RolePermissionRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.merchant.users.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilterConfig extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService merchantService;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
    //check if the request has auth Header
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt;
        String userEmail = null;


        if (authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
        } else if (request.getRequestURI().contains("login")) {
            filterChain.doFilter(request,response);
        } else {

            jwt = authHeader.substring(7);
            if (jwt.length()<5){
                filterChain.doFilter(request,response);
            }

            try {
                userEmail = jwtService.extractUsername(jwt);
            }catch (Exception e){
                System.out.println("error extracting token"+request.getRequestURI());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());

                // Optionally, write an error message to the response body
                response.getWriter().write("Unauthorized: Invalid token");
                response.getWriter().flush();

                return;
            }
            if (userEmail !=null && SecurityContextHolder.getContext().getAuthentication()==null){
                //user not authenticated, we need to authenticate the user
                UserDetails userDetails = merchantService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt,userDetails)){
                    User user = (User)userDetails;
                    List<RolePermission> userPermissionList;
                    if (user.getUserType().equals(UserType.SUPER_ADMIN)||user.getUserType().equals(UserType.ADMIN)){
                        userPermissionList= rolePermissionRepository.findAll().collectList().block();

                    }
                    else {
                        userPermissionList = rolePermissionRepository.findByUserId(user.getUuid()).collectList().block();
                    }
                    List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
                    if (Objects.isNull(userPermissionList)){
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                    else {
                        userPermissionList.forEach(rolePermission -> {
                            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(rolePermission.getPermissionId()));
                        });
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,simpleGrantedAuthorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            filterChain.doFilter(request,response);


        }


    }
}
