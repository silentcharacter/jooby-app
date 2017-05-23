package com.mycompany.service.shop;

import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;
import com.mycompany.service.SearchResult;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.List;
//import org.apache.commons.codec.binary.Base64;


public class ProductService extends AbstractService<Product> {

    public ProductService() {
        super(Product.class);
    }

    @Override
    public void onSave(Product product) {
        if (product.image == null) {
            product.image = getProductImage(product.id);
        }
    }

    public Binary getProductImage(String id) {
        return getCollection().findOne(new ObjectId(id)).as(Product.class).image;
    }

    @Override
    protected Product listReaderCallback(Product product) {
        product.image = null;
        product.description = null;
        return product;
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
