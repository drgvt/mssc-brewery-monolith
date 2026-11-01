package guru.sfg.brewery.web.mappers;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.web.model.BeerDto;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-01T14:53:19+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21 (Amazon.com Inc.)"
)
public class BeerMapperImpl extends BeerMapperDecorator {

    private final BeerMapper delegate;

    public BeerMapperImpl() {
        this( new BeerMapperImpl_() );
    }

    private BeerMapperImpl(BeerMapperImpl_ delegate) {
        this.delegate = delegate;
    }

    @Override
    public Beer beerDtoToBeer(BeerDto beerDto)  {
        return delegate.beerDtoToBeer( beerDto );
    }
}
