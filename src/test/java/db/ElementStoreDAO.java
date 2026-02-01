//package db;
//
//import model.ElementMeta;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//public class ElementStoreDAO {
//
//    // Save only if not already present
//    public static void saveIfNotExists(ElementMeta meta) {
//
//        String checkSql =
//                "SELECT id FROM element_store WHERE page_url=? AND logical_name=?";
//
//        String insertSql =
//                "INSERT INTO element_store " +
//                        "(page_url, logical_name, locator, tag, text, placeholder, x_percent, y_percent) " +
//                        "VALUES (?,?,?,?,?,?,?,?)";
//
//        try (Connection con = DBConnection.getConnection();
//             PreparedStatement checkPs = con.prepareStatement(checkSql)) {
//
//            checkPs.setString(1, meta.getPageUrl());
//            checkPs.setString(2, meta.getLogicalName());
//
//            ResultSet rs = checkPs.executeQuery();
//
//            if (!rs.next()) {
//
//                PreparedStatement ins = con.prepareStatement(insertSql);
//                ins.setString(1, meta.getPageUrl());
//                ins.setString(2, meta.getLogicalName());
//                ins.setString(3, meta.getLocator());
//                ins.setString(4, meta.getTag());
//                ins.setString(5, meta.getText());
//                ins.setString(6, meta.getPlaceholder());
//                ins.setDouble(7, meta.getxPercent());
//                ins.setDouble(8, meta.getyPercent());
//
//                ins.executeUpdate();
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Save failed", e);
//        }
//    }
//
//    // Fetch metadata for healing
//    public static ElementMeta get(String pageUrl, String logicalName) {
//
//        String sql =
//                "SELECT * FROM element_store WHERE page_url=? AND logical_name=?";
//
//        try (Connection con = DBConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setString(1, pageUrl);
//            ps.setString(2, logicalName);
//
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) {
//
//                ElementMeta meta = new ElementMeta();
//                meta.setPageUrl(rs.getString("page_url"));
//                meta.setLogicalName(rs.getString("logical_name"));
//                meta.setLocator(rs.getString("locator"));
//                meta.setTag(rs.getString("tag"));
//                meta.setText(rs.getString("text"));
//                meta.setPlaceholder(rs.getString("placeholder"));
//                meta.setxPercent(rs.getDouble("x_percent"));
//                meta.setyPercent(rs.getDouble("y_percent"));
//
//                return meta;
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Fetch failed", e);
//        }
//
//        return null;
//    }
//
//    // Update locator after healing
//    public static void updateLocator(
//            String pageUrl,
//            String logicalName,
//            String newLocator
//    ) {
//
//        String sql =
//                "UPDATE element_store SET locator=? " +
//                        "WHERE page_url=? AND logical_name=?";
//
//        try (Connection con = DBConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setString(1, newLocator);
//            ps.setString(2, pageUrl);
//            ps.setString(3, logicalName);
//
//            ps.executeUpdate();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Update failed", e);
//        }
//    }
//}
