package com.mycompany.controller;

import com.mycompany.domain.User;
import com.mycompany.service.UserService;
import org.jooby.Request;
import org.jooby.Route;


public class Users extends AbstractResource<User> {

    public Users() {
        super(User.class, UserService.class);
    }

    @Override
    protected User getById(Request req) {
        User user = service.getById(req.param("id").value());
        user.password = "";
        user.passwordConfirm = "";
        return user;
    }

}
