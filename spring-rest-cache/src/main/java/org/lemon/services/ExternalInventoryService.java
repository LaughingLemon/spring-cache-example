package org.lemon.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lemon.model.InventoryItem;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ExternalInventoryService {
    private final RestTemplate restTemplate;

    public ExternalInventoryService(RestTemplateBuilder restTemplateBuilder) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);
        this.restTemplate = restTemplateBuilder.messageConverters(converter).build();
    }

    @Cacheable("inventoryItems")
    public List<InventoryItem> getProducts() {
        ResponseEntity<Resources<InventoryItem>> responseEntity =
                restTemplate.exchange(
                        "http://localhost:8089/inventoryItems",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Resources<InventoryItem>>() {});
        log.info(responseEntity.getStatusCode().toString());
        Resources<InventoryItem> resources = responseEntity.getBody();
        log.info(resources.toString());
        List<InventoryItem> customers = new ArrayList(resources.getContent());

        return customers;
    }

}
