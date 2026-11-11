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
 * Перехватывает событие об изменении статуса заказа, собирает из данных заказа
 * объект OrderStatusUpdate и отправляет его по HTTP (через RestTemplate) на URL,
 * указанный в orderStatusCallbackUrl, чтобы оповестить внешнюю систему.
 * Сам монолит никак не реагирует на эти оповещения — он просто отправляет
 * HTTP-запрос наружу (на orderStatusCallbackUrl) и забывает о нем.
 */

package guru.sfg.brewery.listeners;

import guru.sfg.brewery.events.BeerOrderStatusChangeEvent;
import guru.sfg.brewery.web.mappers.DateMapper;
import guru.sfg.brewery.web.model.OrderStatusUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class BeerOrderStatusChangeEventListener {

    RestTemplate restTemplate; // HTTP-клиент
    DateMapper dateMapper = new DateMapper(); // маппер дат (вручную, не бин)

    public BeerOrderStatusChangeEventListener(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build(); // создает RestTemplate
    }

    @Async // выполняется в отдельном потоке
    @EventListener
    public void listen(BeerOrderStatusChangeEvent event){ // метод-обработчик

        OrderStatusUpdate update = OrderStatusUpdate.builder() // создаем DTO для отправки
                .id(event.getBeerOrder().getId())
                .orderId(event.getBeerOrder().getId()) // дубль, но в реальном проекте могут отличаться
                .version(event.getBeerOrder().getVersion() != null ? event.getBeerOrder().getVersion().intValue() : null) // версия (если есть)
                .createdDate(dateMapper.asOffsetDateTime(event.getBeerOrder().getCreatedDate())) // конвертируем дату создания
                .lastModifiedDate(dateMapper.asOffsetDateTime(event.getBeerOrder().getLastModifiedDate())) // конвертируем дату изменения
                .orderStatus(event.getBeerOrder().getOrderStatus().toString()) // текущий статус
                .customerRef(event.getBeerOrder().getCustomerRef()) // номер заказа клиента
                .build();

        try{
            log.debug("Posting to callback url");
            if (event.getBeerOrder().getOrderStatusCallbackUrl() != null) {
                restTemplate.postForObject(event.getBeerOrder().getOrderStatusCallbackUrl(), update, String.class); // если URL для callback задан
            }
        } catch (Throwable t){
            log.error("Error Preforming callback for order: " + event.getBeerOrder().getId(), t);
        }
    }
}
