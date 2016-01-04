package com.mycompany;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mycompany.auth.MyUsernamePasswordAuthenticator;
import com.mycompany.controllers.Roles;
import com.mycompany.controllers.Todos;
import com.mycompany.controllers.Users;
import com.typesafe.config.Config;
import org.jooby.*;
import org.jooby.hbs.Hbs;
import org.jooby.json.Jackson;
import org.jooby.mongodb.Jongoby;
import org.jooby.mongodb.Mongodb;
import org.jooby.pac4j.Auth;
import org.jooby.pac4j.AuthStore;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.VkClient;
import org.pac4j.oauth.profile.google2.Google2Profile;

import java.util.Optional;

public class App extends Jooby {

    {
        use(new Mongodb());
        use(new Jongoby());
        use(new Hbs());
        use(new Jackson());
//        session(MongoSessionStore.class);

        assets("/assets/**");
        assets("/favicon.ico", "/assets/favicon.ico");

        get("*", (req, rsp) -> {
            boolean loggedIn = req.session().get(Auth.ID).toOptional().isPresent();
            req.set("loggedIn", loggedIn);
        });

        get("/", req -> {
            return Results.html("index");
        });

        get("/angular", req -> Results.html("angular"));
        use(new Todos());
        use(new Users());
        use(new Roles());

        get("/login", (req, rsp) -> {
            Config conf = req.require(Config.class);
            rsp.send(Results.html("login").put("authCallback", conf.getString("auth.callback")));
        });

        use(new Auth()
                        .client("/google/**", conf -> new Google2Client(conf.getString("google.key"), conf.getString("google.secret")))
                        .client("/vk/**", conf -> new VkClient(conf.getString("vk.key"), conf.getString("vk.secret")))
                        .client("/facebook/**", conf -> new FacebookClient(conf.getString("facebook.key"), conf.getString("facebook.secret")))
                        .client("/twitter/**", conf -> new TwitterClient(conf.getString("twitter.key"), conf.getString("twitter.secret")))
                        .form("*", MyUsernamePasswordAuthenticator.class)
                        .authorizer("admin", "/admin/**", (ctx, profile) -> {
                            if (!(profile instanceof HttpProfile || profile instanceof Google2Profile)) {
                                return false;
                            }
//                            final HttpProfile httpProfile = (HttpProfile) profile;
//                            final String username = httpProfile.getUsername();
                            return true;
                        })
        );

        /** One handler for logged user. */
        @SuppressWarnings("unchecked")
        Route.OneArgHandler handler = req -> {
            UserProfile profile = getUserProfile(req);

            return Results.html("profile")
                    .put("client", profile.getClass().getSimpleName().replace("Profile", ""))
                    .put("profile", profile);
        };

        get("/profile", handler);
        get("/facebook", handler);
        get("/twitter", handler);
        get("/google", handler);
        get("/vk", handler);
        get("/admin", (req, rsp) -> {
            Config conf = req.require(Config.class);
            rsp.send(Results.html("admin").put("apiUrl", conf.getString("application.apiUrl")));
        });
//        get("/rest-jwt", handler);
//        get("/generate-token", handler);

    }

    @SuppressWarnings("unchecked")
    public UserProfile getUserProfile(final Request req) throws Exception {
        // show profile or 401
        Optional<String> profileId = req.session().get(Auth.ID).toOptional();
        if (!profileId.isPresent()) {
            throw new Err(Status.UNAUTHORIZED);
        }
        AuthStore<UserProfile> store = req.require(AuthStore.class);
        return store.get(profileId.get()).get();
    }

    public static void main(final String[] args) throws Exception {
        new App().start(args);
    }

}
