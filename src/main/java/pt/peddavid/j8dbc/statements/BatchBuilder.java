package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;

import java.util.Collection;

public class BatchBuilder extends BaseBatch {

    public BatchBuilder(ConnectionService service, String query) {
        super(service, query);
    }

    public <E>BatchFromCollection<E> fromCollection(Collection<E> collection){
        return new BatchFromCollection<>(service, query, collection);
    }
}
