package com.mycompany.service.shop;

import com.mycompany.domain.shop.District;
import com.mycompany.domain.shop.DistrictForList;
import com.mycompany.service.AbstractService;
import org.bson.types.ObjectId;


public class DistrictService extends AbstractService<DistrictForList> {

    private Class listClass = DistrictForList.class;

    public DistrictService() {
        super(DistrictForList.class);
    }

    @Override
    public District getById(String id)
    {
        return getCollection().findOne(new ObjectId(id)).as(District.class);
    }

    public DistrictForList getByIdShort(String id)
    {
        return getCollection().findOne(new ObjectId(id)).as(DistrictForList.class);
    }
}
