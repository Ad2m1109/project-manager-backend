package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }

    public AppUser save(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    public void deleteById(Long id) {
        appUserRepository.deleteById(id);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
}
