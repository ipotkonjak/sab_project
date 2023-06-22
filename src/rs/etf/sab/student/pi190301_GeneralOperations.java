package rs.etf.sab.student;

import rs.etf.sab.operations.GeneralOperations;

import java.sql.*;
import java.util.Calendar;

public class pi190301_GeneralOperations implements GeneralOperations {

    private Connection connection=DB.getInstance().getConnection();
    private static Calendar simTime = Calendar.getInstance();

    private void addSystem() {
        try(PreparedStatement ps1 = connection.prepareStatement(
                " INSERT INTO Client (Credit)  \n" +
                        "VALUES (0);  \n" +
                        "SELECT @@IDENTITY AS 'Identity';" )
        ){

            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next()) {
                int idSys =  rs1.getInt("Identity");
                try(PreparedStatement ps2 = connection.prepareStatement(
                        " INSERT INTO System  \n" +
                                "VALUES (?);" )
                ) {
                    ps2.setInt(1, idSys);
                    ps2.execute();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void setInitialTime(Calendar calendar) {
        simTime.setTime(calendar.getTime()); ;
    }

    @Override
    public Calendar time(int i) {
        simTime.add(Calendar.DATE, i);
        try(PreparedStatement ps = connection.prepareStatement(
                "update [Order] set Status = 'arrived' where Status = 'sent' and Received <= ?" )
        ){
            ps.setDate(1, new Date(simTime.getTimeInMillis()));
            ps.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return simTime;
    }

    @Override
    public Calendar getCurrentTime() {
        return simTime;
    }

    @Override
    public void eraseAll() {

        try(PreparedStatement ps = connection.prepareStatement(
                        "EXEC sp_MSForEachTable 'DISABLE TRIGGER ALL ON ?'\n" +
                        "EXEC sp_MSForEachTable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'\n" +
                        "EXEC sp_MSForEachTable 'DELETE FROM ?'\n" +
                        "EXEC sp_MSForEachTable 'ALTER TABLE ? CHECK CONSTRAINT ALL'\n" +
                        "EXEC sp_MSForEachTable 'ENABLE TRIGGER ALL ON ?'\n"  +
                        "DBCC CHECKIDENT (City, RESEED, 0)" +
                        "DBCC CHECKIDENT (Connection, RESEED, 0)" +
                        "DBCC CHECKIDENT ([Transaction], RESEED, 0)" +
                        "DBCC CHECKIDENT ([Order], RESEED, 0)" +
                        "DBCC CHECKIDENT (Article, RESEED, 0)" +
                        "DBCC CHECKIDENT (Client, RESEED, 0)" +
                        "DBCC CHECKIDENT (Item, RESEED, 0)")
        ){
            ps.execute();
            addSystem();
        }catch (SQLException s) {
            s.printStackTrace();
        }
    }
}
