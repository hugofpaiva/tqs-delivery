package ua.tqs.deliveryservice.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.deliveryservice.model.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StoresInfoPage {
    private WebDriver driver;

    public StoresInfoPage(WebDriver driver, String baseUrl, String name) {
        this.driver = driver;

        this.driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"navbar-main\"]/div/ul/li/a/div/div/span")));
        }
        assertThat(this.driver.findElement(By.xpath("//*[@id=\"navbar-main\"]/div/ul/li/a/div/div/span")).getText(), is(name));
    }

    public String getUserName(){
        return this.driver.findElement(By.xpath("//*[@id=\"navbar-main\"]/div/ul/li/a/div/div/span")).getText();
    }

    public void logoutManager() {
        this.driver.findElement(By.cssSelector("#navbar-main > div > ul > li > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#navbar-main > div > ul > li > div > a")));
        }
        this.driver.findElement(By.cssSelector("#navbar-main > div > ul > li > div > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")));
        }
        assertThat(this.driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")).getText(), is("Sign in with credentials"));
    }

    public void goToRidersInfo() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"sidenav-collapse-main\"]/ul/li[2]/a")));
        }
        this.driver.findElement(By.xpath("//*[@id=\"sidenav-collapse-main\"]/ul/li[2]/a")).click();
    }

    public List<Store> getStores() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[2]/div/div/div/div[2]/table/tbody")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        WebElement table = this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[2]/div/div/div/div[2]/table/tbody"));

        List<WebElement> storesElements = table.findElements(By.xpath("./child::*"));

        List<Store> stores = new ArrayList<>();

        for (WebElement store : storesElements) {
            Store s = new Store();
            s.setName(store.findElement(By.xpath(".//th/div/span")).getText());
            s.setDescription(store.findElement(By.xpath(".//td[1]")).getText());
            s.setId(Integer.parseInt(store.findElement(By.xpath(".//td[2]")).getText()));
            stores.add(s);
        }

        return stores;
    }

    public Integer getTotalStores() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[1]/div/div/div/div[1]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[1]/div/div/div/div[1]/div/div/div/div[1]/span")).getText());

    }

    public Integer getTotalOrders() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[1]/div/div/div/div[2]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[1]/div/div/div/div[2]/div/div/div/div[1]/span")).getText());

    }

    public Integer getAverageOrdersByWeek() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[1]/div/div/div/div[3]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[1]/div/div/div/div[3]/div/div/div/div[1]/span")).getText());

    }

    public boolean isEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[2]/div/div/div/div[2]/h3")));
        }
        if (this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-stores/div[2]/div/div/div/div[2]/h3")).getText().equals("There are no Stores")) {
            return true;
        } else {
            return false;
        }
    }

}
