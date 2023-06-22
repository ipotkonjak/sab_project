import rs.etf.sab.operations.*;
import org.junit.Test;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

import java.util.Calendar;

public class StudentMain {

    public static void main(String[] args) {

        ArticleOperations articleOperations = new pi190301_ArticleOperations(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new pi190301_BuyerOperations();
        CityOperations cityOperations = new pi190301_CityOperations();
        GeneralOperations generalOperations = new pi190301_GeneralOperations();
        OrderOperations orderOperations = new pi190301_OrderOperations();
        ShopOperations shopOperations = new pi190301_ShopOperations();
        TransactionOperations transactionOperations = new pi190301_TransactionOperations();

        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();
    }
}
