package pt.peddavid.j8dbc.connections;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <p>Connection wrapper for Transactions</p>
 * <b>Note:</b> If you're using this class you
 * shouldn't be asking for the connection so you can close it
 */
public class TransactionalConnection extends BaseConnection {

    public TransactionalConnection(BaseConnection baseCon){
        this(baseCon.get());
    }

    public TransactionalConnection(Connection con) {
        super(con);
    }

    @Override
    public void close(){
        /* This does nothing so Query/Update/Batch doesn't close the connection */
    }

    /**
     * Truly closes the connection,
     * only the transaction handler should know about this method (by passing the BaseConnection only)
     * @throws SQLException
     */
    public void closeTransaction() throws SQLException {
        connection.close();
    }
}
