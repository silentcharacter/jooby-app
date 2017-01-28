package com.mycompany;

import com.mycompany.auth.MyUsernamePasswordAuthenticator;
import com.mycompany.controller.News;
import com.mycompany.controller.Roles;
import com.mycompany.controller.Todos;
import com.mycompany.controller.Users;
import com.mycompany.controller.shop.Orders;
import com.mycompany.hbs.EqualHelper;
import com.mycompany.hbs.FormatDateHelper;
import com.mycompany.hbs.IncHelper;
import com.mycompany.service.AuthenticationService;
import com.mycompany.service.MigrationService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.TimeZone;


public class App extends Jooby {

    final static Logger logger = LoggerFactory.getLogger(App.class);

    {
        use(new Mongodb());
        use(new Jongoby());
        use(new Hbs().doWith((hbs, config) -> {
            hbs.registerHelper("equal", new EqualHelper());
            hbs.registerHelper("formatDate", new FormatDateHelper());
            hbs.registerHelper("inc", new IncHelper());
        }));
        use(new Jackson().doWith(mapper -> {
            mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        }));
//        session(MongoSessionStore.class);

        assets("/assets/**");
        assets("/favicon.ico", "/assets/favicon.ico");
        assets("/sp-push-manifest.json", "/assets/js/sp-push-manifest.json");
        assets("/sp-push-worker.js", "/assets/js/sp-push-worker.js");

        get("/", req -> Results.html("angular").put("profile", AuthenticationService.getUserProfile(req)));

        use(new ShopApp());

        onStart(MigrationService::runUpdateScripts);

        //public rest resources
        use(new Todos());
        use(new News());

        //auth routes
        get("/userProfile", AuthenticationService::getProfilePageData);
        get("/login", ((request, response) -> response.redirect("/#/login")));
        post("/register", AuthenticationService.registrationHandler);
        get("**", (req, rsp, chain) -> {
            Optional<String> profileId = req.session().get(Auth.ID).toOptional();
            if (!profileId.isPresent() && req.path().contains("admin")) {
                req.session().set("redirectUrl", req.path());
            }
            chain.next(req, rsp);
        });

        use(new Auth()
                        .form("/admin/**", MyUsernamePasswordAuthenticator.class)
                        .form("/api/**", MyUsernamePasswordAuthenticator.class)
//                        .authorizer("admin", "/admin/**", AuthenticationService.authorizerHandler)
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

        use(new Orders());
        //secure rest resources
        use(new Users());
        use(new Roles());

    }

    public static void main(final String[] args) throws Exception {
        new App().start(args);
    }

}
