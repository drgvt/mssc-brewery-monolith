package guru.sfg.brewery.listeners;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.domain.BeerInventory;
import guru.sfg.brewery.events.BrewBeerEvent;
import guru.sfg.brewery.events.NewInventoryEvent;
import guru.sfg.brewery.repositories.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
/*
 * Да, внутри Spring хранится список (коллекция) всех слушателей для каждого
 * типа события, и при публикации Spring перебирает этот список, передавая
 * объект события в каждый подходящий метод с @EventListener
 */

@Slf4j
@Component
public class BrewBeerListener {

    private final BeerRepository beerRepository;
    private final ApplicationEventPublisher publisher;

    public BrewBeerListener(BeerRepository beerRepository, ApplicationEventPublisher publisher) {
        this.beerRepository = beerRepository;
        this.publisher = publisher;
    }

    @Async
    @EventListener
    @Transactional
    public void listen(BrewBeerEvent event){
        log.debug("Brewing Beer for " + event.getBeer().getBeerName() );

        Beer beer = beerRepository.getOne(event.getBeer().getId()); // // получаем пиво из БД по его ID

        BeerInventory beerInventory = BeerInventory.builder() // // создаем новую партию пива
                                        .beer(beer)
                                        .quantityOnHand(beer.getQuantityToBrew())
                                        .build();

        beer.getBeerInventory().add(beerInventory); //  сначала получаем коллекцию партий пива, а затем добавляем в неё новую партию

        Beer savedBeer = beerRepository.save(beer); // сохраняем пиво в БД (каскадно добавится и новая партия из списка)

        log.debug("Brewed some beer! " + savedBeer.getBeerName());
        publisher.publishEvent(new NewInventoryEvent(beer));
    }
}
