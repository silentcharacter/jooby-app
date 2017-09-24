
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.CmsPost;
import com.mycompany.service.shop.CmsPostService;


public class CmsPosts extends AbstractResource<CmsPost> {

    public CmsPosts() {
        super(CmsPost.class, CmsPostService.class);
    }
}
