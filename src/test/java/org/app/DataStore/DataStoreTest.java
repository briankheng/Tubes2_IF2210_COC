package org.app.DataStore;

import org.app.Customer.Customer;
import org.app.Inventory.Holder.Bill;
import org.app.Inventory.Holder.FixedBill;
import org.app.Inventory.Item.BillItem;
import org.app.Inventory.Item.Item;
import org.app.Setting.Setting;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class DataStoreTest {
    @Test
    void setting_save_and_load_test() {
        DataStore store = new DataStore();
        Setting.getInstance().setStoragePath("dataStore");
        store.save();
        Setting.getInstance().setStoragePath("");
        store.loadSettings();
        Assertions.assertEquals(Setting.getInstance().getStoragePath(), "dataStore");
    }

    @Test
    void data_store_as_data_initializer() {
        DataStore store = new DataStore();
        store.load();
        Assertions.assertNotNull(store.inventory());
        store.save();
    }

    @Test
    void data_store_is_consistent() {
        // Fill data
        DataStore store = new DataStore();
        for (int i = 0; i < 15; i++){
            store.customers().getCustomerList().add(new Customer(i, new ArrayList<>()));
            Random random = new Random();
            int status = random.nextInt() % 3;
            if (status == 1) {
                store.customers().turnToMember(i,
                        "nama customer ke-" + i, "no telp");
            }
            else {
                store.customers().turnToVIP(i,
                        "nama customer ke-" + i, "no telp");
            }

            Bill bill = Bill.builder().user(i).itemList(new ArrayList<>()).build();
            bill.addItem(new BillItem(Item.builder()
                    .itemName("item")
                    .category("makanan")
                    .imagePath("image/item.png")
                    .build()));
            store.customers().getCustomerFromID(i).getBills().add(
                    new FixedBill(bill));

            store.cashier().addBill(bill);
        }

        for (int i = 0; i < 5; i++) {
            store.inventory().addItem(Item.builder()
                    .itemName("item inventory")
                    .category("makanan")
                    .imagePath("image/item2.png")
                    .build());
        }




        // Store old values
        String oldPath = Setting.getInstance().getStoragePath();
        String oldDataFormat = Setting.getInstance().getDataFormat();

        // Switch to xml (to test change changeDataFormat())
        Setting.getInstance().setDataFormat("xml");
        Setting.getInstance().setStoragePath("dataStoreTest");
        store.save();

        // Test json
        DataStore JsonStoreResult = new DataStore();
        Setting.getInstance().setDataFormat("json");
        Setting.getInstance().setStoragePath("dataStoreTest");
        store.save();
        JsonStoreResult.load();

        Assertions.assertEquals(store.inventory(), JsonStoreResult.inventory());
        Assertions.assertEquals(store.cashier(), JsonStoreResult.cashier());
        Assertions.assertEquals(store.customers(), JsonStoreResult.customers());

        // Test xml
        DataStore XmlStoreResult = new DataStore();
        Setting.getInstance().setDataFormat("xml");
        Setting.getInstance().setStoragePath("dataStoreTest");
        store.save();
        XmlStoreResult.load();

        assertThat(store.inventory())
                .usingRecursiveComparison()
                .isEqualTo(XmlStoreResult.inventory());
        assertThat(store.cashier())
                .usingRecursiveComparison()
                .isEqualTo(XmlStoreResult.cashier());
        assertThat(store.customers())
                .usingRecursiveComparison()
                .isEqualTo(XmlStoreResult.customers());

        // Test obj
        DataStore ObjStoreResult = new DataStore();
        Setting.getInstance().setDataFormat("obj");
        Setting.getInstance().setStoragePath("dataStoreTest");
        store.save();
        ObjStoreResult.load();

        assertThat(store.inventory())
                .usingRecursiveComparison()
                .isEqualTo(ObjStoreResult.inventory());
        assertThat(store.cashier())
                .usingRecursiveComparison()
                .isEqualTo(ObjStoreResult.cashier());
        assertThat(store.customers())
                .usingRecursiveComparison()
                .isEqualTo(ObjStoreResult.customers());


        // Turn to old values
        Setting.getInstance().setStoragePath(oldPath);
        Setting.getInstance().setDataFormat(oldDataFormat);
        store.save();

        // Delete folder
        try {
            FileUtils.deleteDirectory(new File("dataStoreTest"));
        }
        catch (Exception e) {

        }
    }
}
