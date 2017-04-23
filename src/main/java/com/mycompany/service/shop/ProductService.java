package com.mycompany.service.shop;

import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;
//import org.apache.commons.codec.binary.Base64;


public class ProductService extends AbstractService<Product> {

    public ProductService() {
        super(Product.class);
    }

    @Override
    public void onSave(Product product)
    {
        if (product.image == null) {
            Product old = getById(product.id);
            product.image = old.image;
        }
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
