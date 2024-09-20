package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.bootstrap.BootstrapData;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(BootstrapData.class)
class BeerRepositoryTest {
    @Autowired
    BeerRepository beerRepository;

    @Test
    void testGetBeerByName() {
        List<Beer> beers = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%City%");
        assertThat(beers.size()).isEqualTo(1);
    }

    @Test
    void testSaveBeer() {
        Beer testBeer = beerRepository.save(Beer.builder()
                .beerName("Test beer")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("13124")
                .price(new BigDecimal("11.99"))
                .build());

        beerRepository.flush();

        assertNotNull(testBeer);
        assertNotNull(testBeer.getId());
    }


}