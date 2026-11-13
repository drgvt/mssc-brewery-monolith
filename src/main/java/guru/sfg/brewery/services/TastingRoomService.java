/*
 * Этот сервис раз в 2 секунды автоматически создает случайный заказ от имени клиента
 * «Tasting Room» (сущность в БД), чтобы имитировать активность пользователей
 */

package guru.sfg.brewery.services;

import guru.sfg.brewery.bootstrap.DefaultBreweryLoader;
import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.domain.Customer;
import guru.sfg.brewery.repositories.BeerOrderRepository;
import guru.sfg.brewery.repositories.BeerRepository;
import guru.sfg.brewery.repositories.CustomerRepository;
import guru.sfg.brewery.services.BeerOrderService;
import guru.sfg.brewery.web.model.BeerOrderDto;
import guru.sfg.brewery.web.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;

    private final BeerRepository beerRepository;
    private final BeerOrderRepository beerOrderRepository;

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService, BeerRepository beerRepository,
                              BeerOrderRepository beerOrderRepository) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;
        this.beerRepository = beerRepository;
        this.beerOrderRepository = beerOrderRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 2000) //run every 2 seconds
    public void placeTastingRoomOrder(){

        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(DefaultBreweryLoader.TASTING_ROOM); // // ищем в БД клиента с именем "Tasting Room"

        if (customerList.size() == 1){ //should be just one - если такой клиент ровно один (как и задумано)
            doPlaceOrder(customerList.get(0)); // // создаем от его имени случайный заказ
        } else {
            log.error("Too many or too few tasting room customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
        Beer beerToOrder = getRandomBeer();

        BeerOrderLineDto beerOrderLine = BeerOrderLineDto.builder() // создаем одну позицию в заказе (строку)
                .beerId(beerToOrder.getId())
                .orderQuantity(new Random().nextInt(6)) //todo externalize value to property (вынести это число в application.properties), пока задаем случайное количество от 0 до 5
                .build();

        List<BeerOrderLineDto> beerOrderLineSet = new ArrayList<>(); // создаем список для позиций заказа
        beerOrderLineSet.add(beerOrderLine); // добавляем созданную позицию в список

        BeerOrderDto beerOrder = BeerOrderDto.builder() // создаем сам заказ
                .customerId(customer.getId()) // привязываем заказ к конкретному клиенту
                .customerRef(UUID.randomUUID().toString()) // генерируем уникальный номер (ссылку) заказа
               // .orderStatusCallbackUrl("http://localhost:8080/beerorder") //todo update
                .beerOrderLines(beerOrderLineSet) // кладем в заказ список наших позиций
                .build();

        BeerOrderDto savedOrder = beerOrderService.placeOrder(customer.getId(), beerOrder); // отправляем заказ в сервис для сохранения в БД

        log.debug("Saved Tasting Room Order: " + savedOrder.getId());
    }

    private Beer getRandomBeer() {
        List<Beer> beers = beerRepository.findAll(); // достаем все сорта пива из базы данных

        return beers.get(new Random().nextInt(beers.size() -0)); // выбираем и возвращаем случайный сорт из списка
    }
}
