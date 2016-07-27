package pt.peddavid.j8dbc.statements;

import pt.peddavid.j8dbc.ConnectionService;
import pt.peddavid.j8dbc.connections.BaseConnection;
import pt.peddavid.j8dbc.function.RowMapper;
import pt.peddavid.j8dbc.function.SQLConsumer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collector;

public class Query<R> extends Statement<Query<R>> {

    protected final RowMapper<ResultSet, R> function;

    public Query(ConnectionService service, String query, RowMapper<ResultSet, R> resultSetTFunction){
        super(service, query);
        this.function = resultSetTFunction;
    }

    protected Query(
            ConnectionService service,
            String query,
            SQLConsumer<PreparedStatement> psSetter,
            RowMapper<ResultSet, R> resultSetTFunction
    ){
        super(service, query, psSetter);
        this.function = resultSetTFunction;
    }

    public Query<R> setMaxRows(int maxRows) {
        this.psSetter = this.psSetter.andThen((prepStmt) -> prepStmt.setMaxRows(maxRows));
        return this;
    }

    public <R1>Query<R1> map(RowMapper<R, R1> function) {
        return new Query<>(service, query, psSetter, this.function.andThen(function));
    }

    public SortedQuery<R> sort(String sortParameters) {
        return new SortedQuery<>(service, query, sortParameters, psSetter, function);
    }

    public SkippedQuery<R> skip(int skipValue) {
        return new SortedQuery<>(service, query, psSetter, function).skip(skipValue);
    }

    public ToppedQuery<R> top(int topValue) {
        return new SortedQuery<>(service, query, psSetter, function).top(topValue);
    }

    public void execute() throws SQLException {
        try(BaseConnection con = service.getConnection()){
            try(PreparedStatement psmt = con.get().prepareStatement(query)) {
                psSetter.set(psmt);
                psmt.executeQuery();
            }
        }
    }

    public <R1, A>R1 collect(Collector<? super R, A, R1> collector) throws SQLException {
        A container = collector.supplier().get();
        try(BaseConnection con = service.getConnection()){
            try(PreparedStatement psmt = con.get().prepareStatement(query)) {
                psSetter.set(psmt);
                ResultSet rs = psmt.executeQuery();
                while(rs.next()){
                    collector.accumulator().accept(container, function.map(rs));
                }
                return collector.finisher().apply(container);
            }
        }
    }

    public Optional<R> fetchSingleResult() throws SQLException {
        try(BaseConnection con = service.getConnection()){
            try(PreparedStatement prepStmt = con.get().prepareStatement(query)){
                psSetter.set(prepStmt);
                ResultSet rs = prepStmt.executeQuery();
                if(rs.next()) return Optional.of(function.map(rs));
                return Optional.empty();
            }
        }
    }

    public void forEach(SQLConsumer<R> consumer) throws SQLException {
        try(BaseConnection con = service.getConnection()){
            try(PreparedStatement psmt = con.get().prepareStatement(query)) {
                psSetter.set(psmt);
                ResultSet rs = psmt.executeQuery();
                while(rs.next()){
                    consumer.set(function.map(rs));
                }
            }
        }
    }

    public static class SortedQuery<R> extends Query<R>{

        private final static String formatter = "select * from (select *, row_number() over(order by %s) as rowNum " +
                "from ( %s ) as src)as numberedSrc";

        public SortedQuery(
                ConnectionService service,
                String query,
                String sortParameters,
                SQLConsumer<PreparedStatement> psSetter,
                RowMapper<ResultSet, R> resultSetTFunction
        ) {
            super(service, String.format(formatter, sortParameters, query), psSetter, resultSetTFunction);
        }

        public SortedQuery(
                ConnectionService service,
                String query,
                SQLConsumer<PreparedStatement> psSetter,
                RowMapper<ResultSet, R> resultSetTFunction
        ) {
            this(service, query, "(select 1)", psSetter, resultSetTFunction);
        }

        public SkippedQuery<R> skip(int skipValue){
            return new SkippedQuery<>(service, query, skipValue, psSetter, function);
        }

        public ToppedQuery<R> top(int topValue){
            return skip(0).top(topValue);
        }
    }

    public static class SkippedQuery<R> extends Query<R>{
        private final static String formatter = "%s where rowNum > %s";

        private final int skipValue;

        protected SkippedQuery(
                ConnectionService service,
                String query,
                int skipValue,
                SQLConsumer<PreparedStatement> psSetter,
                RowMapper<ResultSet, R> resultSetTFunction
        ) {
            super(service, String.format(formatter, query, Integer.toUnsignedString(skipValue)), psSetter, resultSetTFunction);
            this.skipValue = skipValue;
        }

        public ToppedQuery<R> top(int topValue){
            return new ToppedQuery<>(service, query, skipValue + topValue, psSetter, function);
        }
    }

    public static class ToppedQuery<R> extends Query<R>{
        private final static String formatter = "%s and rowNum <= %s";

        protected ToppedQuery(
                ConnectionService service,
                String query,
                int topValue,
                SQLConsumer<PreparedStatement> psSetter,
                RowMapper<ResultSet, R> resultSetTFunction
        ) {
            super(service, String.format(formatter, query, Integer.toUnsignedString(topValue)), psSetter, resultSetTFunction);
        }
    }
}
