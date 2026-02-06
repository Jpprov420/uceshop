package com.uceshop.catalog.api;

import com.uceshop.catalog.domain.Item;
import com.uceshop.catalog.repo.ItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final ItemRepository repo;

    public CatalogController(ItemRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/items")
    public List<Item> items() {
        return repo.findAll();
    }
}
