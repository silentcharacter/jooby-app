package com.mycompany;

import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.http.profile.HttpProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyUsernamePasswordAuthenticator implements UsernamePasswordAuthenticator {

    protected static final Logger logger = LoggerFactory.getLogger(MyUsernamePasswordAuthenticator.class);

    public MyUsernamePasswordAuthenticator() {
    }

    public void validate(UsernamePasswordCredentials credentials) {
        if (credentials == null) {
            this.throwsException("No credential");
            return;
        }

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (CommonHelper.isBlank(username)) {
            this.throwsException("Username cannot be blank");
        }

        if (CommonHelper.isBlank(password)) {
            this.throwsException("Password cannot be blank");
        }

        if (!username.equals("admin") || !password.equals("123123")) {
            this.throwsException("Username : \'" + username + "\' does not match password");
        }
        HttpProfile profile = new HttpProfile();
        profile.setId(username);
        profile.addAttribute("username", username);
        credentials.setUserProfile(profile);
    }

    protected void throwsException(String message) {
        logger.error(message);
        throw new CredentialsException(message);
    }
}
