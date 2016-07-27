package pt.peddavid.j8dbc.function;

import java.sql.SQLException;

/** <p>Interface that maps a row of the result set</p>
 * <b> Note(In method mapRow(ResultSet, int)): </b>
 *  rs.next() should never be called during the user implementation of this method,
 *  unless he's aware of the consequences (row skipping)</p>
 * @param <E> Type of the returned object
 */
@FunctionalInterface
public interface RowMapper<E, R> {

    R map(E element) throws SQLException;

    default <R1>RowMapper<E, R1> andThen(RowMapper<R, R1> after){
        return (elem) -> after.map(this.map(elem));
    }
}
