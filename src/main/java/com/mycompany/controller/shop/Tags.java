
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Tag;
import com.mycompany.service.shop.TagService;


public class Tags extends AbstractResource<Tag> {

    public Tags() {
        super(Tag.class, TagService.class);
    }
}
