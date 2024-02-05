package af.gov.mcipt.securedGateway.service.dto;

import java.util.Set;

import af.gov.mcipt.securedGateway.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RegisterRequest {
  private String firstname;
  private String lastname;
  private String email;
  private String password;
  private Set<Role> roles;
  private String createdBy;

}