package com.mycompany.controllers;

import com.mycompany.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.Find;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;

public class Users extends AbstractResource<User> {

    public Users() {
        super(User.class, "users");
    }

    {
        initializeRoutes();
    }
}
