package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.exception.NotFoundException;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerController beerController;
    @Autowired
    BeerMapper beerMapper;

    @Test
    void testListBeer() {
        List<BeerDTO> beerDTOS = beerController.listBeers();
        assertThat(beerDTOS.size()).isEqualTo(3);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyList() {
        beerRepository.deleteAll();
        List<BeerDTO> beerDTOS = beerController.listBeers();
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
                () ->beerController.updateBeerById(UUID.randomUUID(), null));
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
        beerDTO.setPrice(BigDecimal.valueOf(32.99));
        ResponseEntity responseEntity = beerController.patchBeerById(beer.getId(), beerDTO);
        Beer patchedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(patchedBeer.getPrice()).isEqualTo(beerDTO.getPrice());
        assertThat(patchedBeer.getBeerName()).isEqualTo(beer.getBeerName());
    }

    @Test
    void testPatchNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.patchBeerById(UUID.randomUUID(), null));
    }
}