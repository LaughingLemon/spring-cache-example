package org.lemon.controller;

import org.lemon.model.InventoryItem;
import org.lemon.services.ExternalInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CacheController {

    @Autowired
    private ExternalInventoryService service;

    @GetMapping("/products")
    public List<InventoryItem> getProducts() {
        return service.getProducts();
    }

}
