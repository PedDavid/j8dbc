package pt.peddavid.j8dbc;

import pt.peddavid.j8dbc.connections.BaseConnection;
import pt.peddavid.j8dbc.connections.TransactionalConnection;
import pt.peddavid.j8dbc.function.TransactionFunction;
import pt.peddavid.j8dbc.statements.BatchBuilder;
import pt.peddavid.j8dbc.statements.Query;
import pt.peddavid.j8dbc.statements.SimpleBatch;
import pt.peddavid.j8dbc.statements.Update;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionService {

    /** <p>Supplier for the connection</p>
     *  <p>In most of the cases it's implemented as simple as:</p>
     *  () -> new BaseConnection("userConnection")
     *  where userConnection is the connection established by the user
     */
    BaseConnection getConnection() throws SQLException;

    /* Default query with this connection,
     *  the default uses an Identity function has mapper (returns result set)
     */
    default Query<ResultSet> query(String query){
        return new Query<>(this::getConnection, query, (rs) -> rs);
    }

    /* Default update with this connection */
    default Update update(String query){
        return new Update(this::getConnection, query);
    }

    /* Default batch with this connection */
    default BatchBuilder batch(String query){
        return new BatchBuilder(this::getConnection, query);
    }

    /* Simple batch with this connection */
    default SimpleBatch batch(String[] batchQuery){
        return new SimpleBatch(this::getConnection, batchQuery);
    }

    /**
     * Transaction using this connection
     * @param transaction Function with the transaction,
     *      use the connection supplied as argument to make the transaction
     * @param <E> Return type
     * @return The same value returned in the transaction function
     * @throws SQLException
     */
    default <E>E transaction(TransactionFunction<E> transaction) throws SQLException {
        try (TransactionalConnection trans = new TransactionalConnection(this.getConnection())) {
            Connection con = trans.get();
            try {
                con.setAutoCommit(false);
                E result = transaction.apply(() -> trans);
                con.commit();
                return result;
            } catch (SQLException sql) {
                con.rollback();
                throw sql;
            } finally {
                con.setAutoCommit(true);
                trans.closeTransaction();
            }
        }
    }
}
