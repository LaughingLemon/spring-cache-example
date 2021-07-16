package org.lemon;

import javafx.beans.binding.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lemon.model.InventoryItem;
import org.lemon.model.QInventoryItem;
import org.lemon.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEndpoint() throws Exception {
        InputStream fileStream = ClassLoader.getSystemResourceAsStream("New-API.v1.json");

        String expectedSchema = new BufferedReader(new InputStreamReader(fileStream)).lines().collect(Collectors.joining());

        mockMvc.perform(get("/inventoryItems"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(expectedSchema));
    }

}
