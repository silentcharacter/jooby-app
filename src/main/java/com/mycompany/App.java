package com.mycompany;

import com.mycompany.auth.MyUsernamePasswordAuthenticator;
import com.mycompany.controllers.News;
import com.mycompany.controllers.Roles;
import com.mycompany.controllers.Todos;
import com.mycompany.controllers.Users;
import com.mycompany.domain.New;
import com.mycompany.domain.Role;
import com.mycompany.domain.User;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Results;
import org.jooby.Route;
import org.jooby.hbs.Hbs;
import org.jooby.json.Jackson;
import org.jooby.mongodb.Jongoby;
import org.jooby.mongodb.Mongodb;
import org.jooby.pac4j.Auth;
import org.jooby.pac4j.AuthStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.VkClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class App extends Jooby {

    {
        use(new Mongodb());
        use(new Jongoby());
        use(new Hbs());
        use(new Jackson());
//        session(MongoSessionStore.class);

        assets("/assets/**");
        assets("/favicon.ico", "/assets/favicon.ico");

        get("/", req -> Results.html("angular").put("profile", getUserProfile(req)));
        use(new Todos());
        use(new News());

        get("/userProfile", req -> getProfilePageData(req));

        get("/login", ((request, response) -> response.redirect("/#/login")));

        post("/register", registrationHandler);

        use(new Auth()
                        .client("/google/**", conf -> new Google2Client(conf.getString("google.key"), conf.getString("google.secret")))
                        .client("/vk/**", conf -> new VkClient(conf.getString("vk.key"), conf.getString("vk.secret")))
                        .client("/facebook/**", conf -> new FacebookClient(conf.getString("facebook.key"), conf.getString("facebook.secret")))
                        .client("/twitter/**", conf -> new TwitterClient(conf.getString("twitter.key"), conf.getString("twitter.secret")))
                        .form("*", MyUsernamePasswordAuthenticator.class)
//                        .authorizer("admin", "/admin/**", (ctx, profile) -> {
//                            if (!(profile instanceof HttpProfile || profile instanceof Google2Profile)) {
//                                return false;
//                            }
//                            final HttpProfile httpProfile = (HttpProfile) profile;
//                            final String username = httpProfile.getUsername();
//                            return true;
//                        })
        );

        get("/facebook", socialLoginHandler);
        get("/twitter", socialLoginHandler);
        get("/google", socialLoginHandler);
        get("/vk", socialLoginHandler);

        get("/admin", req -> Results.html("admin"));
        use(new Users());
        use(new Roles());
    }

    private static Route.Handler socialLoginHandler = (req, rsp) -> {
        CommonProfile profile = getUserProfile(req);
        if (profile == null) {
            rsp.redirect("/");
            return;
        }
        String email = extractEmail(req, profile);
        Jongo jongo = req.require(Jongo.class);
        MongoCollection users = jongo.getCollection("users");
        if (users.count("{email : #}", email) == 0) {
            User user = new User();
            user.profileId = profile.getId();
            user.firstName = profile.getFirstName();
            user.lastName = profile.getFamilyName();
            user.email = email;
            MongoCollection roles = jongo.getCollection("roles");
            user.roles = Collections.singletonList(roles.findOne("{name: 'Пользователь'}").as(Role.class).id);
            users.insert(user);
        }
        rsp.redirect("/");
    };

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

    private static Route.Handler registrationHandler = (req, rsp) -> {
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
        rsp.redirect("/#/registrationSuccess");
    };

    private static Map<String, Object> getProfilePageData(Request req) {
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

    private static User getCurrentUser(Request req, CommonProfile profile) {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection users = jongo.getCollection("users");
        if (profile == null) {
            return null;
        }
        User user = users.findOne("{email : #}", extractEmail(req, profile)).as(User.class);
        return user;
    }

    @SuppressWarnings("unchecked")
    public static CommonProfile getUserProfile(Request req) {
        try {
            Optional<String> profileId = req.session().get(Auth.ID).toOptional();
            if (profileId.isPresent()) {
                AuthStore<UserProfile> store = req.require(AuthStore.class);
                return (CommonProfile) store.get(profileId.get()).get();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static void main(final String[] args) throws Exception {
        new App().start(args);
    }

}
