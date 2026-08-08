package com.codewithirfan.service;

import com.codewithirfan.dto.ProductReqDto;
import com.codewithirfan.dto.ProductResDto;

import java.util.List;

public interface ProductService {

    List<ProductResDto> findAll();

    ProductResDto findById(Integer id);

    ProductResDto save(ProductReqDto productReqDto);

    ProductResDto update(Integer id, ProductReqDto productReqDto);

    Boolean delete(Integer id);
}
