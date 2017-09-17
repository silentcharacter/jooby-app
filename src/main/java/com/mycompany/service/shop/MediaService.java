package com.mycompany.service.shop;

import com.mycompany.domain.shop.Media;
import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;
import org.bson.types.ObjectId;


public class MediaService extends AbstractService<Media> {

    public MediaService() {
        super(Media.class);
    }

    @Override
    public void onSave(Media media) {
        if (media.image == null && StringUtils.isNotEmpty(media.id)) {
            media.image = getImage(media.id);
        }
    }

    public Binary getImage(String code) {
        if (StringUtils.isEmpty(code))
            return null;
        return getCollection().findOne("{code:#}", code).as(Media.class).image;
    }

}
