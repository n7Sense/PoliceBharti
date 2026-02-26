package com.nst.ufrs.service;

import com.nst.ufrs.entity.PasswordResetToken;
import com.nst.ufrs.entity.User;

import java.util.Optional;

public interface PasswordResetTokenService {

    PasswordResetToken createTokenForUser(User user);

    Optional<PasswordResetToken> validatePasswordResetToken(String token);

    void markTokenAsUsed(PasswordResetToken token);

    void updatePassword(User user, String newPassword);
}
