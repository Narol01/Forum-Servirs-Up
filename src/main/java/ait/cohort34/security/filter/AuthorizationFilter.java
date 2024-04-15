package ait.cohort34.security.filter;

import ait.cohort34.accounting.dao.UserAccountRepository;
import ait.cohort34.accounting.model.Role;
import ait.cohort34.accounting.model.UserAccount;
import ait.cohort34.accounting.service.UserAccountService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;


public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        if (checkEndpoint(request.getMethod(),request.getServletPath())) {
            try {
                UserAccount userAccount = getCurrentUser(request);
                if (userAccount != null) {
                    Set<Role> roles = userAccount.getRoles();
                    if(!(request.getMethod().equals("DELETE") && checkStartsWithUserPosts(request.getServletPath()))&&roles.contains(Role.ADMINISTRATOR)){
                        response.setStatus(401);
                        return;
                    }
                    else if(!(request.getMethod().equals("DELETE") && checkStartsWithForumPosts(request.getServletPath()))&&roles.contains(Role.MODERATOR)){
                        response.setStatus(401);
                        return;
                    }
                    else {
                        filterChain.doFilter(request,response);
                    }
                }else {
                    response.setStatus(401);
                    return;
                }
            }catch (Exception e){
                response.setStatus(401);
                return;
            }
        }
        filterChain.doFilter(request,response);
    }

    private boolean checkStartsWithUserPosts(String servletPath) {
        return servletPath.startsWith("/user");
    }
    private boolean checkStartsWithForumPosts(String servletPath) {
        return servletPath.startsWith("/post");
    }
    private UserAccount getCurrentUser(HttpServletRequest request) {
        String[] credentials =getCredentials(request.getHeader("Authorization"));
       // return userAccountRepository.findById(credentials[0]).orElseThrow(RuntimeException::new);
        return null;
    }

    private String[] getCredentials(String authorization) {
        String token = authorization.split(" ")[1];
        String decode=new String(Base64.getDecoder().decode(token)) ;
        return decode.split(":");
    }
    private boolean checkEndpoint(String method, String servletPath) {
        return !((method.equals("POST") &&
                (servletPath.equals("/account/register") ||
                        checkStartsWithForumPosts(servletPath))) ||
                (method.equals("GET") && checkStartsWithForumPosts(servletPath)));

    }
}
