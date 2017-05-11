
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.District;
import com.mycompany.domain.shop.DistrictForList;
import com.mycompany.service.shop.DistrictService;


public class Districts extends AbstractResource<DistrictForList> {

    public Districts() {
        super(DistrictForList.class, DistrictService.class);
    }
}
