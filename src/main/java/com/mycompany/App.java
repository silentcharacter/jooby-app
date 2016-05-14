package com.mycompany;

import com.mycompany.auth.MyUsernamePasswordAuthenticator;
import com.mycompany.controller.News;
import com.mycompany.controller.Roles;
import com.mycompany.controller.Todos;
import com.mycompany.controller.Users;
import com.mycompany.hbs.EqualHelper;
import com.mycompany.service.AuthenticationService;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.hbs.Hbs;
import org.jooby.json.Jackson;
import org.jooby.mongodb.Jongoby;
import org.jooby.mongodb.Mongodb;
import org.jooby.pac4j.Auth;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.VkClient;

public class App extends Jooby {

    {
        use(new Mongodb());
        use(new Jongoby());
        use(new Hbs().doWith((hbs, config) -> {
            hbs.registerHelper("equal", new EqualHelper());
        }));
        use(new Jackson());
//        session(MongoSessionStore.class);

        assets("/assets/**");
        assets("/favicon.ico", "/assets/favicon.ico");

        get("/", req -> Results.html("angular").put("profile", AuthenticationService.getUserProfile(req)));

        use(new ShopApp());

        //public rest resources
        use(new Todos());
        use(new News());

        //auth routes
        get("/userProfile", req -> AuthenticationService.getProfilePageData(req));
        get("/login", ((request, response) -> response.redirect("/#/login")));
        post("/register", AuthenticationService.registrationHandler);

        use(new Auth()
                        .form("/admin/**", MyUsernamePasswordAuthenticator.class)
                        .form("/api/**", MyUsernamePasswordAuthenticator.class)
                        .authorizer("admin", "/admin/**", AuthenticationService.authorizerHandler)
                        .client("/google/**", conf -> new Google2Client(conf.getString("google.key"), conf.getString("google.secret")))
                        .client("/vk/**", conf -> new VkClient(conf.getString("vk.key"), conf.getString("vk.secret")))
                        .client("/facebook/**", conf -> new FacebookClient(conf.getString("facebook.key"), conf.getString("facebook.secret")))
                        .client("/twitter/**", conf -> new TwitterClient(conf.getString("twitter.key"), conf.getString("twitter.secret")))
        );

        get("/facebook", AuthenticationService.socialLoginHandler);
        get("/twitter", AuthenticationService.socialLoginHandler);
        get("/google", AuthenticationService.socialLoginHandler);
        get("/vk", AuthenticationService.socialLoginHandler);

        //secure routes
        get("/admin", req -> Results.html("admin"));

        //secure rest resources
        use(new Users());
        use(new Roles());
    }

    public static void main(final String[] args) throws Exception {
        new App().start(args);
    }

}
