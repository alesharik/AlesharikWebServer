package tests.com.alesharik.webserver.control.dashboard.elements.menu;

import com.alesharik.webserver.control.dashboard.elements.menu.Menu;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuItem;
import com.alesharik.webserver.control.dashboard.elements.menu.TextMenuItem;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MenuTest {
    private static Menu menu;
    private static Menu read;
    private static Menu sameAsRead;

    private static MenuItem toRemove;
    private static MenuItem inRead;

    @BeforeClass
    public static void setUp() throws Exception {
        menu = new Menu();
        toRemove = new TextMenuItem("qwer", "y");
        menu.addItem(toRemove);
        menu.addItem(new TextMenuItem("ty", "yui"));
        read = new Menu();
        inRead = new TextMenuItem("none", "none");
        read.addItem(inRead);
        read.addItem(new TextMenuItem("asd", "sdf"));
        sameAsRead = new Menu();
        sameAsRead.addItem(new TextMenuItem("none", "none"));
        sameAsRead.addItem(new TextMenuItem("asd", "sdf"));
    }

    @Test
    public void addItem() throws Exception {
        menu.addItem(new TextMenuItem("asdd", "sdfga"));
    }

    @Test(expected = NullPointerException.class)
    public void addItemNull() throws Exception {
        menu.addItem(null);
    }

    @Test
    public void removeItem() throws Exception {
        menu.removeItem(toRemove);
    }

    @Test(expected = NullPointerException.class)
    public void removeItemNull() throws Exception {
        menu.removeItem(null);
    }

    @Test
    public void containsItem() throws Exception {
        assertTrue(read.containsItem(inRead));
        assertFalse(read.containsItem(new TextMenuItem("asdf", "sdf")));
        assertFalse(read.containsItem(null));
    }

    @Test
    public void menuItemList() throws Exception {
        List<MenuItem> object = read.menuItemList();
        assertNotNull(object);
        assertTrue(object.size() == 2);
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue(sameAsRead.equals(read));
        assertFalse(sameAsRead.equals(menu));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(sameAsRead.hashCode(), read.hashCode()) == 0);
        assertFalse(Integer.compare(sameAsRead.hashCode(), menu.hashCode()) == 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(read.toString());
    }
}