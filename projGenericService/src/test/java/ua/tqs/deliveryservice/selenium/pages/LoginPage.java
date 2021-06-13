package ua.tqs.deliveryservice.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class LoginPage {
    private WebDriver driver;

    public LoginPage(WebDriver driver, String baseUrl) {
        this.driver = driver;

        this.driver.get("http://" + baseUrl + ":4200/");
        this.driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")));
        }
    }

    public String login(String email, String password) {
        this.driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")).click();
        this.driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")).sendKeys(email);
        this.driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div:nth-child(2) > div > input")).sendKeys(password);
        this.driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.text-center.form-group > button")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#sidenav-collapse-main > ul > li > a")));
        }
        return this.driver.findElement(By.cssSelector("#sidenav-collapse-main > ul > li > a")).getText();
    }
}
