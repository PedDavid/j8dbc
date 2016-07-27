package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;
import pt.peddavid.j8dbc.connections.BaseConnection;
import pt.peddavid.j8dbc.function.StatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class BatchFromCollection<E> {

    private final ConnectionService service;
    private final String query;
    private StatementSetter<E> batchSetter;

    private final Collection<E> source;

    public BatchFromCollection(ConnectionService service, String query, Collection<E> col) {
        this.service = service;
        this.query = query;
        this.batchSetter = (prepStmt, e) -> { };
        this.source = col;
    }

    public BatchFromCollection<E> setInt(int idx, ToIntFunction<E> function){
        this.batchSetter = this.batchSetter.andThen((psmt, element) ->
                psmt.setInt(idx, function.applyAsInt(element))
        );
        return this;
    }

    public BatchFromCollection<E> setLong(int idx, ToLongFunction<E> function){
        this.batchSetter = this.batchSetter.andThen((psmt, element) ->
                psmt.setLong(idx, function.applyAsLong(element))
        );
        return this;
    }

    public BatchFromCollection<E> setString(int idx, Function<E, String> function){
        this.batchSetter = this.batchSetter.andThen((psmt, element) ->
                psmt.setString(idx, function.apply(element))
        );
        return this;
    }

    public BatchFromCollection<E> setObject(int idx, Function<E, Object> function){
        this.batchSetter = this.batchSetter.andThen((psmt, element) ->
                psmt.setObject(idx, function.apply(element))
        );
        return this;
    }

    /** Executes the batch
     * @return an Array in which each index corresponds to
     * the number of altered rows in the statement(at that index) of the batch
     * @throws SQLException
     */
    public int[] execute() throws SQLException {
        try (BaseConnection con = service.getConnection();
             PreparedStatement pstm = con.get().prepareStatement(query)
        ) {
            for(E element : source){
                batchSetter.set(pstm, element);
                pstm.addBatch();
            }
            return pstm.executeBatch();
        }
    }
}
