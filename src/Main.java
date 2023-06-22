import org.junit.Assert;
import rs.etf.sab.operations.*;
import rs.etf.sab.student.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
//TODO pregledati coverage izvestaj pokriti preostale fje
public class Main {
    GeneralOperations generalOperations = new pi190301_GeneralOperations();
    ArticleOperations articleOperations = new pi190301_ArticleOperations();
    ShopOperations shopOperations = new pi190301_ShopOperations();
    CityOperations cityOperations = new pi190301_CityOperations();
    BuyerOperations buyerOperations = new pi190301_BuyerOperations();
    OrderOperations orderOperations = new pi190301_OrderOperations();
    TransactionOperations transactionOperations = new pi190301_TransactionOperations();

    void test() {
        Calendar initialTime = Calendar.getInstance();
        initialTime.clear();
        initialTime.set(2018, 0, 1);
        this.generalOperations.setInitialTime(initialTime);
        Calendar receivedTime = Calendar.getInstance();
        receivedTime.clear();
        receivedTime.set(2018, 0, 22);
        int cityB = this.cityOperations.createCity("B");
        int cityC1 = this.cityOperations.createCity("C1");
        int cityA = this.cityOperations.createCity("A");
        int cityC2 = this.cityOperations.createCity("C2");
        int cityC3 = this.cityOperations.createCity("C3");
        int cityC4 = this.cityOperations.createCity("C4");
        int cityC5 = this.cityOperations.createCity("C5");
        this.cityOperations.connectCities(cityB, cityC1, 8);
        this.cityOperations.connectCities(cityC1, cityA, 10);
        this.cityOperations.connectCities(cityA, cityC2, 3);
        this.cityOperations.connectCities(cityC2, cityC3, 2);
        this.cityOperations.connectCities(cityC3, cityC4, 1);
        this.cityOperations.connectCities(cityC4, cityA, 3);
        this.cityOperations.connectCities(cityA, cityC5, 15);
        this.cityOperations.connectCities(cityC5, cityB, 2);
        int shopA = this.shopOperations.createShop("shopA", "A");
        int shopC2 = this.shopOperations.createShop("shopC2", "C2");
        int shopC3 = this.shopOperations.createShop("shopC3", "C3");
        this.shopOperations.setDiscount(shopA, 20);
        this.shopOperations.setDiscount(shopC2, 50);
        int laptop = this.articleOperations.createArticle(shopA, "laptop", 1000);
        int monitor = this.articleOperations.createArticle(shopC2, "monitor", 200);
        int stolica = this.articleOperations.createArticle(shopC3, "stolica", 100);
        int sto = this.articleOperations.createArticle(shopC3, "sto", 200);
        this.shopOperations.increaseArticleCount(laptop, 10);
        this.shopOperations.increaseArticleCount(monitor, 10);
        this.shopOperations.increaseArticleCount(stolica, 10);
        this.shopOperations.increaseArticleCount(sto, 10);
        int buyer = this.buyerOperations.createBuyer("kupac", cityB);
        this.buyerOperations.increaseCredit(buyer, new BigDecimal("20000"));
        int order = this.buyerOperations.createOrder(buyer);
        this.orderOperations.addArticle(order, laptop, 5);
        this.orderOperations.addArticle(order, monitor, 4);
        this.orderOperations.addArticle(order, stolica, 10);
        this.orderOperations.addArticle(order, sto, 4);
        Assert.assertNull(this.orderOperations.getSentTime(order));
        Assert.assertTrue("created".equals(this.orderOperations.getState(order)));
        this.orderOperations.completeOrder(order);
        Assert.assertTrue("sent".equals(this.orderOperations.getState(order)));
        int buyerTransactionId = (Integer)this.transactionOperations.getTransationsForBuyer(buyer).get(0);
        Assert.assertEquals(initialTime, this.transactionOperations.getTimeOfExecution(buyerTransactionId));
        Assert.assertNull(this.transactionOperations.getTransationsForShop(shopA));
        BigDecimal shopAAmount = (new BigDecimal("5")).multiply(new BigDecimal("1000")).setScale(3);
        BigDecimal shopAAmountWithDiscount = (new BigDecimal("0.8")).multiply(shopAAmount).setScale(3);
        BigDecimal shopC2Amount = (new BigDecimal("4")).multiply(new BigDecimal("200")).setScale(3);
        BigDecimal shopC2AmountWithDiscount = (new BigDecimal("0.5")).multiply(shopC2Amount).setScale(3);
        BigDecimal shopC3Amount = (new BigDecimal("10")).multiply(new BigDecimal("100")).add((new BigDecimal("4")).multiply(new BigDecimal("200"))).setScale(3);
        BigDecimal amountWithoutDiscounts = shopAAmount.add(shopC2Amount).add(shopC3Amount).setScale(3);
        BigDecimal amountWithDiscounts = shopAAmountWithDiscount.add(shopC2AmountWithDiscount).add(shopC3Amount).setScale(3);
        BigDecimal systemProfit = amountWithDiscounts.multiply(new BigDecimal("0.05")).setScale(3);
        BigDecimal shopAAmountReal = shopAAmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        BigDecimal shopC2AmountReal = shopC2AmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        BigDecimal shopC3AmountReal = shopC3Amount.multiply(new BigDecimal("0.95")).setScale(3);
        Assert.assertEquals(amountWithDiscounts, this.orderOperations.getFinalPrice(order));
        Assert.assertEquals(amountWithoutDiscounts.subtract(amountWithDiscounts), this.orderOperations.getDiscountSum(order));
        Assert.assertEquals(amountWithDiscounts, this.transactionOperations.getBuyerTransactionsAmmount(buyer));
        Assert.assertEquals(this.transactionOperations.getShopTransactionsAmmount(shopA), (new BigDecimal("0")).setScale(3));
        Assert.assertEquals(this.transactionOperations.getShopTransactionsAmmount(shopC2), (new BigDecimal("0")).setScale(3));
        Assert.assertEquals(this.transactionOperations.getShopTransactionsAmmount(shopC3), (new BigDecimal("0")).setScale(3));
        Assert.assertEquals((new BigDecimal("0")).setScale(3), this.transactionOperations.getSystemProfit());
        this.generalOperations.time(2);
        Assert.assertEquals(initialTime, this.orderOperations.getSentTime(order));
        Assert.assertNull(this.orderOperations.getRecievedTime(order));
        Assert.assertEquals((long)this.orderOperations.getLocation(order), (long)cityA);
        this.generalOperations.time(9);
        Assert.assertEquals((long)this.orderOperations.getLocation(order), (long)cityA);
        this.generalOperations.time(8);
        Assert.assertEquals((long)this.orderOperations.getLocation(order), (long)cityC5);
        this.generalOperations.time(5);
        Assert.assertEquals((long)this.orderOperations.getLocation(order), (long)cityB);
        Assert.assertEquals(receivedTime, this.orderOperations.getRecievedTime(order));
        Assert.assertEquals(shopAAmountReal, this.transactionOperations.getShopTransactionsAmmount(shopA));
        Assert.assertEquals(shopC2AmountReal, this.transactionOperations.getShopTransactionsAmmount(shopC2));
        Assert.assertEquals(shopC3AmountReal, this.transactionOperations.getShopTransactionsAmmount(shopC3));
        Assert.assertEquals(systemProfit, this.transactionOperations.getSystemProfit());
        int shopATransactionId = this.transactionOperations.getTransactionForShopAndOrder(order, shopA);
        Assert.assertNotEquals(-1L, (long)shopATransactionId);
        Assert.assertEquals(receivedTime, this.transactionOperations.getTimeOfExecution(shopATransactionId));
    }

