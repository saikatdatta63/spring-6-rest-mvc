package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.exception.NotFoundException;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory) {
        List<Beer> beers;

        if (StringUtils.hasText(beerName) && null==beerStyle) {
            beers = listBeerByName(beerName);
        }
        else if(!StringUtils.hasText(beerName) && null!=beerStyle){
            beers = listBeerByStyle(beerStyle);
        }
        else if(StringUtils.hasText(beerName) && null != beerStyle){
            beers = listBeerByNameAndStyle(beerName, beerStyle);
        }
        else {
            beers = beerRepository.findAll();
        }

        if(null != showInventory && !showInventory) {
            beers.forEach(beer -> beer.setQuantityOnHand(null));
        }

        return beers
                .stream()
                .map(beerMapper::beerToBeerDTO)
                .collect(Collectors.toList());
    }

    private List<Beer> listBeerByNameAndStyle(String beerName, BeerStyle beerStyle) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle(
                "%" + beerName + "%", beerStyle);
    }

    private List<Beer> listBeerByStyle(BeerStyle beerStyle) {
        return beerRepository.findAllByBeerStyle(beerStyle);
    }

    private List<Beer> listBeerByName(String beerName) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%");
    }

    @Override
    public Optional<BeerDTO> findBeerById(UUID id) {
        return beerRepository.findById(id)
                .map(beerMapper::beerToBeerDTO);
    }

    @Override
    @Transactional
    public BeerDTO createNewBeer(BeerDTO beer) {
        Beer beerToSave = beerMapper.beerDtoToBeer(beer);
        beerToSave.setCreatedDate(LocalDateTime.now());
        Beer savedBeer = beerRepository.save(beerToSave);
        return beerMapper.beerToBeerDTO(savedBeer);
    }

    @Override
    @Transactional
    public void updateBeerById(UUID beerId, BeerDTO beer) {
        Optional<Beer> existingBeer = beerRepository.findById(beerId);
        if (existingBeer.isPresent()) {
            Beer beerToUpdate = existingBeer.get();
            beerToUpdate.setBeerName(beer.getBeerName());
            beerToUpdate.setBeerStyle(beer.getBeerStyle());
            beerToUpdate.setUpc(beer.getUpc());
            beerToUpdate.setPrice(beer.getPrice());
            beerToUpdate.setQuantityOnHand(beer.getQuantityOnHand());
            beerToUpdate.setUpdateDate(LocalDateTime.now());
            beerRepository.save(beerToUpdate);
        } else {
            throw new NotFoundException("Beer not found!");
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID beerId) {
        beerRepository.findById(beerId).ifPresentOrElse(beerRepository::delete,
                () -> {
                    throw new NotFoundException("Beer not found!");
                });
    }

    @Override
    @Transactional
    public void patchBeerById(UUID beerId, BeerDTO beer) {
        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if (StringUtils.hasText(beer.getBeerName())) {
                foundBeer.setBeerName(beer.getBeerName());
            }
            if (StringUtils.hasText(beer.getUpc())) {
                foundBeer.setUpc(beer.getUpc());
            }
            if (null != beer.getBeerStyle()) {
                foundBeer.setBeerStyle(beer.getBeerStyle());
            }
            if (null != beer.getPrice()) {
                foundBeer.setPrice(beer.getPrice());
            }
            if (null != beer.getQuantityOnHand()) {
                foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            }
            foundBeer.setUpdateDate(LocalDateTime.now());
            beerRepository.save(foundBeer);
            beerRepository.flush();
        }, () -> {
            throw new NotFoundException("Beer not found!");
        });
    }
}
