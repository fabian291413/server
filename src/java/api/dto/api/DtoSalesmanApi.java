package api.dto.api;

import api.BaseAPI;
import api.dto.dto.MoveSmanDto;
import api.dto.model.DtoCenter;
import api.dto.model.DtoSalesman;
import api.dto.model.DtoSalesmanLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

@Path("/dtoSalesman")
public class DtoSalesmanApi extends BaseAPI {

    @POST
    public Response insert(DtoSalesman obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(DtoSalesman obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            DtoSalesman obj = new DtoSalesman().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            DtoSalesman.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(DtoSalesman.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/keysMinistry")
    public Response getKeysMinistry(@QueryParam("centerId") Integer centerId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            String str = "SELECT s.document, CONCAT(s.first_name, ' ', s.last_name), s.id_regist, s.minas_pass, c.name "
                    + "FROM dto_salesman s "
                    + "LEFT JOIN dto_center c ON c.id = s.center_id "
                    + "LEFT JOIN employee e ON s.driver_id = e.id AND e.active "
                    + "WHERE s.active "
                    + (centerId != null ? "AND s.center_id = ?1 " : "")
                    + "ORDER BY c.name ASC";

            MySQLQuery q = new MySQLQuery(str);
            if (centerId != null) {
                q.setParam(1, centerId);
            }
            Object[][] data = q.getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Listado claves ministerio", "", "listado", MySQLQuery.now(conn));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Listado claves ministerio");
            tb.getColumns().add(new Column("Cedula Vendedor", 35, 0));
            tb.getColumns().add(new Column("Nombre Vendedor", 35, 0));
            tb.getColumns().add(new Column("Código Ministerio", 35, 0));
            tb.getColumns().add(new Column("Contraseña Ministerio", 35, 0));
            tb.getColumns().add(new Column("Centro Operativo", 35, 0));

            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(conn), "keysMinistry.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/moveSman")
    public Response moveSman(MoveSmanDto obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            try {
                conn.setAutoCommit(false);
                DtoCenter cOrig = new DtoCenter().select(obj.sman.centerId, conn);
                DtoCenter cDest = new DtoCenter().select(obj.destCenterId, conn);

                Date begMonthOrig = Dates.getDatesBegEnd(cOrig.dDay)[0];
                Date begMonthDest = null;

                if (cDest.dDay != null) {
                    begMonthDest = Dates.getDatesBegEnd(cDest.dDay)[0];
                }

                //suma fras del ministerio del antes del mes del cambio
                String changeMonthQ = "SELECT SUM(subsidy) "
                        + "FROM dto_sale "
                        + "WHERE "
                        + "date(dt) BETWEEN ?1 AND ?2 "
                        + "AND dto_liq_id IS NULL "
                        + "AND center_id = " + cOrig.id + " "
                        + "AND salesman_id = " + obj.sman.id;
                //Ids de facturas que se van a mover, requisito para el log de movimientos

                BigDecimal origChange = new MySQLQuery(changeMonthQ).setParam(1, begMonthOrig).setParam(2, cOrig.dDay).getAsBigDecimal(conn, true);
                MySQLQuery query = new MySQLQuery(changeMonthQ);
                if (cDest.dDay != null) {
                    query.setParam(1, begMonthDest).setParam(2, cDest.dDay);
                } else {
                    query.setParam(1, begMonthOrig).setParam(2, cOrig.dDay);
                }
                BigDecimal destChange = query.getAsBigDecimal(conn, true);
                Integer destSalesmanId = new MySQLQuery("SELECT id FROM dto_salesman "
                        + "WHERE document = \"" + obj.sman.document + "\" "
                        + "AND center_id = " + cDest.id + " AND active = 0").getAsInteger(conn);

                Object[][] movDetailData = new MySQLQuery("SELECT CONCAT(DATE_FORMAT(dt, '%y%m%d%H%i%s'), ' ', clie_doc) "
                        + "FROM dto_sale "
                        + "WHERE "
                        + "dto_liq_id IS NULL "
                        + "AND center_id = " + cOrig.id + " "
                        + "AND salesman_id = " + obj.sman.id).getRecords(conn);

                boolean newSalesman = false;

                //---------------- Log de movimiento de facturas --------------------------------------
                Object[][] saleIds = new MySQLQuery("SELECT id "
                        + "FROM dto_sale "
                        + "WHERE "
                        + "date(dt) < ?1 "
                        + "AND dto_liq_id IS NULL "
                        + "AND center_id = " + cOrig.id + " "
                        + "AND salesman_id = " + obj.sman.id).setParam(1, new Date()).getRecords(conn);
                if (saleIds.length > 0) {
                    //MySQLBatch qq = new MySQLBatch();
                    for (int i = 0; i < saleIds.length; i++) {
                        new MySQLQuery("INSERT INTO dto_sales_move SET "
                                + "sale_id = " + MySQLQuery.getAsInteger(saleIds[i][0]) + ", "
                                + "sman_id = " + obj.sman.id + ", "
                                + "mov_date = NOW()").executeInsert(conn);
                    }

                    //qq.sendData(ep());
                }
                //-------------------------------------------------------------------------------------

                StringBuilder sbMovDetailData = new StringBuilder("\\n\\rSe movieron fras por " + obj.movVal + ", fueron: (ymdHis)");
                for (Object[] movDetailRow : movDetailData) {
                    sbMovDetailData.append("\\n\\r");
                    sbMovDetailData.append(movDetailRow[0]);
                }
                String movDetail = sbMovDetailData.toString();

                if (destSalesmanId == null) {
                    newSalesman = true;
                    destSalesmanId = new MySQLQuery("INSERT INTO dto_salesman SET id_regist = \"" + obj.sman.idRegist + "\", first_name = \"" + obj.sman.firstName + "\", last_name = \"" + obj.sman.lastName + "\", document = \"" + obj.sman.document + "\" "
                            + ", center_id = " + cDest.id + ", "
                            + "active = 1, "
                            + "store_id = " + obj.sman.storeId + ", "
                            + "driver_id = " + obj.sman.driverId + ", "
                            + "contractor_id = " + obj.sman.contractorId + ", "
                            + "distributor_id = " + obj.sman.distributorId + ", "
                            + "minas_pass = '" + (obj.sman.minasPass != null ? obj.sman.minasPass : "") + "' ").executeInsert(conn);
                }

                new MySQLQuery("UPDATE dto_salesman SET active = 0 WHERE id = " + obj.sman.id).executeUpdate(conn);
                new MySQLQuery("UPDATE dto_salesman SET "
                        + "store_id = " + obj.sman.storeId + ", "
                        + "driver_id = " + obj.sman.driverId + ", "
                        + "contractor_id = " + obj.sman.contractorId + ", "
                        + "distributor_id = " + obj.sman.distributorId + ", "
                        + "active = 1 WHERE id = " + destSalesmanId).executeUpdate(conn);
                new MySQLQuery("UPDATE dto_center SET initial_balance_month = initial_balance_month - " + origChange + " WHERE id = " + cOrig.id).executeUpdate(conn);
                if (cDest.dDay != null) {
                    new MySQLQuery("UPDATE dto_center SET initial_balance_month = initial_balance_month + " + destChange + " WHERE id = " + cDest.id).executeUpdate(conn);
                }
                new MySQLQuery("UPDATE dto_sale "
                        + "SET salesman_id = " + destSalesmanId + ", center_id = " + cDest.id + " "
                        + "WHERE "
                        + "center_id = " + cOrig.id + " "
                        + "AND salesman_id = " + obj.sman.id + " "
                        + "AND dto_liq_id IS NULL").executeUpdate(conn);

                DtoSalesmanLog logSalesmanOrig = new DtoSalesmanLog();
                logSalesmanOrig.salesmanId = obj.sman.id;
                logSalesmanOrig.type = "edit";
                logSalesmanOrig.notes = "Se desactivó por movimiento de " + cOrig.name + " a " + cDest.name + "." + System.lineSeparator() + movDetail;

                logSalesmanOrig.insert(logSalesmanOrig, sl.employeeId, conn);

                //log del empleado activado o creado
                DtoSalesmanLog logSalesmanDest = new DtoSalesmanLog();
                logSalesmanDest.salesmanId = destSalesmanId;
                logSalesmanDest.type = (newSalesman ? "new" : "edit");
                logSalesmanDest.notes = (newSalesman ? "Se creó" : "Se reactivó") + " por movimiento de " + cOrig.name + " a " + cDest.name + System.lineSeparator() + movDetail;
                DtoSalesmanLog insert = new DtoSalesmanLog();
                insert.insert(logSalesmanDest, sl.employeeId, conn);

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw new Exception(ex);
            }

            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }
}
