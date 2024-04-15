package ait.cohort34.security.filter;

import ait.cohort34.accounting.dao.UserAccountRepository;
import ait.cohort34.accounting.model.Role;
import ait.cohort34.post.dao.PostRepository;
import ait.cohort34.post.model.Post;
import ait.cohort34.security.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
@Order(44)
@RequiredArgsConstructor
public class AdminManagingRolesFilter implements Filter {
    final UserAccountRepository userAccountRepository;
    final PostRepository postRepository;
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        User principal = (User) request.getUserPrincipal();
        if (checkEndpoint(request.getMethod(),request.getServletPath())) {
            if (!principal.getRoles().contains(Role.ADMINISTRATOR.name())) {
                response.sendError(403, "You are not an administrator");
                return;
            }
        }
        if ( checkStartsWithUserPosts(request.getMethod(),request.getServletPath()) ){
            String[] parts = request.getServletPath().split("/");
            if(!(principal.getRoles().contains(Role.ADMINISTRATOR.name()) || principal.getName().equals(parts[3]))){
                response.sendError(403, "You are not an administrator");
                return;
            }
        }
        if ( checkUpdateEnd(request.getMethod(),request.getServletPath()) ){
            String[] parts = request.getServletPath().split("/");
            if(! principal.getName().equals(parts[3])){
                response.sendError(403, "You are not an administrator or owner of this Account");
                return;
            }
        }
        if ( checkDeletePost(request.getMethod(),request.getServletPath()) ){
            String[] parts = request.getServletPath().split("/");
            Post post = postRepository.findById(parts[3]).orElse(null);
            if(! (principal.getName().equals(post.getAuthor())|| principal.getRoles().contains(Role.MODERATOR.name()))){
                response.sendError(403, "You are not an administrator or owner of this Post");
                return;
            }
        }
        if ( checkUpdatePost(request.getMethod(),request.getServletPath()) ){
            String[] parts = request.getServletPath().split("/");
            Post post = postRepository.findById(parts[3]).orElse(null);
            if(! (principal.getName().equals(post.getAuthor()))){
                response.sendError(403, "You are not owner of this Post");
                return;
            }
        }
        if ( checkAddPost(request.getMethod(),request.getServletPath()) ){
            String[] parts = request.getServletPath().split("/");
            if(! (principal.getName().equals(parts[3]))){
                response.sendError(403, "You are not owner of this Post");
                return;
            }
        }
        if ( checkAddCommentToPost(request.getMethod(),request.getServletPath()) ){
            String[] parts = request.getServletPath().split("/");
            if(! (principal.getName().equals(parts[7]))){
                response.sendError(403, "You are not owner of this Post");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkAddCommentToPost(String method, String servletPath) {
        String[] parts = servletPath.split("/");
        return parts.length==7 && "comment".equalsIgnoreCase(parts[5]);
    }

    private boolean checkAddPost(String method, String servletPath) {
        return servletPath.startsWith("/forum/post") && method.equals("POST");
    }

    private boolean checkUpdatePost(String method, String servletPath) {
        return servletPath.startsWith("/forum/post") && method.equals("PUT") ;
    }

    private boolean checkDeletePost(String method, String servletPath) {
        return servletPath.startsWith("/forum/post") && (method.equals("DELETE")) ;
    }

    private boolean checkUpdateEnd(String method, String servletPath) {
        return servletPath.startsWith("/account/user") && method.equals("PUT") ;
    }

    private boolean checkStartsWithUserPosts(String method,String servletPath) {
        return servletPath.startsWith("/account/user") && method.equals("DELETE") ;
    }

    private boolean checkEndpoint(String method, String servletPath) {
        String[] parts = servletPath.split("/");
        return parts.length==6 && "role".equalsIgnoreCase(parts[4]);
    }
    private String[] getCredentials(String authorization) {
        String token = authorization.split(" ")[1];
        String decode=new String(Base64.getDecoder().decode(token)) ;
        return decode.split(":");
}
}
