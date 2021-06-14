package ua.tqs.humberpecas.selenium.pages;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.hamcrest.MatcherAssert.assertThat;

public class ShopPage {
    private WebDriver driver;

    public ShopPage(WebDriver driver) {
        this.driver = driver;
    }

    public void logoutRider() {
        this.driver.findElement(By.cssSelector("#navbar_global > ul > li.nav-item.d-none.d-lg-block.ml-lg-4 > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-login > main > section > div.container.pt-lg-md > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")));
        }

        assertThat(this.driver.findElement(By.cssSelector("body > app-root > app-login > main > section > div.container.pt-lg-md > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")).getText(), Matchers.is("Sign in with credentials"));
    }
}
