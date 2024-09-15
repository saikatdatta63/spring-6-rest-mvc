package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BeerService beerService;

    @Autowired
    private ObjectMapper objectMapper;

    BeerServiceImpl beerServiceImpl;

    @Captor
    ArgumentCaptor<Beer> beerArgumentCaptor;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @BeforeEach
    void setup() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void getBeerById() throws Exception {

        Beer beer = beerServiceImpl.listBeers().getFirst();
        given(beerService.findBeerById(beer.getId())).willReturn(beer);

        mockMvc.perform(get(BeerController.BEER_PATH_PARAM, beer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(beer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(beer.getBeerName())));

    }


    @Test
    void listBeers() throws Exception {
        given(beerService.listBeers()).willReturn(beerServiceImpl.listBeers());

        mockMvc.perform(get(BeerController.BEER_PATH).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void createNewBeer() throws Exception {
        Beer beer = beerServiceImpl.listBeers().getFirst();
        beer.setId(null);
        beer.setVersion(null);
        given(beerService.createNewBeer(any(Beer.class))).willReturn(beerServiceImpl.listBeers().get(1));
        mockMvc.perform(post(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void updateBeerById() throws Exception {
        Beer beer = beerServiceImpl.listBeers().getFirst();
        mockMvc.perform(put(BeerController.BEER_PATH_PARAM, beer.getId())
                        .content(objectMapper.writeValueAsString(beer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(beerService).updateBeerById(eq(beer.getId()), any(Beer.class));
    }

    @Test
    void deleteBeerById() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete(BeerController.BEER_PATH_PARAM, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(beerService).deleteById(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void patchBeerById() throws Exception {
        UUID id = UUID.randomUUID();
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New name");
        mockMvc.perform(patch(BeerController.BEER_PATH_PARAM, id)
                        .content(objectMapper.writeValueAsString(beerMap))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).isEqualTo(id);
        assertThat(beerArgumentCaptor.getValue().getBeerName()).isEqualTo(beerMap.get("beerName"));
    }
}