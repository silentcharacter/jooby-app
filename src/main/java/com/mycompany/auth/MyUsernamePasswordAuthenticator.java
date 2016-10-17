package com.mycompany.auth;

import com.google.inject.Inject;
import com.mycompany.domain.Role;
import com.mycompany.domain.User;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyUsernamePasswordAuthenticator implements Authenticator<UsernamePasswordCredentials>
{

    protected static final Logger logger = LoggerFactory.getLogger(MyUsernamePasswordAuthenticator.class);

    private Jongo jongo;

    @Inject
    public MyUsernamePasswordAuthenticator(Jongo jongo) {
        this.jongo = jongo;
    }

    public void validate(UsernamePasswordCredentials credentials, WebContext context) {
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

        MongoCollection users = jongo.getCollection("users");
        User user = users.findOne("{email:#}", credentials.getUsername()).as(User.class);
        if (user == null || !credentials.getPassword().equals(user.password)) {
            this.throwsException("Password doesn't match");
        }

        CommonProfile profile = new CommonProfile();
        profile.setId(user.id);
        profile.addAttribute("username", username);
        profile.addAttribute("email", user.email);
        profile.addAttribute("first_name", user.firstName);
        profile.addAttribute("family_name", user.lastName);
        profile.addAttribute("display_name", user.firstName + " " + user.lastName);
        if (user.roles != null) {
            MongoCollection roles = jongo.getCollection("roles");
            for (String role : user.roles) {
                profile.addRole(roles.findOne(new ObjectId(role)).as(Role.class).name);
            }
        }
        credentials.setUserProfile(profile);
    }

    protected void throwsException(String message) {
        logger.error(message);
        throw new CredentialsException(message);
    }
}
