/*
Это событие создается, когда клиент оформляет новый заказ, и оно нужно,
чтобы оповестить остальную систему (склад, уведомления) о том, что появился
новый заказ и с ним нужно что-то сделать.
 */

package guru.sfg.brewery.events;

import guru.sfg.brewery.domain.BeerOrder;
import org.springframework.context.ApplicationEvent;

public class NewBeerOrderEvent extends ApplicationEvent {

    public NewBeerOrderEvent(BeerOrder source) {
        super(source);
    }

    public BeerOrder getBeerOrder(){
        return (BeerOrder) this.source;
    }
}
