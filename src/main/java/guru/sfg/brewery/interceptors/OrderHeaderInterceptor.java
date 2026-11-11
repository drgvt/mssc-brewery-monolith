/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
* Hibernate-перехватчик, который при каждом сохранении заказа (BeerOrder)
* сравнивает его текущий статус с предыдущим и, если они отличаются, генерирует
* событие BeerOrderStatusChangeEvent для остальной системы.
* Перехватчик мы нигде явно в коде не используем - он сам срабатывает когда Hibernate
* готовит SQL-запрос на обновление (UPDATE) этой записи в базе (UPDATE запускает onFlushDirty)
*/

package guru.sfg.brewery.interceptors;

import guru.sfg.brewery.domain.BeerOrder;
import guru.sfg.brewery.domain.OrderStatusEnum;
import guru.sfg.brewery.events.BeerOrderStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Catch order updates
 */
@Slf4j
@Component
public class OrderHeaderInterceptor extends EmptyInterceptor { // Перехватчик Hibernate

    private final ApplicationEventPublisher publisher; // Публикатор событий

    public OrderHeaderInterceptor(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /*
     * Flush — это момент, когда Hibernate синхронизирует (сбрасывает) изменения из памяти в базу данных.
     * А onFlushDirty переводится как «при сбросе (flush) грязной (dirty) сущности». В Hibernate «грязная»
     *  (dirty) сущность — это объект, поля которого были изменены и требуют сохранения в БД. То есть метод
     * срабатывает ровно перед тем, как Hibernate обновит измененные данные в базе.
     * Метод вызывается перед обновлением сущности в БД
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {

        if (entity instanceof BeerOrder){ // Если обновляется именно заказ
            /*
             * entity — это сам объект заказа (класс BeerOrder), а currentState — это массив
             * всех его полей (значений), который Hibernate передает в перехватчик для сравнения
             */
            for(Object curObj : currentState){ // Перебираем текущие значения полей
                if(curObj instanceof OrderStatusEnum){
                    for (Object prevObj : previousState){
                        if (prevObj instanceof OrderStatusEnum) {
                            OrderStatusEnum curStatus = (OrderStatusEnum) curObj; // Новый статус
                            OrderStatusEnum prevStatus = (OrderStatusEnum) prevObj; // Старый статус

                            if(curStatus != prevStatus){ // Если статус изменился
                                log.debug("Order status change detected");

                                /* Публикуем событие со старым статусом. Со старым - чтобы слушатель события знал,
                                 * из какого состояния перешел заказ (например, чтобы отличить переход NEW -> READY от
                                 * CANCELLED -> READY), и мог выполнить нужную бизнес-логику в зависимости от прошлого статуса
                                 */
                                publisher.publishEvent(new BeerOrderStatusChangeEvent((BeerOrder) entity, prevStatus));
                            }
                        }
                    }
                }
            }
        }

        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types); // Передаем управление дальше
    }
}
