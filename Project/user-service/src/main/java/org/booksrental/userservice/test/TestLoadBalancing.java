package org.booksrental.userservice.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usersTest")
public class TestLoadBalancing {

    @Value("${server.port}")
    private String port;

    @GetMapping("/instance-id")
    public ResponseEntity<?> getInstanceId() {
        return ResponseEntity.ok().body("User Service running on port: " + port);
    }
}