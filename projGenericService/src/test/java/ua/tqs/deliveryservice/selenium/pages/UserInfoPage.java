package ua.tqs.deliveryservice.selenium.pages;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
}
