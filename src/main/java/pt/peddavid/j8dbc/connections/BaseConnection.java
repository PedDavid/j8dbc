package pt.peddavid.j8dbc.connections;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Basic connection wrapper
 */
public class BaseConnection implements AutoCloseable{

    protected final Connection connection;

    public BaseConnection(Connection con){
        this.connection = con;
    }

    public Connection get(){
        return connection;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
