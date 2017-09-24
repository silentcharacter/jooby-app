package com.mycompany.service.shop;

import com.mycompany.domain.shop.CmsPage;
import com.mycompany.domain.shop.CmsPost;
import com.mycompany.service.AbstractService;


public class CmsPageService extends AbstractService<CmsPage> {

    public CmsPageService() {
        super(CmsPage.class);
    }


    @Override
    public void onSave(CmsPage object) {
        if (object.posts != null) {
            boolean odd = true;
            boolean first = true;
            for (CmsPost post : object.posts)
            {
                post.odd = odd;
                post.first = first;
                odd = !odd;
                first = false;
            }
        }
    }
}
