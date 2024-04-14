package ait.cohort34.security.filter;

import ait.cohort34.accounting.dao.UserAccountRepository;
import ait.cohort34.accounting.dto.exceptions.UserNotFoundException;
import ait.cohort34.accounting.model.UserAccount;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Order(10)
public class AuthenticationFilter implements Filter {

final UserAccountRepository userAccountRepository;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        if (checkEndpoint(request.getMethod(),request.getServletPath())) {
            try {
                String[] credentials =getCredentials(request.getHeader("Authorization"));
                UserAccount userAccount = userAccountRepository.findById(credentials[0]).orElseThrow(RuntimeException::new);
                if (!BCrypt.checkpw(credentials[1], userAccount.getPassword())) {
                    throw new RuntimeException();
                }
                request = new WrappedRequest(request, userAccount.getLogin());
            }catch (Exception e){
                response.setStatus(401);
                return;
            }
        }
        chain.doFilter(request,response);
    }

    private boolean checkEndpoint(String method, String servletPath) {
        if ((method.equals("POST") &&
                (servletPath.equals("/account/register") ||
                checkStartsWithForumPosts(servletPath)))||
           (method.equals("GET") && checkStartsWithForumPosts(servletPath))){
            return false;
        }else {
            return true;
        }
    }
    private boolean checkStartsWithForumPosts(String servletPath) {
        return servletPath.startsWith("/forum/posts/");
    }

    private String[] getCredentials(String authorization) {
        String token = authorization.split(" ")[1];
        String decode=new String(Base64.getDecoder().decode(token)) ;
        return decode.split(":");
    }
    private static class WrappedRequest extends HttpServletRequestWrapper {
        private final String login;

        public WrappedRequest(HttpServletRequest request, String login) {
            super(request);
            this.login = login;
        }

        @Override
        public Principal getUserPrincipal() {
            return () -> login;
        }
    }
}

