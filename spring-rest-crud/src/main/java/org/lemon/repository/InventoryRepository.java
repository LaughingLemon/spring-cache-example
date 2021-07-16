package org.lemon.repository;

import org.lemon.model.InventoryItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

@Repository
public interface InventoryRepository extends
        CrudRepository<InventoryItem, Long>, QuerydslPredicateExecutor<InventoryItem> {
}
