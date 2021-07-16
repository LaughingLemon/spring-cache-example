package org.lemon.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lemon.model.InventoryItem;
import org.lemon.model.QInventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository repository;

    @Test
    public void testSpecification() {
        QInventoryItem item = QInventoryItem.inventoryItem;
        Iterable<InventoryItem> results =
                repository.findAll(item.name.containsIgnoreCase("N")
                        .and(item.name.containsIgnoreCase("e")));
        assertThat(results).extracting("name").contains("Nicolle Bradley");
    }

}