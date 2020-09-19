package org.example.cucumber;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.After;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class StepDefinitions  {

    String itemName = "15.6\" Ноутбук Acer Aspire 3 A315-42G-R6EF (NX.HF8ER.03A), черный";

    private ChromeDriver chromeDriver;
    private WebDriverWait wait;
    private Integer searchResultItemPrice;
    private String searchResultItemName;

    @Given("User is logged into Ozon.ru site")
    public void userIsLoggedIntoSite() throws Exception {
        URL chromeExecutableUrl = ClassLoader.getSystemClassLoader().getResource("driver/chromedriver.exe");
        Assert.assertNotNull(chromeExecutableUrl);
        File chromeDriverExecutable = new File(chromeExecutableUrl.toURI());
        ChromeDriverService service = new ChromeDriverService(chromeDriverExecutable,
                9515, ImmutableList.<String>builder().build(), ImmutableMap.<String, String>builder().build());
        chromeDriver = new ChromeDriver(service);
        wait = new WebDriverWait(chromeDriver, 50);
        chromeDriver.manage().window().maximize();
        chromeDriver.get("https://www.ozon.ru/");
    }

    @And("User searches a product on homepage")
    public void userSearchAProduct() {
        WebElement findItem = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Искать на Ozon']")));
        findItem.sendKeys(itemName);
        findItem.submit();
    }

    @And("User can see item name and price in search result")
    public void userSeeAllInformation() {
        WebElement searchResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-widget=searchResultsV2]")));
        List<WebElement> spansInSearchResult = searchResult.findElements(By.tagName("span"));

        Optional<Integer> optionalPrice = spansInSearchResult.stream()
                .map(WebElement::getText)
                .map(webElementText -> webElementText
                        .replaceAll("\\s", "")
                        .replaceAll("₽", "")
                        .replaceAll("\u2009", ""))
                .filter(NumberUtils ::isNumeric)
                .map(Integer::valueOf)
                .min(Integer::compareTo);

        Assert.assertTrue(optionalPrice.isPresent());
        searchResultItemPrice = optionalPrice.get();

        List<WebElement> links = searchResult.findElements(By.tagName("a"));
        Optional<String> optionalName = links.stream()
                .map(WebElement::getText)
                .filter(linkText -> !linkText.isEmpty())
                .filter(linkText -> linkText.contains(itemName)).findFirst();

        Assert.assertTrue(optionalName.isPresent());
        searchResultItemName = optionalName.get();
    }

    @And("User add product to cart")
    public void addToCart() throws InterruptedException {
        WebElement searchResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-widget=searchResultsV2]")));
        List<WebElement> buttons = searchResult.findElements(By.tagName("button"));
        buttons.stream()
                .filter(button -> button.findElement(By.xpath("div/div")).getText().equals("В корзину"))
                .findFirst()
                .ifPresent(WebElement::click);
        Thread.sleep(2000);
    }

    @When("User go to cart")
    public void userGoToCart(){
        chromeDriver.findElement(By.cssSelector("a[href=\\/cart]")).click();
    }

    @Then("User sees the same item name and price")
    public void sameProductAndPriceInCart(){
        WebElement itemsInCartFrame = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-widget='split']")));
        List<WebElement> itemsInCartFrameSpans = itemsInCartFrame.findElements(By.tagName("span"));

        Optional<Integer> priceInCart =  itemsInCartFrameSpans.stream()
                .map(WebElement::getText)
                .map(webElementText -> webElementText.replaceAll("\\s", "").replaceAll("₽", ""))
                .filter(NumberUtils::isNumeric)
                .map(Integer::valueOf)
                .min(Integer::compareTo);


        Assert.assertTrue(priceInCart.isPresent());
        Assert.assertEquals(searchResultItemPrice ,priceInCart.get());

        Optional<String> optionalItemNameInCart = itemsInCartFrameSpans.stream()
                .map(WebElement::getText)
                .filter(linkText -> !linkText.isEmpty())
                .filter(linkText -> linkText.contains(itemName)).findFirst();

        Assert.assertTrue(optionalItemNameInCart.isPresent());
        Assert.assertEquals(searchResultItemName, optionalItemNameInCart.get());
    }

    @After()
    public void closeBrowser() {
        chromeDriver.quit();
    }

}
