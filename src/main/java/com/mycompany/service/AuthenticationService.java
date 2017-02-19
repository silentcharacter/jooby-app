package com.mycompany.service;

import com.mycompany.domain.Role;
import com.mycompany.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Request;
import org.jooby.Route;
import org.jooby.pac4j.Auth;
import org.jooby.pac4j.AuthStore;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.profile.google2.Google2Profile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthenticationService {

    private static String ADMIN_NAME = "Администратор";

    public static Route.Handler socialLoginHandler = (req, rsp) -> {
        CommonProfile profile = getUserProfile(req);
        if (profile == null) {
            rsp.redirect("/");
            return;
        }
        String email = extractEmail(req, profile);
        Jongo jongo = req.require(Jongo.class);
        MongoCollection users = jongo.getCollection("users");
        if (users.count("{email : #}", email) == 0 && users.count("{profileId : #}", profile.getId()) == 0) {
            User user = new User();
            user.profileId = profile.getId();
            user.firstName = Optional.ofNullable(profile.getFirstName()).orElse(profile.getDisplayName());
            user.lastName = profile.getFamilyName();
            user.email = email;
            MongoCollection roles = jongo.getCollection("roles");
            Role role = roles.findOne("{name: 'Пользователь'}").as(Role.class);
            if (role != null)
                user.roles = Collections.singletonList(role.id);
            users.insert(user);
        } else {
            User user = users.findOne("{profileId : #}", profile.getId()).as(User.class);
            if (user != null && user.roles != null) {
                MongoCollection roles = jongo.getCollection("roles");
                for (String role : user.roles) {
                    profile.addRole(roles.findOne(new ObjectId(role)).as(Role.class).name);
                }
                AuthStore<CommonProfile> store = req.require(AuthStore.class);
                store.set(profile);
            }
        }
        Optional<String> redirectUrl = req.session().get("redirectUrl").toOptional();
        if (redirectUrl.isPresent()) {
            rsp.redirect(redirectUrl.get());
            req.session().unset("redirectUrl");
        }
        rsp.redirect("/");
    };

    public static Route.Handler registrationHandler = (req, rsp) -> {
        User user = req.body().to(User.class);
        user.email = user.email.trim();
        Jongo jongo = req.require(Jongo.class);
        MongoCollection users = jongo.getCollection("users");
        if (users.count("{email : #}", user.email) > 0) {
            rsp.send("Пользователь с указанным email уже зарегистрирован!");
            return;
        }
        if (user.password.equals(req.param("password_confirm").toString())) {
            rsp.send("Указанные пароли не совпадают!");
            return;
        }
        MongoCollection roles = jongo.getCollection("roles");
        user.roles = Collections.singletonList(roles.findOne("{name: 'Пользователь'}").as(Role.class).id);
        users.insert(user);
        rsp.redirect("/todo/#/registrationSuccess");
    };

    public static Authorizer authorizerHandler = (ctx, profiles) -> {
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

    private static User getCurrentUser(Request req, CommonProfile profile) {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection users = jongo.getCollection("users");
        if (profile == null) {
            return null;
        }
        String email = AuthenticationService.extractEmail(req, profile);
        if (!StringUtils.isEmpty(email)) {
            return users.findOne("{email : #}", email).as(User.class);
        }
        return users.findOne("{profileId : #}", profile.getId()).as(User.class);
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

    public static Map<String, Object> getProfilePageData(Request req) {
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
