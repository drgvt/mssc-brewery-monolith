package guru.sfg.brewery.web.mappers;

import guru.sfg.brewery.domain.BeerOrder;
import guru.sfg.brewery.web.model.BeerOrderDto;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-01T14:53:19+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21 (Amazon.com Inc.)"
)
public class BeerOrderMapperImpl implements BeerOrderMapper {

    @Override
    public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
        if ( beerOrder == null ) {
            return null;
        }

        BeerOrderDto beerOrderDto = new BeerOrderDto();

        return beerOrderDto;
    }

    @Override
    public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerOrder beerOrder = new BeerOrder();

        return beerOrder;
    }
}
