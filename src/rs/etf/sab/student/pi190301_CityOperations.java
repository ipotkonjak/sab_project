package rs.etf.sab.student;

import rs.etf.sab.operations.CityOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class pi190301_CityOperations implements CityOperations {

    private Connection connection=DB.getInstance().getConnection();
    @Override
    public int createCity(String name) {
        try(PreparedStatement ps = connection.prepareStatement(
                " INSERT INTO City (Name)  \n" +
                        "VALUES (?);  \n" +
                        "SELECT @@IDENTITY AS 'Identity';" )
        ){
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return  rs.getInt("Identity");
            }
            else return -1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getCities() {
        try(PreparedStatement ps = connection.prepareStatement(
                " select IdC from City " )
        ){
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> cities = new ArrayList<>();
            while(rs.next()) {
                cities.add(rs.getInt("IdC"));
            }
            return cities;

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean check(int c1, int c2) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select * from Connection where (IdC1 = ? and IdC2 = ?) or (IdC1 = ? and IdC2 = ?)" )
        ){
            ps.setInt(1, c1);
            ps.setInt(2, c2);
            ps.setInt(3, c2);
            ps.setInt(4, c1);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return false;
            }
            else return true;

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int connectCities(int idC1, int idC2, int dist) {
        if(check(idC1, idC2)) {
            try(PreparedStatement ps = connection.prepareStatement(
                    "insert into Connection(IdC1, IdC2, Distance) values (?, ?, ?); SELECT @@IDENTITY AS 'Identity';" )
            ){
                ps.setInt(1, idC1);
                ps.setInt(2, idC2);
                ps.setInt(3, dist);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    return rs.getInt("Identity");
                }

            }catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }
        else return -1;

    }

    @Override
    public List<Integer> getConnectedCities(int idC) {
        try(PreparedStatement ps = connection.prepareStatement(
                " select IdC1, IdC2 from Connection where IdC1 = ? or IdC2 = ?" )
        ){
            ps.setInt(1, idC);
            ps.setInt(2, idC);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> cities = new ArrayList<>();
            while(rs.next()) {
                int tmp = rs.getInt("IdC1");
                if(tmp != idC) {
                    cities.add(tmp);
                }
                else {
                    cities.add(rs.getInt("IdC2"));
                }
            }
            return cities;

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Integer> getShops(int idC) {
        try(PreparedStatement ps = connection.prepareStatement(
                " select IdS from Shop where IdC = ?" )
        ){
            ps.setInt(1, idC);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> shops = new ArrayList<>();
            while(rs.next()) {
                shops.add(rs.getInt("IdS"));
            }
            return shops;

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
