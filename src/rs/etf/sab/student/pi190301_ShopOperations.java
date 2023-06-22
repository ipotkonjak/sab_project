package rs.etf.sab.student;

import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.ShopOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class pi190301_ShopOperations implements ShopOperations {

    private Connection connection=DB.getInstance().getConnection();

    private int checkCity(String city) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdC from City where Name = ?" )
        ){
            ps.setString(1, city);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  rs.getInt("IdC");
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    @Override
    public int createShop(String s, String s1) {
        try(PreparedStatement ps = connection.prepareStatement(
                " select IdC from City where Name = ?" )
        ){
            ps.setString(1, s1);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                int idC = rs.getInt("IdC");
                try(PreparedStatement ps1 = connection.prepareStatement(
                        " INSERT INTO Client (Credit)  \n" +
                                "VALUES (0);  \n" +
                                "SELECT @@IDENTITY AS 'Identity';" )
                ){

                    ResultSet rs1 = ps1.executeQuery();
                    if(rs1.next()) {
                        int idS =  rs1.getInt("Identity");
                        try(PreparedStatement ps2 = connection.prepareStatement(
                                " INSERT INTO Shop(IdS, Name, IdC, Discount)  \n" +
                                        "VALUES (?,?,?,0);" )
                        ) {
                            ps2.setInt(1, idS);
                            ps2.setString(2, s);
                            ps2.setInt(3, idC);
                            ps2.execute();
                            return idS;
                        }
                    }
                }
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int setCity(int idS, String city) {
        int idC = checkCity(city);
        if(idC != -1) {
            try(PreparedStatement ps = connection.prepareStatement(
                    "update Shop set IdC = ? where IdS = ?" )
            ){
                ps.setInt(1, idC);
                ps.setInt(2, idS);
                ps.execute();
                return 1;
            }catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }
        else return -1;
    }

    @Override
    public int getCity(int idS) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdC from Shop where IdS = ?" )
        ){
            ps.setInt(1, idS);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  rs.getInt("IdC");
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int setDiscount(int idS, int discount) {
        if(discount >= 0 && discount <= 100) {
            try(PreparedStatement ps = connection.prepareStatement(
                    "update Shop set Discount = ? where IdS = ?" )
            ){
                ps.setInt(1, discount);
                ps.setInt(2, idS);
                ps.execute();
                return 1;
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Override
    public int increaseArticleCount(int idA, int incr) {
        try(PreparedStatement ps = connection.prepareStatement(
                "update Article set [Count] = [Count] + ? where IdA = ?;"
                    + "select a.Count from Article a where IdA = ?")
        ){
            ps.setInt(1, incr);
            ps.setInt(2, idA);
            ps.setInt(3, idA);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  rs.getInt("Count");
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getArticleCount(int idA) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select a.Count from Article a where IdA = ?" )
        ){
            ps.setInt(1, idA);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  rs.getInt("Count");
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getArticles(int idS) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdA from Article where IdS = ?" )
        ){
            ps.setInt(1, idS);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> articles = new ArrayList<>();
            while(rs.next()) {
                articles.add(rs.getInt("IdA"));
            }
            return articles;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getDiscount(int idS) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select Discount from Shop where IdS = ?" )
        ){
            ps.setInt(1, idS);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  rs.getInt("Discount");
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
