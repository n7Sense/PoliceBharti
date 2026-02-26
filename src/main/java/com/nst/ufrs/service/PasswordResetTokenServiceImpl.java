package com.nst.ufrs.service;

import com.nst.ufrs.entity.PasswordResetToken;
import com.nst.ufrs.entity.User;
import com.nst.ufrs.repository.PasswordResetTokenRepository;
import com.nst.ufrs.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final int EXPIRATION_MINUTES = 15;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetTokenServiceImpl(PasswordResetTokenRepository tokenRepository,
                                         UserRepository userRepository,
                                         PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordResetToken createTokenForUser(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(expiry);
        token.setUsed(false);

        return tokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> validatePasswordResetToken(String tokenValue) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.isUsed() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        return tokenOpt;
    }

    @Override
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
