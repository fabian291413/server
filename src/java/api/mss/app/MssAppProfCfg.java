package api.mss.app;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssAppProfCfg extends BaseModel<MssAppProfCfg> {

    public String profileName;
//inicio zona de reemplazo

    public int profId;
    public boolean supervisions;
    public boolean shifs;
    public boolean agent;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "supervisions",
            "shifs",
            "agent"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, supervisions);
        q.setParam(3, shifs);
        q.setParam(4, agent);
    }

    @Override
    protected String getTblName() {
        return "mss_app_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new MssAppProfCfg().getSelFldsForAlias(alias);
    }

    public static List<MssAppProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssAppProfCfg().getListFromQuery(q, conn);
    }

    public static List<MssAppProfCfg> getList(Params p, Connection conn) throws Exception {
        return new MssAppProfCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssAppProfCfg().deleteById(id, conn);
    }

    public static List<MssAppProfCfg> getAll(Connection conn) throws Exception {
        return new MssAppProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
    @Override
    public void setRow(Object[] row) throws Exception {
        int indexId = getFlds().length == row.length ? row.length - 1 : row.length - 2;

        profId = MySQLQuery.getAsInteger(row[0]);
        supervisions = MySQLQuery.getAsBoolean(row[1]);
        shifs = MySQLQuery.getAsBoolean(row[2]);
        agent = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[indexId]);

        if (getFlds().length != row.length) {
            profileName = MySQLQuery.getAsString(row[row.length - 1]);
        }
    }
}
