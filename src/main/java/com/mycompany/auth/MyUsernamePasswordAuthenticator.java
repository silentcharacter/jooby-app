package com.mycompany.auth;

import com.google.inject.Inject;
import com.mycompany.domain.Role;
import com.mycompany.domain.User;
import com.mycompany.service.RoleService;
import com.mycompany.service.UserService;
import com.typesafe.config.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyUsernamePasswordAuthenticator implements Authenticator<UsernamePasswordCredentials>
{
    protected static final Logger logger = LoggerFactory.getLogger(MyUsernamePasswordAuthenticator.class);

    @Inject
    private UserService userService;
    @Inject
    private RoleService roleService;

    public void validate(UsernamePasswordCredentials credentials, WebContext context) throws HttpAction, CredentialsException {
        if (credentials == null) {
            this.throwsException("No credential");
            return;
        }

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (CommonHelper.isBlank(username)) {
            throwsException("Username cannot be blank");
        }

        if (CommonHelper.isBlank(password)) {
            throwsException("Password cannot be blank");
        }

        User user = userService.getBy("email", credentials.getUsername());
        if (user == null || !userService.validatePassword(user, credentials.getUsername(), credentials.getPassword())) {
            throwsException("Password doesn't match");
            return;
        }

        CommonProfile profile = new CommonProfile();
        profile.setId(user.id);
        profile.addAttribute("username", username);
        profile.addAttribute("email", user.email);
        profile.addAttribute("first_name", user.firstName);
        profile.addAttribute("family_name", user.lastName);
        profile.addAttribute("display_name", user.firstName + " " + user.lastName);
        if (user.roles != null) {
            for (String role : user.roles) {
                profile.addRole(roleService.getById(role).name);
            }
        }
        credentials.setUserProfile(profile);
    }

    private void throwsException(String message) throws CredentialsException {
        logger.error(message);
        throw new CredentialsException(message);
    }
}
