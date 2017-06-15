
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Category;
import com.mycompany.domain.shop.Color;
import com.mycompany.service.shop.CategoryService;
import com.mycompany.service.shop.ColorService;


public class Categories extends AbstractResource<Category> {

    public Categories() {
        super(Category.class, CategoryService.class);
    }
}
