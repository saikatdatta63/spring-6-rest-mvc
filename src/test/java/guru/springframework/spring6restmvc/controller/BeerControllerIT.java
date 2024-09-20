package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.exception.NotFoundException;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.core.Is.is;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerController beerController;
    @Autowired
    BeerMapper beerMapper;

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testGetBeerbyName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "City"))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void testGetBeerByStyle() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerStyle", "PALE_ALE"))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @Test
    void testGetBeerByNameAndStyleAndShowInventoryTrue() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerName", "City")
                .queryParam("beerStyle", "IPA")
                .queryParam("showInventory", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.notNullValue()));

    }

    @Test
    void testGetBeerByNameAndStyleAndShowInventoryFalse() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "City")
                        .queryParam("beerStyle", "IPA")
                        .queryParam("showInventory", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.nullValue()));

    }

    @Test
    void testListBeer() {
        List<BeerDTO> beerDTOS = beerController.listBeers(null, null, false);
        assertThat(beerDTOS.size()).isEqualTo(3);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyList() {
        beerRepository.deleteAll();
        List<BeerDTO> beerDTOS = beerController.listBeers(null, null, false);
        assertThat(beerDTOS).isEmpty();
    }

    @Test
    void testGetBeerById() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO beerDto = beerController.getBeerById(beer.getId());
        assertThat(beerDto).isNotNull();
    }

    @Test
    void testGetBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.getBeerById(UUID.randomUUID()));
    }

    @Transactional
    @Rollback
    @Test
    void testSaveNewBeer() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("Test Beer")
                .build();
        ResponseEntity beerResponse = beerController.createBeer(beerDTO);
        assertThat(beerResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertNotNull(beerResponse.getHeaders().getLocation());

        String[] locationUUID = beerResponse.getHeaders().getLocation().getPath().split("/");
        UUID savedId = UUID.fromString(locationUUID[4]);
        Optional<Beer> savedBeer = beerRepository.findById(savedId);
        assertThat(savedBeer).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateBeerById() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        beerDTO.setBeerName("Updated Beer");
        ResponseEntity responseEntity = beerController.updateBeerById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerDTO.getBeerName());
        assertThat(updatedBeer.getBeerStyle()).isEqualTo(beer.getBeerStyle());
    }

    @Test
    void testBeerUpdateNotFound() {
        assertThrows(NotFoundException.class,
                () -> beerController.updateBeerById(UUID.randomUUID(), null));
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteById() {
        Beer beer = beerRepository.findAll().getFirst();
        ResponseEntity responseEntity = beerController.deleteBeerById(beer.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(beerRepository.findById(beer.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.deleteBeerById(UUID.randomUUID()));
    }

    @Test
    @Transactional
    @Rollback
    void testPatchById() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        beerDTO.setBeerName("Patched");
        beerDTO.setPrice(BigDecimal.valueOf(32.99));
        ResponseEntity responseEntity = beerController.patchBeerById(beer.getId(), beerDTO);
        Beer patchedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(patchedBeer.getPrice()).isEqualTo(beerDTO.getPrice());
        assertThat(patchedBeer.getBeerName()).isEqualTo(beerDTO.getBeerName());
    }

    @Test
    void testPatchNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.patchBeerById(UUID.randomUUID(), null));
    }
}