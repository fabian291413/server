package api.ord.api;

import api.BaseAPI;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.ord.model.OrdOffice;

@Path("/ordOffice")
public class OrdOfficeApi extends BaseAPI {

    @POST
    public Response insert(OrdOffice obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdOffice obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdOffice old = new OrdOffice().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdOffice obj = new OrdOffice().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdOffice.delete(id, conn);
            SysCrudLog.deleted(this, OrdOffice.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(OrdOffice.getAll(conn));
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
