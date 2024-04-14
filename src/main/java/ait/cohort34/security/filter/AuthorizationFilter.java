package ait.cohort34.security.filter;

import ait.cohort34.accounting.model.Role;
import ait.cohort34.accounting.model.UserAccount;
import ait.cohort34.accounting.service.UserAccountService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Order(21)
@Component
@RequiredArgsConstructor
public class AuthorizationFilter implements Filter {
    final UserAccountService userAccountService;
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        UserAccount userAccount = getCurrentUser(request);
        if (userAccount != null) {
            Set<Role> roles = userAccount.getRoles();
            if(!(request.getMethod().equals("DELETE") && checkStartsWithUserPosts(request.getServletPath()))&&roles.contains(Role.ADMINISTRATOR)){
                response.setStatus(401);
            }
            else if(!(request.getMethod().equals("DELETE") && checkStartsWithForumPosts(request.getServletPath()))&&roles.contains(Role.MODERATOR)){
                response.setStatus(401);
            }
            else {
                filterChain.doFilter(request,response);
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
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (UserAccount) session.getAttribute("currentUser");
        }
        return null;
    }
}
