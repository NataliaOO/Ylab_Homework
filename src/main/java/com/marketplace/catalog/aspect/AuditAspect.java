package com.marketplace.catalog.aspect;

import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.AuthService;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;


@Aspect
public class AuditAspect {

    private static final Logger log = Logger.getLogger(AuditAspect.class.getName());

    // ---------- PRODUCT CRUD ----------

    // CREATE
    @AfterReturning(
            pointcut = "execution(* com.marketplace.catalog.service.ProductService+.createProduct(..)) && args(product, username)",
            returning = "created",
            argNames = "product,username,created")
    public void auditCreate(Product product, String username, Product created) {
        Long id = created != null ? created.getId() : null;
        audit(username, "CREATE_PRODUCT", "id=" + id + " product=" + product);
    }

    // UPDATE
    @AfterReturning(
            pointcut = "execution(* com.marketplace.catalog.service.ProductService+.updateProduct(..)) && args(id, updated, username)",
            returning = "ignored",
            argNames = "id,updated,username,ignored")
    public void auditUpdate(Long id, Product updated, String username, Object ignored) {
        audit(username, "UPDATE_PRODUCT", "id=" + id + " product=" + updated);
    }

    // DELETE
    @AfterReturning(
            pointcut = "execution(* com.marketplace.catalog.service.ProductService+.deleteProduct(..)) && args(id, username)",
            returning = "ignored",
            argNames = "id,username,ignored")
    public void auditDelete(Long id, String username, Object ignored) {
        audit(username, "DELETE_PRODUCT", "id=" + id);
    }

    // ---------- AUTH: LOGIN / LOGOUT ----------

    @AfterReturning(
            pointcut = "execution(boolean com.marketplace.catalog.service.AuthService+.login(..)) && args(login, password)",
            returning = "success",
            argNames = "login,password,success")
    public void auditLogin(String login, String password, boolean success) {
        audit(login, "LOGIN", success ? "success" : "failed");
    }

    @After("execution(* com.marketplace.catalog.service.AuthService+.logout(..)) && target(authService)")
    public void auditLogout(AuthService authService) {
        User current = authService.getCurrentUser();
        String username = current != null ? current.getLogin() : "unknown";
        audit(username, "LOGOUT", "user logged out");
    }

    // ---------- COMMON ----------

    private void audit(String username, String action, String details) {
        AuditRecord record = new AuditRecord(
                LocalDateTime.now(),
                username,
                action,
                details
        );
        log.log(Level.INFO,
                () -> String.format("[AUDIT] %s", record));
    }
}
