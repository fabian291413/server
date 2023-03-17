package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillCfg;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/billCfg")
public class BillCfgApi extends BaseAPI {

    @PUT
    public Response update(BillCfg obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillCfg old = new BillCfg().select(obj.id, conn);
            obj.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillCfg obj = new BillCfg().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    /*@GET
    @Path("/grid")
    public Response getGrid() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
            };
            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_ASC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
}
