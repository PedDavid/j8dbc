package pt.peddavid.j8dbc.function;

import java.sql.SQLException;

/** Interface to set up the prepared statement
 *      ex: Setting the variable arguments up
 */
@FunctionalInterface
public interface SQLConsumer<E> {

    void set(E prepStmt) throws SQLException;

    default SQLConsumer<E> andThen(SQLConsumer<E> builder){
        return (prepStmt) -> {
            this.set(prepStmt);
            builder.set(prepStmt);
        };
    }
}
