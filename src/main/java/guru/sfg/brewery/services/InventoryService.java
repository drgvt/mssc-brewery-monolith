/*
 * Этот сервис каждые 5 секунд проверяет склад: суммирует остатки
 * каждого сорта пива, и если их количество падает до минимума (minOnHand),
 * публикует событие BrewBeerEvent, чтобы запустить варку новой партии.
 */

package guru.sfg.brewery.services;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.events.BrewBeerEvent;
import guru.sfg.brewery.repositories.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class InventoryService {

    private final BeerRepository beerRepository;
    private final ApplicationEventPublisher publisher;

    public InventoryService(BeerRepository beerRepository, ApplicationEventPublisher publisher) {
        this.beerRepository = beerRepository;
        this.publisher = publisher;
    }

    @Transactional // весь метод должен выполняться как одна транзакция: либо все изменения в БД сохранятся вместе, либо откатятся вместе при ошибке
    @Scheduled(fixedRate = 5000) //run every 5 seconds
    public void checkInventory(){
        List<Beer> beers = beerRepository.findAll();

        beers.forEach(beer -> {
            AtomicInteger inventory_qoh = new AtomicInteger();

            /*
             * Он перебирает все партии пива на складе и суммирует их количество (quantityOnHand)
             * в общий счетчик inventory_qoh, т.к. BeerInventory — это не «сколько всего пива», а запись
             * о конкретной сваренной партии, и таких партий (с разными датами и объемами) у одного
             * сорта пива может быть сколько угодно
             */
            beer.getBeerInventory().forEach(inv -> inventory_qoh.addAndGet(inv.getQuantityOnHand()));

            /*
             * Это отладочный лог, чтобы во время работы приложения видеть, сколько пива каждого сорта
             * осталось на складе (он выводится только если включен уровень логирования DEBUG)
             */
            log.debug("Inv: " + beer.getBeerName() + " : QOH = " + inventory_qoh.get());

            if(beer.getMinOnHand() >= inventory_qoh.get() ) {
                //brew beer
                publisher.publishEvent(new BrewBeerEvent(beer)); // event - просто новость, паблишер ее публикует
            }
        });
     }
}
