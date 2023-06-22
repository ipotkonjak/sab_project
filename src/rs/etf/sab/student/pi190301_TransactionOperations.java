package rs.etf.sab.student;

import rs.etf.sab.operations.TransactionOperations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class pi190301_TransactionOperations implements TransactionOperations {

    private Connection connection=DB.getInstance().getConnection();
    @Override
    public BigDecimal getBuyerTransactionsAmmount(int idB) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select coalesce (sum(Amount),0) as Amount from [Transaction]  where IdCliFrom = ?" )
        ){
            ps.setInt(1, idB);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Amount");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int idS) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select coalesce (sum(Amount),0) as Amount from [Transaction]  where IdCliTo = ?" )
        ){
            ps.setInt(1, idS);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Amount");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public List<Integer> getTransationsForBuyer(int idB) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdT from [Transaction]  where IdCliFrom = ?" )
        ){
            ps.setInt(1, idB);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> transactions = new ArrayList<>();
            while(rs.next()) {
                transactions.add(rs.getInt("IdT"));
            }
            return transactions;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getTransactionForBuyersOrder(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select t.IdT from [Transaction] t inner join [Order] o on (t.IdO = o.IdO)  where o.IdO = ? and o.IdB = t.IdCliFrom" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("IdT");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int idO, int idS) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdT from [Transaction] where IdO = ? and IdCliTo = ?" )
        ){
            ps.setInt(1, idO);
            ps.setInt(2, idS);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("IdT");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int idS) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdT from [Transaction]  where IdCliTo = ?" )
        ){
            ps.setInt(1, idS);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> transactions = new ArrayList<>();
            while(rs.next()) {
                transactions.add(rs.getInt("IdT"));
            }
            return (transactions.size() == 0 ? null : transactions);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public Calendar getTimeOfExecution(int idT) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select ExecutionTime from [Transaction]  where IdT = ?" )
        ){
            ps.setInt(1, idT);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> transactions = new ArrayList<>();
            if(rs.next()) {
                Calendar execTime = Calendar.getInstance();
                execTime.setTime(rs.getDate("ExecutionTime"));
                return execTime;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select t.Amount from [Transaction] t inner join [Order] o on (t.IdO = o.IdO)  where t.IdCliFrom = o.IdB and o.IdO = ?" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Amount");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int idS, int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select Amount from [Transaction]  where IdCliTo = ? and IdO = ?" )
        ){
            ps.setInt(1, idS);
            ps.setInt(2, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Amount");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public BigDecimal getTransactionAmount(int idT) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select Amount from [Transaction]  where IdT = ?" )
        ){
            ps.setInt(1, idT);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getBigDecimal("Amount");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public BigDecimal getSystemProfit() {
        try(PreparedStatement ps = connection.prepareStatement(
                "select c.Credit from Client c inner join [System] s on (c.IdCli = s.IdSys)" )
        ){
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                BigDecimal credit = rs.getBigDecimal("Credit").setScale(3);
                try(PreparedStatement ps1 = connection.prepareStatement(
                        "select coalesce(sum(t.Amount),0) as Amount from [Transaction] t inner join [Order] o on (t.IdO = o.IdO) where o.Status = 'sent'" )
                ){
                    ResultSet rs1 = ps1.executeQuery();
                    if(rs1.next()) {
                        BigDecimal notProcessed = rs1.getBigDecimal("Amount").setScale(3);
                        return credit.subtract(notProcessed);
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }
}
