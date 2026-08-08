package com.codewithirfan.controller;

import com.codewithirfan.dto.ProductReqDto;
import com.codewithirfan.dto.ProductResDto;
import com.codewithirfan.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProductGraphqlController {

    private final ProductService  productService;

    @Autowired
    public ProductGraphqlController(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public List<ProductResDto> products() {
        return productService.findAll();
    }

    @QueryMapping
    public ProductResDto product(@Argument("id") Integer id) {
        return productService.findById(id);
    }

    @MutationMapping
    public ProductResDto createProduct(@Argument("input") ProductReqDto productReqDto) {
        return productService.save(productReqDto);
    }

    @MutationMapping
    public ProductResDto updateProduct(@Argument Integer id, @Argument("reqDto") ProductReqDto productReqDto) {
        return productService.update(id, productReqDto);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument Integer id) {

        Boolean result = productService.delete(id);

        return result;
    }
}
