package com.w16a.danish.user.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description Util class for password encryption and verification
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordUtil(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Encrypt password using BCrypt
     */
    public String encryptPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verify if the raw password matches the encoded password
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    /**
     *
     * Check if the password is valid
     *
     * @param pwd Password to check
     * @return boolean true if valid, false otherwise
     */
    public boolean isPasswordValid(String pwd) {
        return StrUtil.isNotBlank(pwd) &&
                pwd.length() >= 8 &&
                ReUtil.isMatch(".*[A-Z].*", pwd) &&
                ReUtil.isMatch(".*\\d.*", pwd);
    }

}
