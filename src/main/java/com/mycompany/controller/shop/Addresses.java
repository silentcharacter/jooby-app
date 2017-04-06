
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Address;
import com.mycompany.domain.shop.Color;
import com.mycompany.service.shop.AddressService;
import com.mycompany.service.shop.ColorService;


public class Addresses extends AbstractResource<Address> {

    public Addresses() {
        super(Address.class, AddressService.class);
    }
}
