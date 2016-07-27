package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;
import pt.peddavid.j8dbc.connections.BaseConnection;

import java.sql.SQLException;

public class SimpleBatch {

    private final ConnectionService dbService;
    private final String[] src;

    public SimpleBatch(ConnectionService service, String[] batchQuery){
        this.dbService = service;
        this.src = batchQuery;
    }

    /** Executes the batch
     * @return an Array in which each index corresponds to
     * the number of altered rows in the statement(at that index) of the batch
     * @throws SQLException
     */
    public int[] execute() throws SQLException {
        try (BaseConnection con = dbService.getConnection();
             java.sql.Statement pstm = con.get().createStatement()
        ) {
            for(String statement : src){
                pstm.addBatch(statement);
            }
            return pstm.executeBatch();
        }
    }
}
