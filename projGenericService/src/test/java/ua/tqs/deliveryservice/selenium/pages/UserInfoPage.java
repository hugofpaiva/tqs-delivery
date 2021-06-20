package ua.tqs.deliveryservice.selenium.pages;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.model.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

public class UserInfoPage {
    private WebDriver driver;

    public UserInfoPage(WebDriver driver) {
        this.driver = driver;
    }

    public void logoutRider() {
        this.driver.findElement(By.cssSelector("#navbar-main > div > ul > li > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#navbar-main > div > ul > li > div > a:nth-child(4)")));
        }
        this.driver.findElement(By.cssSelector("#navbar-main > div > ul > li > div > a:nth-child(4)")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")));
        }
        assertThat(this.driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")).getText(), Matchers.is("Sign in with credentials"));
    }

    public boolean isEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[2]/div/div[2]/div/div/div/div[1]/h3")));
        }
        if (this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[2]/div/div[2]/div/div/div/div[1]/h3")).getText().equals("There are no deliveries history")) {
            return true;
        } else {
            return false;
        }
    }

    public Integer getTotalDeliveries() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[1]/div/div[3]/div/div/div/div[1]/span[1]")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[1]/div/div[3]/div/div/div/div[1]/span[1]")).getText());

    }

    public Integer getTotalReviews() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[1]/div/div[3]/div/div/div/div[2]/span[1]")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[1]/div/div[3]/div/div/div/div[2]/span[1]")).getText());

    }

    public Double getAvgRating() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[1]/div/div[3]/div/div/div/div[3]/span[1]")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Double.parseDouble(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[1]/div/div[3]/div/div/div/div[3]/span[1]")).getText());

    }

    public List<Purchase> getOrders() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[2]/div/div[2]/div/div/div/div[1]/table/tbody")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        WebElement table = this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-user-profile/div[2]/div/div[2]/div/div[2]/div/div/div/div[1]/table/tbody"));

        List<WebElement> purchasesElements = table.findElements(By.xpath("./child::*"));

        List<Purchase> purchases = new ArrayList<>();

        for (WebElement purchase : purchasesElements) {
            Purchase p = new Purchase();
            Store s = new Store();
            s.setName(purchase.findElement(By.xpath(".//td[1]")).getText());
            p.setStore(s);
            p.setClientName(purchase.findElement(By.xpath(".//td[2]")).getText());
            p.setStatus(Status.valueOf(purchase.findElement(By.xpath(".//td[3]/span/text()")).getText()));
            purchases.add(p);
        }

        return purchases;
    }
}
