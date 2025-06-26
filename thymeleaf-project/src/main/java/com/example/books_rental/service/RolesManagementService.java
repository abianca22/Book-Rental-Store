package com.example.books_rental.service;

import com.example.books_rental.model.entities.Role;
import com.example.books_rental.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolesManagementService {
    private final RoleRepository roleRepository;

    public RolesManagementService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(int id) {
        return roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
    }

}
