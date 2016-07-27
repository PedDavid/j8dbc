package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;
import pt.peddavid.j8dbc.function.SQLConsumer;

import java.sql.PreparedStatement;

/** Super class for every type of statement (query/update)
 * @param <Type> Children Type so children don't need to override some methods
 */
public class Statement<Type> {

    protected final ConnectionService service;
    protected final String query;
    protected SQLConsumer<PreparedStatement> psSetter;

    public Statement(ConnectionService service, String query){
        this(service, query, (prepStmt) -> { });
    }

    public Statement(ConnectionService service, String query, SQLConsumer<PreparedStatement> psSetter){
        this.service = service;
        this.query = query;
        this.psSetter = psSetter;
    }

    @SuppressWarnings("unchecked")
    public Type setInt(int idx, int x){
        this.psSetter = this.psSetter.andThen((psmt -> psmt.setInt(idx, x)));
        return (Type)this;
    }

    @SuppressWarnings("unchecked")
    public Type setLong(int idx, long x){
        this.psSetter = this.psSetter.andThen((psmt -> psmt.setLong(idx, x)));
        return (Type)this;
    }

    @SuppressWarnings("unchecked")
    public Type setString(int idx, String x){
        this.psSetter = this.psSetter.andThen((psmt -> psmt.setString(idx, x)));
        return (Type)this;
    }

    @SuppressWarnings("unchecked")
    public Type setObject(int idx, Object x){
        this.psSetter = this.psSetter.andThen((psmt -> psmt.setObject(idx, x)));
        return (Type)this;
    }
}
