package com.mycompany.service;

import com.google.inject.Inject;
import com.mycompany.domain.Role;
import com.mycompany.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Route;
import org.jooby.pac4j.Auth;
import org.jooby.pac4j.AuthStore;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;


public class AuthenticationService {

    private static String ADMIN_NAME = "Администратор";

    @Inject
    private UserService userService;
    @Inject
    private RoleService roleService;

    public void handleSocLogin(Request req, Response rsp) throws Throwable {
        CommonProfile profile = getUserProfile(req);
        if (profile == null) {
            rsp.redirect("/");
            return;
        }
        String email = extractEmail(req, profile);
        User userByEmail = userService.getBy("email", email);
        User userByProfileId = userService.getBy("profileId", profile.getId());
        if (userByEmail == null && userByProfileId == null) {
            User user = new User();
            user.profileId = profile.getId();
            user.firstName = Optional.ofNullable(profile.getFirstName()).orElse(profile.getDisplayName());
            user.lastName = profile.getFamilyName();
            user.email = email;
            Role role = roleService.getBy("name", "Пользователь");
            if (role != null)
                user.roles = Collections.singletonList(role.id);
            userService.insert(user);
        } else if (userByProfileId != null && userByProfileId.roles != null) {
            for (String role : userByProfileId.roles) {
                profile.addRole(roleService.getById(role).name);
            }
            AuthStore<CommonProfile> store = req.require(AuthStore.class);
            store.set(profile);
        }
        Optional<String> redirectUrl = req.session().get("redirectUrl").toOptional();
        if (redirectUrl.isPresent()) {
            rsp.redirect(redirectUrl.get());
            req.session().unset("redirectUrl");
        }
        rsp.redirect("/");
    };

    public void handleRegistration(Request req, Response rsp) throws Throwable {
        User user = req.body().to(User.class);
        user.email = user.email.trim();
        User userByEmail = userService.getBy("email", user.email);
        if (userByEmail != null) {
            rsp.send("Пользователь с указанным email уже зарегистрирован!");
            return;
        }
        if (user.password.equals(req.param("password_confirm").toString())) {
            rsp.send("Указанные пароли не совпадают!");
            return;
        }
        user.roles = Collections.singletonList(roleService.getBy("name", "Пользователь").id);
        userService.insert(user);
        rsp.redirect("/todo/#/registrationSuccess");
    };

    public boolean isAuthorized(WebContext ctx, List<CommonProfile> profiles) throws HttpAction {
        for (Object profile : profiles) {
            if (profile instanceof CommonProfile) {
                CommonProfile commonProfile = (CommonProfile)profile;
                if (commonProfile.getRoles() != null) {
                    for (String role : commonProfile.getRoles())
                    {
                        if (ADMIN_NAME.equals(role)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    };

    private User getCurrentUser(Request req, CommonProfile profile) {
        if (profile == null) {
            return null;
        }
        String email = AuthenticationService.extractEmail(req, profile);
        if (!StringUtils.isEmpty(email)) {
            return userService.getBy("email", email);
        }
        return userService.getBy("profileId", profile.getId());
    }

    private static String extractEmail(Request req, CommonProfile profile) {
        String email = profile.getEmail();
        if (email == null) {
            final String[] emails = {null};
            req.session().attributes().forEach((k, v) -> {
                if (k.endsWith("emails")) {
                    emails[0] = v;
                }
            });
            email = emails[0];
        }
        return email;
    }

    @SuppressWarnings("unchecked")
    public static CommonProfile getUserProfile(Request req) {
        try {
            Optional<String> profileId = req.session().get(Auth.ID).toOptional();
            if (profileId.isPresent()) {
                AuthStore<CommonProfile> store = req.require(AuthStore.class);
                return store.get(profileId.get()).get();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public Map<String, Object> getProfilePageData(Request req) {
        Map<String, Object> response = new HashMap<>();
        CommonProfile profile = getUserProfile(req);
        if (profile != null) {
            response.put("client", profile.getClass().getSimpleName().replace("Profile", ""));
        }
        response.put("profile", profile);
        User user = getCurrentUser(req, profile);
        if (user != null) {
            response.put("userId", user.id);
        }
        return response;
    }

}
