package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillInstCheckType extends BaseModel<BillInstCheckType> {
//inicio zona de reemplazo

    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_inst_check_type";
    }

    public static String getSelFlds(String alias) {
        return new BillInstCheckType().getSelFldsForAlias(alias);
    }

    public static List<BillInstCheckType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillInstCheckType().getListFromQuery(q, conn);
    }

    public static List<BillInstCheckType> getList(Params p, Connection conn) throws Exception {
        return new BillInstCheckType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillInstCheckType().deleteById(id, conn);
    }

    public static List<BillInstCheckType> getAll(Connection conn) throws Exception {
        return new BillInstCheckType().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillInstCheckType> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}