package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;
import pt.peddavid.j8dbc.connections.BaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BaseBatch extends Statement<BaseBatch> {

    public BaseBatch(ConnectionService service, String query) {
        super(service, query);
    }

    /**
     * Called after setting each member to add to the batch
     * @return this for method chaining
     */
    public BaseBatch addBatch(){
        this.psSetter = psSetter.andThen(PreparedStatement::addBatch);
        return this;
    }

    /** Executes the batch
     * @return an Array in which each index corresponds to
     * the number of altered rows in the statement(at that index) of the batch
     * @throws SQLException
     */
    public int[] execute() throws SQLException {
        try (BaseConnection con = service.getConnection();
             PreparedStatement prepStmt = con.get().prepareStatement(query)
        ) {
            psSetter.set(prepStmt);
            return prepStmt.executeBatch();
        }
    }
}
