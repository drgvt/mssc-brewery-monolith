package guru.sfg.brewery.web.mappers;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.web.model.BeerDto;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-14T00:52:20+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21 (Amazon.com Inc.)"
)
@Component
@Primary
public class BeerMapperImpl extends BeerMapperDecorator {

    @Autowired
    @Qualifier("delegate")
    private BeerMapper delegate;

    @Override
    public Beer beerDtoToBeer(BeerDto beerDto)  {
        return delegate.beerDtoToBeer( beerDto );
    }
}
