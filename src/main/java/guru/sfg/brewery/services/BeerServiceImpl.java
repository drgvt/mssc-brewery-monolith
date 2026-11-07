/*
 * Этот класс — клиент, который по HTTP обращается к внешнему сервису пива (Beer Service) по
 * адресу из настроек, чтобы получить данные о конкретном пиве по его ID.
 * В монолите сервисы обычно работают с БД, но здесь BeerServiceImpl — это заготовка
 * на будущее: после декомпозиции на микросервисы этот класс станет клиентом, который ходит за
 * пивом в отдельный Beer Service, а пока он просто демонстрирует, как это будет устроено.
 */

package guru.sfg.brewery.services;

import guru.sfg.brewery.web.model.BeerDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Service
public class BeerServiceImpl implements BeerService {

    public final String BEER_PATH_V1 = "/api/v1/beer/";
    private final RestTemplate restTemplate; // // HTTP-клиент из Spring, который позволяет Java-коду отправлять запросы (GET, POST и т.д.) на другие сервисы и получать от них ответы

    private String beerServiceHost;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID uuid){
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 + uuid.toString(), BeerDto.class));
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }
}
