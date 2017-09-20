package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.bson.types.Binary;


@Deployment(table = "reviews")
public class Review extends Entity
{
    public String name;
    public String text;
    public String link;
    public String productId;
    public Binary image;
    public boolean approved;
}
