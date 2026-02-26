package com.nst.ufrs.service;

import com.nst.ufrs.dto.RegistrationDto;
import com.nst.ufrs.entity.User;

import java.util.Optional;

public interface UserService {

    User registerNewUser(RegistrationDto registrationDto);

    Optional<User> findByEmail(String email);
}
