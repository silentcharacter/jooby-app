package com.mycompany.service.shop;

import com.mycompany.domain.shop.Unit;
import com.mycompany.service.AbstractService;

import java.util.Map;
import java.util.stream.Collectors;


public class UnitService extends AbstractService<Unit> {

    public UnitService() {
        super(Unit.class);
    }

    public Map<String, String> getLabelsMap() {
        return getAll().stream().collect(Collectors.toMap(u -> u.id, u -> u.label));
    }
}