    public static void main(String[] args) {
//        GeneralOperations go = new pi190301_GeneralOperations();
//        go.eraseAll();
//        go.setInitialTime(Calendar.getInstance());
//        System.out.println(go.getCurrentTime());
//        go.time(15);
//        System.out.println(go.getCurrentTime());
//        CityOperations co = new pi190301_CityOperations();
//        ShopOperations so = new pi190301_ShopOperations();
//        ArticleOperations ao = new pi190301_ArticleOperations();
//        BuyerOperations bo = new pi190301_BuyerOperations();
//        OrderOperations op = new pi190301_OrderOperations();
//        System.out.println(co.createCity("Beograd"));
//        System.out.println(co.createCity("Novi Sad"));
//        System.out.println(co.createCity("Nis"));
//        System.out.println(co.createCity("Sombor"));
//        System.out.println(co.createCity("Subotica"));
//        System.out.println(co.createCity("Loznica"));
//        System.out.println(co.connectCities(1,2,20));
//        System.out.println(co.connectCities(1,3,1));
//        System.out.println(co.connectCities(2,3,15));
//        System.out.println(co.connectCities(4,2,1));
//        System.out.println(co.connectCities(3,4,4));
//        System.out.println(co.connectCities(5,4,7));
//        System.out.println(co.connectCities(6,1,6));
//        System.out.println(co.getCities());
//        System.out.println(co.getConnectedCities(1));
//
//        System.out.println(so.createShop("S1", "Beograd"));
//        System.out.println(so.createShop("S2", "Nis"));
//        System.out.println(so.createShop("S3", "Subotica"));
//        System.out.println(so.createShop("S4", "Loznica"));
//        System.out.println(co.getShops(2));
//
//        System.out.println(ao.createArticle(2,"A1", 50));
//        System.out.println(ao.createArticle(3,"A2", 10));
//        System.out.println(ao.createArticle(5,"A3", 20));
//        System.out.println(ao.createArticle(6,"A4", 30));
//
////        System.out.println(so.getCity(2));
////        System.out.println(so.setCity(2, "Novi Sad"));
////        System.out.println(so.getCity(2));
//
//        System.out.println(so.setDiscount(2, 50));
//        System.out.println(so.getDiscount(2));
//
//        System.out.println(so.getArticleCount(1));
//        System.out.println(so.increaseArticleCount(1, 15));
//        System.out.println(so.increaseArticleCount(2, 15));
//        System.out.println(so.increaseArticleCount(3, 15));
//        System.out.println(so.increaseArticleCount(4, 15));
//
//        System.out.println(so.getArticles(2));
//        System.out.println(bo.createBuyer("Iva", 2));
//
//        //odradjeni getcity set city
////
//        System.out.println(bo.getCredit(6));
//        System.out.println(bo.increaseCredit(6,new BigDecimal("25")));
//
//        System.out.println(bo.createOrder(6));
//        System.out.println(bo.getOrders(6));
//
//        System.out.println(op.addArticle(1,2,5));
//        System.out.println(op.addArticle(1,1,25));
//        System.out.println(op.addArticle(1,4,5));
//        System.out.println(op.addArticle(1,3,5));
//        System.out.println(op.addArticle(1,3,2));
//        System.out.println(op.removeArticle(1, 2));
//        System.out.println(so.getCity(5));

//        new Main().test();
        Main maincl = new Main();
        System.out.println(maincl.transactionOperations.getTransactionForBuyersOrder(2));
    }
}