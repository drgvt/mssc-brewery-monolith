/*
 * Этот слушатель ловит событие о новом заказе и резервирует (выделяет)
 * под него пиво на складе: он берет остатки из всех партий конкретного пива,
 * списывает их в счет заказа и, если пива хватило на всё, меняет статус заказа на READY
 */

package guru.sfg.brewery.listeners;

import guru.sfg.brewery.domain.BeerInventory;
import guru.sfg.brewery.domain.BeerOrder;
import guru.sfg.brewery.domain.BeerOrderLine;
import guru.sfg.brewery.domain.OrderStatusEnum;
import guru.sfg.brewery.events.NewBeerOrderEvent;
import guru.sfg.brewery.repositories.BeerInventoryRepository;
import guru.sfg.brewery.repositories.BeerOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class NewBeerOrderListener {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerInventoryRepository beerInventoryRepository;

    public NewBeerOrderListener(BeerOrderRepository beerOrderRepository, BeerInventoryRepository beerInventoryRepository) {
        this.beerOrderRepository = beerOrderRepository;
        this.beerInventoryRepository = beerInventoryRepository;
    }

    /*
     * Он перехватывает событие о новом заказе и пытается зарезервировать (выделить)
     * нужное количество пива на складе для каждой позиции, а если всё заказанное
     * пиво успешно найдено — меняет статус заказа на READY (готов к выдаче).
     * Если пива на складе не хватает, статус заказа останется прежним (NEW), и он
     * будет ждать следующей проверки склада (когда сварят новую партию).
     */
    @Async // // выполняется в отдельном потоке
    @EventListener
    @Transactional
    public synchronized void listen(NewBeerOrderEvent event){ //needed sychronized to prevent errors (от гонки потоков)
        log.debug("Allocating Order: " + event.getBeerOrder().getCustomerRef());

        AtomicInteger totalOrdered = new AtomicInteger(); // счетчик всего заказанного (TestingRoom заказывает под одной позиции, но это на случай реальных заказов, где может быть много позиций разного пива)
        AtomicInteger totalAllocated = new AtomicInteger(); // счетчик всего зарезервированного

        BeerOrder beerOrder = beerOrderRepository.findOneById(event.getBeerOrder().getId()); // достаем заказ из БД (сохранен ранее в БД в BeerOrderServiceImpl)

        if (beerOrder == null){ // если заказ еще не долетел до БД (событие сработало быстрее)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace(); // логируем ошибку прерывания
            }
            //event firing before save is complete
            beerOrder = beerOrderRepository.findOneById(event.getBeerOrder().getId()); // пробуем достать заказ еще раз
        }

        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> { // перебираем строки заказа
            if ((beerOrderLine.getOrderQuantity() - beerOrderLine.getQuantityAllocated()) > 0) { // если не все еще зарезервировано
                allocateBeerOrderLine(beerOrderLine); // запускаем резервацию для этой строки
            }
            totalOrdered.set(totalOrdered.get() + beerOrderLine.getOrderQuantity()); // прибавляем к общему заказанному
            totalAllocated.set(totalAllocated.get() + beerOrderLine.getQuantityAllocated()); // прибавляем к общему зарезервированному
        });

        if(totalOrdered.get() == totalAllocated.get()){ // если зарезервировали ровно столько, сколько заказали
            log.debug("Order Completely Allocated: " + beerOrder.getCustomerRef());
            beerOrder.setOrderStatus(OrderStatusEnum.READY); // меняем статус на READY
        }

        beerOrderRepository.saveAndFlush(beerOrder); // сохраняем заказ в БД
    }

    /*
     * Этот метод берет все партии нужного пива на складе, по очереди
     * вычитает из них заказанное количество (пока не наберет нужное),
     * сохраняет изменения и удаляет те партии, которые опустели до нуля.
     */
    private void allocateBeerOrderLine(BeerOrderLine beerOrderLine) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByBeer(beerOrderLine.getBeer()); // ищем все партии этого пива на складе

        beerInventoryList.forEach(beerInventory -> { // перебираем партии
            int inventory = (beerInventory.getQuantityOnHand() == null) ? 0 : beerInventory.getQuantityOnHand(); // сколько в партии (или 0)
            int orderQty = (beerOrderLine.getOrderQuantity() == null) ? 0 : beerOrderLine.getOrderQuantity() ; // сколько заказано (или 0)
            int allocatedQty = (beerOrderLine.getQuantityAllocated() == null) ? 0 : beerOrderLine.getQuantityAllocated(); // сколько уже зарезервировано (или 0)
            int qtyToAllocate = orderQty - allocatedQty; // сколько еще надо зарезервировать

            if(inventory >= qtyToAllocate){ // full allocation (если в этой одной сваренной партии хватает пива)
                inventory = inventory - qtyToAllocate; // вычитаем из партии
                beerOrderLine.setQuantityAllocated(orderQty); // в строке заказа ставим полную резервацию
                beerInventory.setQuantityOnHand(inventory); // обновляем остаток в партии
            } else if (inventory > 0) { //partial allocation (если в партии есть пиво, но не хватает)
                beerOrderLine.setQuantityAllocated(allocatedQty + inventory); // резервируем сколько есть
                beerInventory.setQuantityOnHand(0); // обнуляем партию (allocated beer "забираем с собой" в объект BeerOrderLine)
            }
        });
        /*
         * Он зарезервирует 4 бутылки (quantityAllocated = 4), оставит заказ в статусе NEW,
         * а разница (1 бутылка) останется недоделанной до тех пор, пока на складе не появится новая партия.
         */

        beerInventoryRepository.saveAll(beerInventoryList); // сохраняем изменения по всем партиям

        beerInventoryList.stream()
                .filter(beerInventory -> beerInventory.getQuantityOnHand() == 0) // находим пустые партии
                .forEach(beerInventory -> beerInventoryRepository.delete(beerInventory)); // удаляем пустые партии из БД

    }
}
