package apicela.notstagram.services;

import apicela.notstagram.exceptions.NotFoundException;
import apicela.notstagram.models.entities.Role;
import apicela.notstagram.repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Cacheable(value = "rolesCache", key = "#name")
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.info("Role not found with name: {}", name);
                    return new EntityNotFoundException("Role not found: " + name);
                });
    }

}
