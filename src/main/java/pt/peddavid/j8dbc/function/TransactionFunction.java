package pt.peddavid.j8dbc.function;

import pt.peddavid.j8dbc.ConnectionService;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionFunction<E> {

    E apply(ConnectionService trans) throws SQLException;

}
