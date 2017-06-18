package com.mycompany.service.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;
import com.mycompany.service.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
//import org.apache.commons.codec.binary.Base64;


public class ProductService extends AbstractService<Product> {

    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    private UnitService unitService;

    public ProductService() {
        super(Product.class);
    }

    @Override
    public void onSave(Product product) {
        if (product.image == null && StringUtils.isNotEmpty(product.id)) {
            product.image = getProductImage(product.id);
        }
    }

    public Binary getProductImage(String id) {
        if (StringUtils.isEmpty(id))
            return null;
        return getCollection().findOne(new ObjectId(id)).as(Product.class).image;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object listReaderCallback(Product product) {
        product.image = null;
        product.description = null;
        Map<String, Object> map = mapper.convertValue(product, Map.class);
        map.put("unit", unitService.getById(product.unitId));
        return map;
    }

    //    @Override
//    public Product getById(String id)
//    {
//        Product product = super.getById(id);
//        if (product.image != null)
//        {
//            byte c[] = product.image.getData();
//            product.base64 = new String(Base64.encodeBase64(c));
//        }
//        return product;
//    }
}
