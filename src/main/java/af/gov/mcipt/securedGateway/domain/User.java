package af.gov.mcipt.securedGateway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
@Data
@Entity
@Builder
@Table(name = "tbl_user")
@AllArgsConstructor
@NoArgsConstructor
public class User extends AbstractAuditingEntity<Long> implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;
  @NotBlank
  private String firstname;
  @NotBlank
  private String lastname;
  @NotBlank
  @Email
  @Column(unique = true)
  private String email;
  @NotBlank
  private String password;
 
  @NotNull
  private boolean isActivated;
  @Column(name = "reset_date")
  private Instant resetDate = null;
  
  private LocalDate lastLogin;

  @Size(max = 20)
  @Column(name = "reset_key", length = 20)
  @JsonIgnore
   private String resetKey;
  
  @ManyToMany(fetch = FetchType.EAGER )
	@JoinTable(name="user_roles", joinColumns = @JoinColumn(name="user_id"), inverseJoinColumns = @JoinColumn(name="role_id"))
  private Set<Role> roles=new HashSet<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
	  List<GrantedAuthority> authorities = new ArrayList<>();
      this.getRoles().forEach(role -> {
          authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
      });
      
      return authorities;
  }
  public boolean isActivated() {
	    return isActivated;
	}

	public void setIsActivated(boolean activated) {
	    isActivated = activated;
	}

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getPassword() {
    return password;
  }
}
