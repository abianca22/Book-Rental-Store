package org.booksrental.userservice.service;

import org.booksrental.userservice.exception.NoDataFound;
import org.booksrental.userservice.model.entity.Role;
import org.booksrental.userservice.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NoDataFound("Role with name " + name + " does not exist!"));
    }
}
