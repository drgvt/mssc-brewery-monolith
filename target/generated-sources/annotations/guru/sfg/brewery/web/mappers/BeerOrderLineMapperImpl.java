package guru.sfg.brewery.web.mappers;

import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-01T14:53:19+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21 (Amazon.com Inc.)"
)
public class BeerOrderLineMapperImpl extends BeerOrderLineMapperDecorator {

    private final BeerOrderLineMapper delegate;

    public BeerOrderLineMapperImpl() {
        this( new BeerOrderLineMapperImpl_() );
    }

    private BeerOrderLineMapperImpl(BeerOrderLineMapperImpl_ delegate) {
        this.delegate = delegate;
    }
}
