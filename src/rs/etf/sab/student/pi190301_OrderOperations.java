package rs.etf.sab.student;

import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.OrderOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class pi190301_OrderOperations implements OrderOperations {

    private Connection connection=DB.getInstance().getConnection();
    private int getArticleCount(int idO, int idA) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select i.Count from Item i where IdO = ? and IdA = ?" )
        ){
            ps.setInt(1, idO);
            ps.setInt(2, idA);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("Count");
            }
            else return 0;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int addArticle(int idO, int idA, int count) {
        if(count > 0) {
            if(!getState(idO).equals("created")) return -1;
            int cnt = getArticleCount(idO, idA);
            if(cnt == 0) {
                try(PreparedStatement ps = connection.prepareStatement(
                        "select a.Count from Article a where IdA = ?" )
                ){
                    ps.setInt(1, idA);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        int avail =  rs.getInt("Count");
                        if(avail >= count) {
                            try(PreparedStatement ps1 = connection.prepareStatement(
                                    "insert into Item(IdA, IdO, [Count]) values (?,?,?);"
                                        + "SELECT @@IDENTITY AS 'Identity';")
                            ){
                                ps1.setInt(1, idA);
                                ps1.setInt(2, idO);
                                ps1.setInt(3, count);
                                ResultSet rs1 = ps1.executeQuery();
                                if(rs1.next()) {
                                    return rs1.getInt("Identity");
                                }
                            }
                        }
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if(cnt > 0) {
                try(PreparedStatement ps = connection.prepareStatement(
                        "select a.Count from Article a where IdA = ?" )
                ){
                    ps.setInt(1, idA);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        int avail =  rs.getInt("Count");
                        if(avail >= count + cnt) {
                            try(PreparedStatement ps1 = connection.prepareStatement(
                                    "update Item set [Count] = [Count] + ? where IdA = ? and IdO = ?;"
                                            + "SELECT IdI from Item  where IdA = ? and IdO = ?;")
                            ){
                                ps1.setInt(1, count);
                                ps1.setInt(2, idA);
                                ps1.setInt(3, idO);
                                ps1.setInt(4, idA);
                                ps1.setInt(5, idO);

                                ResultSet rs1 = ps1.executeQuery();
                                if(rs1.next()) {
                                    return rs1.getInt("IdI");
                                }
                            }
                        }
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    @Override
    public int removeArticle(int idO, int idA) {
        if(!getState(idO).equals("created")) return -1;
        try(PreparedStatement ps = connection.prepareStatement(
                "delete from Item where IdO = ? and IdA = ?" );
            PreparedStatement ps2 = connection.prepareStatement(
                    "select * from Item where IdO = ? and IdA = ?"
            )
        ){
            ps2.setInt(1, idO);
            ps2.setInt(2, idA);
            ResultSet rs2 = ps2.executeQuery();
            if(!rs2.next()) {
                return -1;
            }
            ps.setInt(1, idO);
            ps.setInt(2, idA);
            ps.execute();
            return 1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getItems(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdI from Item where IdO = ?" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> items = new ArrayList<>();
            while(rs.next()) {
                items.add(rs.getInt("IdI"));
            }
            return items;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public ArrayList<Integer> dijkstraConnections(int idB, int idO) {
        CityOperations co = new pi190301_CityOperations();
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdC from Buyer where IdB = ?" );
            PreparedStatement ps1 = connection.prepareStatement(
                    "select * from [Connection]"
            );
            PreparedStatement ps2 = connection.prepareStatement(
                    "select distinct (s.IdC) from Item i inner join Article a on (i.IdA = a.IdA) inner join Shop s on (a.IdS = s.IdS) where i.IdO = ?"
            )
        ){
            ps.setInt(1, idB);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                int myCity = rs.getInt("IdC");
                HashMap<Integer, HashMap<Integer, Integer>> dist = new HashMap<>();
                HashMap<Integer, ArrayList<Integer>> endDist = new HashMap<>();
                HashMap<Integer, Integer> endDistMax = new HashMap<>();
                ResultSet rs1 = ps1.executeQuery();
                while(rs1.next()) {
                    Integer c1 = rs1.getInt("IdC1");
                    Integer c2 = rs1.getInt("IdC2");
                    Integer d = rs1.getInt("Distance");

                    HashMap<Integer, Integer> pom = null;
                    if((pom = dist.get(c1)) == null) pom = new HashMap<>();
                    pom.put(c2, d);
                    dist.put(c1, pom);
                    if((pom = dist.get(c2)) == null) pom = new HashMap<>();
                    pom.put(c1, d);
                    dist.put(c2, pom);
                }
                List<Integer> toDO = co.getCities();
                List<Integer> toDOLater = new ArrayList<>(toDO);
                ArrayList<Integer> queue = new ArrayList<>();
                int minNode = -1;
                int minDist = -1;
                queue.add(myCity);
                while(!queue.isEmpty()) {
                    Integer curr = queue.remove(0);
                    boolean b = toDO.remove(curr);
                    int myDist = (myCity == curr ? 0 : endDist.get(curr).get(0));
                    for(Integer k : dist.get(curr).keySet()) {
                        if(toDO.contains(k)) queue.add(k);

                        if(endDist.containsKey(k)) {
                            if(dist.get(curr).get(k) + myDist < endDist.get(k).get(0)) {
                                ArrayList ar = new ArrayList<>();
                                ar.add(dist.get(curr).get(k) + myDist);
                                ar.add(curr);
                                endDist.put(k, ar);

                                if(co.getShops(k).size() != 0 && (minDist > dist.get(curr).get(k) + myDist || minDist == -1)) {
                                    minDist = dist.get(curr).get(k) + myDist;
                                    minNode = k;
                                }
                            }
                        }
                        else {
//                            if(co.getShops(k).size() != 0) {
                                ArrayList ar = new ArrayList<>();
                                ar.add(dist.get(curr).get(k) + myDist);
                                ar.add(curr);
                                endDist.put(k, ar);
                                if(co.getShops(k).size() != 0 && (minDist > dist.get(curr).get(k) + myDist || minDist == -1)) {
                                    minDist = dist.get(curr).get(k) + myDist;
                                    minNode = k;
                                }
//                            }
                        }
                    }
                }

                ps2.setInt(1, idO);
                ResultSet rs2 = ps2.executeQuery();
                ArrayList<Integer> orderCities = new ArrayList<>();
                while(rs2.next()) {
                    orderCities.add(rs2.getInt("IdC"));
                }

                queue.add(minNode);
                while(!queue.isEmpty()) {
                    Integer curr = queue.remove(0);
                    boolean b = toDOLater.remove(curr);
                    int myDist = (minNode == curr ? 0 : endDistMax.get(curr));
                    for(Integer k : dist.get(curr).keySet()) {
                        if(toDOLater.contains(k)) queue.add(k);

                        if(endDistMax.containsKey(k)) {
                            if(dist.get(curr).get(k) + myDist < endDistMax.get(k)) {

                                endDistMax.put(k, dist.get(curr).get(k) + myDist);

                            }
                        }
                        else {
                            endDistMax.put(k, dist.get(curr).get(k) + myDist);
                        }
                    }
                }
                int max = 0;
                for(Integer city : orderCities) {
                    if(city != minNode && endDistMax.get(city) > max) {
                        max = endDistMax.get(city);
                    }
                }
                ArrayList<Integer> ret =  new ArrayList<>();
                ret.add(max);
                ret.add(minNode);
                ret.add(minDist);
                int tmp = minNode;
                while(tmp != myCity) {
                    ret.add(tmp);
                    tmp = endDist.get(tmp).get(1);
                }
                ret.add(myCity);
                return ret;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public int completeOrder(int idO) {
        int idB = getBuyer(idO);
        String status = getState(idO);
        if(!status.equals("created")) return -1;

        List<Integer> items =  getItems(idO);
        if(items == null) return -1;
        if(items.size() == 0) return -1;

        try(PreparedStatement ps = connection.prepareStatement(
                "select IdI from Item i inner join Article a on (i.IdA = a.IdA) where IdO = ? and i.Count > a.Count" );
            PreparedStatement ps1 = connection.prepareStatement(
                    "update Article set [Count] = [Count] - (select [Count] from Item i where IdO = ? and Article.IdA = i.IdA) where IdA in (select IdA from Item where IdO = ?)"
            );
            PreparedStatement ps2 = connection.prepareStatement(
                    "update Item set Discount = (select Discount from Shop s inner join Article a on (a.IdS = s.IdS) where a.IdA = Item.IdA) where IdO = ?"
            );
            PreparedStatement ps3 = connection.prepareStatement(
                    "update [Order] set Sent = ?, BonusDisc = ?, Status = 'sent', IdC = ?, WaitTime = ?, Received = ? where IdO = ?"
            );
            PreparedStatement ps4 = connection.prepareStatement(
                    "select * from [Transaction] t inner join [Order] o on (t.IdO = o.IdO) where o.IdB = ? and t.Amount > 10000 and datediff(day, o.Sent, ?) < 30"
            );
            PreparedStatement ps5 = connection.prepareStatement(
                    "insert into [Path](IdCon, IdO, OrdNum) values ((select IdCon from [Connection] where (IdC1 = ? and IdC2 = ?) or (IdC1 = ? and IdC2 = ?) ), ? , ?)"
            );
            PreparedStatement ps6 = connection.prepareStatement(
                    "insert into [Transaction](Amount, IdCliFrom, IdCliTo, IdO, ExecutionTime) values (?,?,(select min(IdSys) from System),?,?);" +
                            "update Client set Credit = Credit + ? where IdCli in (select IdSys from System);" +
                            "update Client set Credit = Credit - ? where IdCli = ?"
            )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return -1;

            ps1.setInt(1, idO);
            ps1.setInt(2, idO);
            ps1.execute();

            ps2.setInt(1, idO);
            ps2.execute();
            GeneralOperations go = new pi190301_GeneralOperations();
            ps4.setInt(1, idB);
            ps4.setDate(2, new Date(go.getCurrentTime().getTimeInMillis()));
            ResultSet rs4 = ps4.executeQuery();
            boolean bonus = false;
            if(rs4.next()) {
                bonus = true;
            }

            ArrayList<Integer> info = dijkstraConnections(idB, idO); // waittime, node, dist, path
            ps3.setDate(1, new Date(go.getCurrentTime().getTimeInMillis()));
            ps3.setBoolean(2, bonus);

            ps3.setInt(3, info.get(1));
            ps3.setInt(4, info.get(0));
            Calendar date = Calendar.getInstance();
            date.setTime(go.getCurrentTime().getTime());
            date.add(Calendar.DATE, (info.get(0) + info.get(2)));
            ps3.setDate(5, new Date(date.getTimeInMillis()));
            ps3.setInt(6, idO);
            ps3.execute();

            //dodati path

            for(int i = 3; i < info.size() - 1; i++) {
                int idC1 = info.get(i);
                int idC2 = info.get(i + 1);

                ps5.setInt(1, idC1);
                ps5.setInt(2, idC2);
                ps5.setInt(3, idC2);
                ps5.setInt(4, idC1);
                ps5.setInt(5, idO);
                ps5.setInt(6, i - 2);

                ps5.execute();
            }

            BigDecimal amount = getFinalPrice(idO);
            ps6.setBigDecimal(1, amount);
            ps6.setInt(2, idB);
            ps6.setInt(3, idO);
            ps6.setDate(4, new Date(go.getCurrentTime().getTimeInMillis()));
            ps6.setBigDecimal(5, amount);
            ps6.setBigDecimal(6, amount);
            ps6.setInt(7, idB);
            ps6.execute();

            return 1;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public BigDecimal getFinalPrice(int idO) {
        try(CallableStatement cs = connection.prepareCall("{call SP_FINAL_PRICE(?,?)}"))
        {
            cs.setInt(1, idO);
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.execute();
            BigDecimal res = cs.getBigDecimal(2).setScale(3);
            return res;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public BigDecimal getDiscountSum(int idO) {


        try(PreparedStatement ps = connection.prepareStatement(
                "select sum(i.Count * a.Price) as FullPrice, sum(i.Count * a.Price * (100 - i.Discount) / 100) as FinalPrice from Item i inner join Article a on (i.IdA=a.IdA) where i.IdO = ?" );
            PreparedStatement ps1 = connection.prepareStatement(
                    "select BonusDisc as Bonus, Status from [Order] where IdO = ? "
            )
        ){
            ps.setInt(1, idO);
            ps1.setInt(1, idO);
            ResultSet rs1 = ps1.executeQuery();

            if(rs1.next()) {
                String status = rs1.getString("Status");
                boolean bonus = rs1.getBoolean("Bonus");
                if(status.equals("created")) return new BigDecimal("-1");
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {

                    BigDecimal fullprice = rs.getBigDecimal("FullPrice");
                    BigDecimal price =  rs.getBigDecimal("FinalPrice");

                    if(bonus) {
                        price = price.multiply(new BigDecimal("0.98"));
                    }

                    return fullprice.subtract(price).setScale(3);
                }
            }


        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal("-1");
    }

    @Override
    public String getState(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select Status from [Order] where IdO = ?" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString("Status");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getSentTime(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select Sent from [Order] where IdO = ?" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Date date = rs.getDate("Sent");
                if(date == null) return null;
                Calendar time = Calendar.getInstance();
                time.setTime(date);
                return time;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getRecievedTime(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select Received, Status from [Order] where IdO = ?" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Date date = rs.getDate("Received");
                String status = rs.getString("Status");
                if(date == null || !status.equals("arrived")) return null;
                Calendar time = Calendar.getInstance();
                time.setTime(date);
                return time;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getBuyer(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdB from [Order] where IdO = ?" )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("IdB");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getLocation(int idO) {
        try(PreparedStatement ps = connection.prepareStatement(
                "select IdC, WaitTime, Sent from [Order] where IdO = ?" );
            PreparedStatement ps1 = connection.prepareStatement(
                    "select c.IdC1 as IdC1, c.IdC2 as IdC2, c.Distance as Distance from [Path] p inner join [Connection] c on (p.IdCon = c.IdCon) where p.IdO = ? order by p.OrdNum asc"
            )
        ){
            ps.setInt(1, idO);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                int idC = rs.getInt("IdC");
                int waitTime = rs.getInt("WaitTime");
                Date sent = rs.getDate("Sent");

                GeneralOperations go = new pi190301_GeneralOperations();
                Calendar currTime = Calendar.getInstance();
                currTime.setTime(go.getCurrentTime().getTime());

                long diffInMillies = Math.abs(sent.getTime() - currTime.getTimeInMillis());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                if(diff <= waitTime) return  idC;
                diff -= waitTime;

                ps1.setInt(1, idO);
                ResultSet rs1 = ps1.executeQuery();
                int city = idC;
                while(rs1.next()) {
                   int idC1 = rs1.getInt("IdC1");
                   int idC2 = rs1.getInt("IdC2");
                   int dist = rs1.getInt("Distance");

                   if(dist > diff) return city;
                   diff -= dist;
                   city = (idC1 != city ? idC1 : idC2);
                }
                return city;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void main(String[] args) {
        pi190301_OrderOperations op = new pi190301_OrderOperations();
//        System.out.println(op.dijkstraConnections(6, 1));
//        System.out.println(op.completeOrder(1));
        System.out.println(op.getDiscountSum(1));
//        System.out.println(op.getRecievedTime(1));
//        System.out.println(op.getBuyer(1));
//        System.out.println(op.getSentTime(1));
//        System.out.println(op.getArticleCount(1, 3));
//        System.out.println(op.getState(1));
//        System.out.println(op.getItems(1));
    }
}


