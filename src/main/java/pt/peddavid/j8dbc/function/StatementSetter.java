package pt.peddavid.j8dbc.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementSetter<E> {

    void set(PreparedStatement prepStmt, E element) throws SQLException;

    default StatementSetter<E> andThen(StatementSetter<E> batchSetter){
        return (prepStmt, element) -> {
            this.set(prepStmt, element);
            batchSetter.set(prepStmt, element);
        };
    }
}
