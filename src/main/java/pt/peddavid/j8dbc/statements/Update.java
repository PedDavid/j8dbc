package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;
import pt.peddavid.j8dbc.connections.BaseConnection;
import pt.peddavid.j8dbc.function.SQLConsumer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Update extends Statement<Update> {

    public Update(ConnectionService service, String query){
        this(service, query, (prepStmt) -> { });
    }

    protected Update(ConnectionService service, String query, SQLConsumer<PreparedStatement> psBuilder){
        super(service, query, psBuilder);
    }

    public int execute() throws SQLException {
        try (BaseConnection con = service.getConnection();
             PreparedStatement prepStmt = con.get().prepareStatement(query)
        ) {
            psSetter.set(prepStmt);
            return prepStmt.executeUpdate();
        }
    }

    public long fetchGeneratedKeys() throws SQLException {
        try (BaseConnection con = service.getConnection();
             PreparedStatement pstm = con.get().prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
        ) {
            psSetter.set(pstm);
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if(rs != null && rs.next()){
                return rs.getLong(1);
            }
            return -1;
        }
    }

}
