package ua.tqs.deliveryservice.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage {
    private WebDriver driver;

    public RegisterPage(WebDriver driver, String baseUrl) {
        this.driver = driver;

        this.driver.get("http://" + baseUrl + ":4200/");
        this.driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.row.mt-3 > div > a")));
        }
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.row.mt-3 > div > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-register > div.container.mt--8.pb-5 > div > div > div > div > form > div:nth-child(1) > div > input")));
        }
    }

    public void register(String email, String password, String name) {
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-register > div.container.mt--8.pb-5 > div > div > div > div > form > div:nth-child(1) > div > input")).click();
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-register > div.container.mt--8.pb-5 > div > div > div > div > form > div:nth-child(1) > div > input")).sendKeys(name);
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-register > div.container.mt--8.pb-5 > div > div > div > div > form > div:nth-child(2) > div > input")).sendKeys(email);
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-register > div.container.mt--8.pb-5 > div > div > div > div > form > div:nth-child(3) > div > input")).sendKeys(password);
        driver.findElement(By.cssSelector("body > app-root > app-auth-layout > div > app-register > div.container.mt--8.pb-5 > div > div > div > div > form > div.text-center.form-group > button")).click();
        {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-auth-layout > div > app-login > div.container.mt--8.pb-5 > div > div > div.card.bg-secondary.shadow.border-0 > div > form > div.form-group.mb-3 > div > input")));
        }
    }
}
