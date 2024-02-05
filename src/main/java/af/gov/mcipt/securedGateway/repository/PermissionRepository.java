package af.gov.mcipt.securedGateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import af.gov.mcipt.securedGateway.domain.Permission;

public 	interface PermissionRepository extends JpaRepository<Permission, Long> {
  
}
