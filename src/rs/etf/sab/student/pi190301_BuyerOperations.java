package rs.etf.sab.student;

import rs.etf.sab.operations.BuyerOperations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class pi190301_BuyerOperations implements BuyerOperations {

    private Connection connection=DB.getInstance().getConnection();

    private boolean checkCity(int city) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select * from City where IdC = ?" )
        ){
            ps.setInt(1, city);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  true;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkBuyer(int buyer) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select * from Buyer where IdB = ?" )
        ){
            ps.setInt(1, buyer);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  true;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int createBuyer(String name, int idC) {
        if(checkCity(idC)) {
            try(PreparedStatement ps1 = connection.prepareStatement(
                    " INSERT INTO Client (Credit)  \n" +
                            "VALUES (0);  \n" +
                            "SELECT @@IDENTITY AS 'Identity';" )
            ){

                ResultSet rs1 = ps1.executeQuery();
                if(rs1.next()) {
                    int idB =  rs1.getInt("Identity");
                    try(PreparedStatement ps2 = connection.prepareStatement(
                            " INSERT INTO Buyer(IdB, Name, IdC)  \n" +
                                    "VALUES (?,?,?);" )
                    ) {
                        ps2.setInt(1, idB);
                        ps2.setString(2, name);
                        ps2.setInt(3, idC);
                        ps2.execute();
                        return idB;
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Override
    public int setCity(int idB, int idC) {
        if(checkCity(idC)) {
            try(PreparedStatement ps = connection.prepareStatement(
                    "update Buyer set IdC = ? where IdB = ?" )
            ){
                ps.setInt(1, idC);
                ps.setInt(2, idB);
                ps.execute();
                return 1;
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Override
    public int getCity(int idB) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdC from Buyer where IdB = ?" )
        ){
            ps.setInt(1, idB);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("IdC");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public BigDecimal increaseCredit(int idB, BigDecimal bigDecimal) {
        try(PreparedStatement ps = connection.prepareStatement(
                "update Client set Credit = Credit + ? where IdCli = ? and IdCli in (select IdB from Buyer);"
                    + "select c.Credit from Client c inner join Buyer b on (c.IdCli = b.IdB) where c.IdCli = ?")
        ){
            ps.setBigDecimal(1, bigDecimal);
            ps.setInt(2, idB);
            ps.setInt(3, idB);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Credit");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int createOrder(int idB) {
        if(checkBuyer(idB)) {
            try(PreparedStatement ps = connection.prepareStatement(
                    "insert into [Order](IdB, Status) values (?,?)" +
                            "SELECT @@IDENTITY AS 'Identity';" )
            ){
                ps.setInt(1, idB);
                ps.setString(2, "created");
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    return  rs.getInt("Identity");
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Override
    public List<Integer> getOrders(int idB) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdO from [Order] where IdB = ?" )
        ){
            ps.setInt(1, idB);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> orders = new ArrayList<>();
            while(rs.next()) {
                orders.add(rs.getInt("IdO"));
            }
            return orders;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BigDecimal getCredit(int idB) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select c.Credit from Client c inner join Buyer b on (c.IdCli = b.IdB) where c.IdCli = ?")
        ){
            ps.setInt(1, idB);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Credit");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
