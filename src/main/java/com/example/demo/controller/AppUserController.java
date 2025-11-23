package com.example.demo.controller;

import com.example.demo.model.AppUser;
import com.example.demo.service.AppUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUser> getAllAppUsers() {
        return appUserService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getAppUserById(@PathVariable Long id) {
        Optional<AppUser> appUser = appUserService.findById(id);
        return appUser.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public AppUser createAppUser(@RequestBody AppUser appUser) {
        return appUserService.save(appUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUser> updateAppUser(@PathVariable Long id, @RequestBody AppUser appUserDetails) {
        Optional<AppUser> appUserOptional = appUserService.findById(id);
        if (appUserOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            // Update fields here
            appUser.setUsername(appUserDetails.getUsername());
            appUser.setEmail(appUserDetails.getEmail());
            appUser.setFirstName(appUserDetails.getFirstName());
            appUser.setLastName(appUserDetails.getLastName());
            return ResponseEntity.ok(appUserService.save(appUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppUser(@PathVariable Long id) {
        appUserService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
