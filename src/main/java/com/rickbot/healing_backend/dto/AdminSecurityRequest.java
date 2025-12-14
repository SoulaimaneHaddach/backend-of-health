package com.rickbot.healing_backend.dto;

import java.util.Map;

public class AdminSecurityRequest {
    // enable or disable two-factor authentication globally
    public Boolean twoFactorEnabled;

    // allow password-based login (if false, require external SSO)
    public Boolean allowPasswordLogin;

    // arbitrary additional security options
    public Map<String, Object> extras;
}
