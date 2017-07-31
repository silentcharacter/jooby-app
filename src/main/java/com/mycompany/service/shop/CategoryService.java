package com.mycompany.service.shop;

import com.google.inject.Inject;
import com.mycompany.domain.shop.Category;
import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CategoryService extends AbstractService<Category> {

    @Inject
    public ProductService productService;

    public CategoryService() {
        super(Category.class);
    }

    public List<Pair<Category, List<Product>>> getAllWithProducts() {
        return getAll().stream().map(c ->
              new ImmutablePair<>(c, productService.getAll("{categoryId:'" + c.id + "', active: true}")))
              .collect(Collectors.toList());
    }
}
