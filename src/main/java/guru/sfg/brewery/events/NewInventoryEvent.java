/*
Это событие создается, когда на склад поступает новая партия пива, и оно нужно,
чтобы оповестить остальную систему о том, что запас конкретного пива пополнился.
 */

package guru.sfg.brewery.events;

import guru.sfg.brewery.domain.Beer;
import org.springframework.context.ApplicationEvent;

public class NewInventoryEvent extends ApplicationEvent {

    public NewInventoryEvent(Beer source) {
        super(source);
    }

    public Beer getBeer(){
        return (Beer) this.source;
    }
}
