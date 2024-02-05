package af.gov.mcipt.securedGateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import af.gov.mcipt.securedGateway.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
}
