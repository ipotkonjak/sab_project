package rs.etf.sab.student;

import rs.etf.sab.operations.ArticleOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class pi190301_ArticleOperations implements ArticleOperations {

    private Connection connection=DB.getInstance().getConnection();

    private boolean checkStore(int store) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select * from Shop where IdS = ?" )
        ){
            ps.setInt(1, store);
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
    public int createArticle(int idS, String name, int price) {
        if(checkStore(idS)) {
            try(PreparedStatement ps = connection.prepareStatement(
                    "insert into Article(Name, Count, Price, IdS) values (?,0,?,?)" +
                            "SELECT @@IDENTITY AS 'Identity';" )
            ){
                ps.setString(1, name);
                ps.setInt(2, price);
                ps.setInt(3, idS);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    return  rs.getInt("Identity");
                }
                else return -1;
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
