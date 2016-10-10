package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;


@Deployment(table = "colors")
public class Color extends Product {
    public boolean _default;
}
