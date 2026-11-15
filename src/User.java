import java.util.HashSet;
import java.util.Set;
public class User {
    private Long id;
    private String username;
    private String password;

    private Set<UserRole> roles;
    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }
    public void addRole(UserRole role) {
        roles.add(role);
    }
    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(this.roles);
    }
}