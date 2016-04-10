package com.mycompany;

import org.jooby.Jooby;
import org.jooby.Results;

public class ShopApp extends Jooby {

    {
        get("/shop", req -> Results.html("shop"));
    }
}
