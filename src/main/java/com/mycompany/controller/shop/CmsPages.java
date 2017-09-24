
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.CmsPage;
import com.mycompany.service.shop.CmsPageService;


public class CmsPages extends AbstractResource<CmsPage> {

    public CmsPages() {
        super(CmsPage.class, CmsPageService.class);
    }
}
