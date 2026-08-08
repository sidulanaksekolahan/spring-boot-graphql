package com.codewithirfan.service;

import com.codewithirfan.dto.ProductReqDto;
import com.codewithirfan.dto.ProductResDto;
import com.codewithirfan.entity.Product;
import com.codewithirfan.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository  productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResDto> findAll() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(
                        product -> new ProductResDto(
                                product.getName(),
                                product.getDescription(),
                                product.getPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResDto findById(Integer id) {
        Product existingProduct = productRepository.findById(id).orElseThrow();

        return new ProductResDto(
                existingProduct.getName(),
                existingProduct.getDescription(),
                existingProduct.getPrice()
        );
    }

    @Override
    public ProductResDto save(ProductReqDto productReqDto) {
        Product savedProduct = productRepository.save(new Product(
                productReqDto.getName(),
                productReqDto.getDescription(),
                productReqDto.getPrice()
        ));

        return new ProductResDto(
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice()
        );
    }

    @Override
    public ProductResDto update(Integer id, ProductReqDto productReqDto) {
        Product existingProduct = productRepository.findById(id).orElseThrow();

        if (existingProduct != null) {
            existingProduct.setName(productReqDto.getName());
            existingProduct.setPrice(productReqDto.getPrice());
            existingProduct.setDescription(productReqDto.getDescription());

            Product savedProduct = productRepository.save(existingProduct);

            return new ProductResDto(savedProduct.getName(), savedProduct.getDescription(), savedProduct.getPrice());
        }

        return null;
    }

    @Override
    public Boolean delete(Integer id) {
        Product existingProduct = productRepository.findById(id).orElseThrow();

        if (existingProduct != null) {
            productRepository.delete(existingProduct);

            return true;
        }

        return false;
    }
}
